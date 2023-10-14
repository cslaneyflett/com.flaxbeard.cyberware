package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import flaxbeard.cyberware.common.registry.CWBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockEngineeringTable extends HorizontalDirectionalBlock implements EntityBlock
{
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	public BlockEngineeringTable(Properties pProperties)
	{
		super(pProperties);

		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(HALF, DoubleBlockHalf.LOWER)
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder
			.add(FACING)
			.add(HALF);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState)
	{
		return pState.getValue(HALF) == DoubleBlockHalf.UPPER
			? new TileEntityEngineeringTable(pPos, pState)
			: new TileEntityEngineeringTable.TileEntityEngineeringDummy(pPos, pState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state,
																  @Nonnull BlockEntityType<T> type)
	{
		return type == CWBlockEntities.ENGINEERING_TABLE.get() ? TileEntityEngineeringTable::tick : null;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
	{
		var pos = context.getClickedPos();
		var level = context.getLevel();

		if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context))
		{
			return this.defaultBlockState()
				.setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(HALF, DoubleBlockHalf.LOWER);
		} else
		{
			return null;
		}
	}

	@Override
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
							LivingEntity placer, @Nonnull ItemStack stack)
	{
		level.setBlockAndUpdate(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER));
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos usePos,
								 @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit)
	{
		// The real entity is stored in the top half.
		var pos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? usePos : usePos.above();

		if (level.getBlockEntity(pos) instanceof TileEntityEngineeringTable blockEntity)
		{
			// TODO
			// player.openGui(Cyberware.INSTANCE, 2, world, pos.getX(), pos.getY(), pos.getZ());

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.FAIL;
	}

	private static final VoxelShape SHAPE_SOUTH = Shapes.create(
		new AABB(4F / 16F, 0F, 0F / 16F, 12F / 16F, 1F, 12F / 16F)
	);
	private static final VoxelShape SHAPE_NORTH = Shapes.create(
		new AABB(4F / 16F, 0F, 4F / 16F, 12F / 16F, 1F, 16F / 16F)
	);
	private static final VoxelShape SHAPE_EAST = Shapes.create(
		new AABB(0F / 16F, 0F, 4F / 16F, 12F / 16F, 1F, 12F / 16F)
	);
	private static final VoxelShape SHAPE_WEST = Shapes.create(
		new AABB(4F / 16F, 0F, 4F / 16F, 16F / 16F, 1F, 12F / 16F)
	);

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
							   @Nonnull CollisionContext context)
	{
		return switch (state.getValue(FACING).getOpposite())
		{
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			case EAST -> SHAPE_EAST;
			default -> SHAPE_NORTH;
		};
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Override
	public boolean canSurvive(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull BlockPos pos)
	{
		var below = pos.below();
		var stateBelow = level.getBlockState(below);

		return state.getValue(HALF) == DoubleBlockHalf.LOWER
			? stateBelow.isFaceSturdy(level, below, Direction.UP)
			: stateBelow.is(this);
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Override
	public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean moving)
	{
		if (level.getBlockEntity(pos) instanceof TileEntityEngineeringTable blockEntity)
		{
			var handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElseThrow();
			for (int i = 0; i < handler.getSlots(); i++)
			{
				var stack = handler.getStackInSlot(i);

				if (!stack.isEmpty())
				{
					level.addFreshEntity(new ItemEntity(
						level, pos.getX(), pos.getY(), pos.getZ(),
						stack
					));
				}
			}
		}

		super.onRemove(state, level, pos, newState, moving);
	}
}
