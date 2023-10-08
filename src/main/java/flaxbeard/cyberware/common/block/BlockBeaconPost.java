package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityBeaconPost;
import flaxbeard.cyberware.common.registry.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class BlockBeaconPost extends FenceBlock implements EntityBlock
{
	// N/E/S/W and waterlogged are on CrossCollisionBlock
	// TODO: EnumProperty for less magic numbers
	public static final IntegerProperty TRANSFORMED = IntegerProperty.create("transformed", 0, 2);

	public BlockBeaconPost(Properties pProperties)
	{
		super(pProperties);

		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(TRANSFORMED, 0)
				.setValue(NORTH, Boolean.FALSE)
				.setValue(EAST, Boolean.FALSE)
				.setValue(SOUTH, Boolean.FALSE)
				.setValue(WEST, Boolean.FALSE)
		);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState)
	{
		return switch (pState.getValue(TRANSFORMED))
		{
			case 1 -> new TileEntityBeaconPost(pPos, pState);
			case 2 -> new TileEntityBeaconPost.TileEntityBeaconPostMaster(pPos, pState);
			default -> null;
		};
	}

	@Override
	public boolean connectsTo(BlockState pState, boolean pIsSideSolid, @Nonnull Direction pDirection)
	{
		Block block = pState.getBlock();

		return /*!isExceptionForConnection(pState) && pIsSideSolid ||*/ block == this;
	}

	@Override
	public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity)
	{
		return super.isLadder(state, level, pos, entity) || state.getValue(TRANSFORMED) > 0;
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		return state.getValue(TRANSFORMED) > 0
			? RenderShape.INVISIBLE
			: RenderShape.MODEL;
	}

	@Override
	public void destroy(@Nonnull LevelAccessor level, @Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		if (state.getValue(TRANSFORMED) > 0 && level.getBlockEntity(pos) instanceof TileEntityBeaconPost tileEntityBeaconPost)
		{
			if (state.getValue(TRANSFORMED) == 2)
			{
				tileEntityBeaconPost.destruct();
			} else if (tileEntityBeaconPost.master != null &&
				!tileEntityBeaconPost.master.equals(pos) &&
				!tileEntityBeaconPost.destructing &&
				level.getBlockEntity(tileEntityBeaconPost.master) instanceof TileEntityBeaconPost.TileEntityBeaconPostMaster master)
			{
				if (!master.destructing)
				{
					master.destruct();
				}
			}
		}

		super.destroy(level, pos, state);
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Override
	public void onPlace(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
						@Nonnull BlockState oldState, boolean isMoving)
	{
		StructureValidator.tryCompleteFromNode(level, pos);
	}

	// TODO: this is horrifically inefficient, i've abstracted it but there must be a better way
	private static class StructureValidator
	{
		private enum ScanResult
		{
			RETURN_FALSE,
			RETURN_TRUE,
			CONTINUE,
			COMPLETE;
		}

		private static ScanResult scanAround(
			BlockPos pos,
			int x1, int x2, int y1, int y2, int z1, int z2,
			Function<BlockPos, ScanResult> handle)
		{
			for (int x = x1; x <= x2; x++)
			{
				for (int y = y1; y <= y2; y++)
				{
					for (int z = z1; z <= z2; z++)
					{
						var res = handle.apply(pos.offset(x, y, z));

						if (res != ScanResult.CONTINUE)
						{
							return res;
						}
					}
				}
			}

			return ScanResult.COMPLETE;
		}

		private static void tryCompleteFromNode(Level level, BlockPos node)
		{
			scanAround(
				node,
				-1, 1, -9, 0, -1, 1,
				(p) -> validateAndCompleteStructure(level, p)
					? ScanResult.RETURN_TRUE
					: ScanResult.CONTINUE
			);
		}

		private static boolean validateAndCompleteStructure(Level level, BlockPos start)
		{
			// First pass validates that we only have posts, and they are not already transformed.
			var firstPass = scanAround(
				start,
				-1, 1, 0, 9, -1, 1,
				(p) -> {
					if (p.getY() > 3 && (p.getX() != 0 || p.getY() != 0)) return ScanResult.CONTINUE;

					var state = level.getBlockState(p);
					var block = state.getBlock();
					if (block != Blocks.BEACON_POST.get() || state.getValue(TRANSFORMED) != 0)
					{
						return ScanResult.RETURN_FALSE;
					}

					return ScanResult.CONTINUE;
				}
			);

			if (firstPass == ScanResult.RETURN_FALSE) return false;

			// Second pass assigns master locations to every post, and transforms them.
			scanAround(
				start,
				-1, 1, 0, 9, -1, 1,
				(p) -> {
					if (p.getY() > 3 && (p.getX() != 0 || p.getY() != 0)) return ScanResult.CONTINUE;

					var state = level.getBlockState(p);
					level.setBlockAndUpdate(p, state.setValue(TRANSFORMED, p.equals(start) ? 2 : 1));

					if (!p.equals(start))
					{
						TileEntityBeaconPost post = (TileEntityBeaconPost) level.getBlockEntity(p);
						assert post != null;

						post.setMasterLoc(start);
					}

					return ScanResult.CONTINUE;
				}
			);

			return true;
		}
	}
}