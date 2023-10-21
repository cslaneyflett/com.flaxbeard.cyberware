package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgeryChamber;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlockSurgeryChamber extends HorizontalDirectionalBlock implements EntityBlock
{
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	public BlockSurgeryChamber(Properties pProperties)
	{
		super(pProperties);

		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(OPEN, false)
				.setValue(FACING, Direction.NORTH)
				.setValue(HALF, DoubleBlockHalf.LOWER)
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder
			.add(FACING)
			.add(OPEN)
			.add(HALF);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState)
	{
		return pState.getValue(HALF) == DoubleBlockHalf.UPPER
			? new TileEntitySurgeryChamber(pPos, pState)
			: null;
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
			if (this.canOpen(pos, level))
			{
				this.toggleDoor(!state.getValue(OPEN), state, pos, level);
				this.notifySurgeon(pos, level);
			} else
			{
				return InteractionResult.FAIL;
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.FAIL;
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

	private static final VoxelShape SHAPE_TOP = Shapes.create(
		new AABB(0F, 15F / 16F, 0F, 1F, 1F, 1F)
	);
	private static final VoxelShape SHAPE_SOUTH = Shapes.create(
		new AABB(0F, 0F, 0F, 1F, 1F, 1F / 16F)
	);
	private static final VoxelShape SHAPE_NORTH = Shapes.create(
		new AABB(0F, 0F, 15F / 16F, 1F, 1F, 1F)
	);
	private static final VoxelShape SHAPE_EAST = Shapes.create(
		new AABB(0F, 0F, 0F, 1F / 16F, 1F, 1F)
	);
	private static final VoxelShape SHAPE_WEST = Shapes.create(
		new AABB(15F / 16F, 0F, 0F, 1F, 1F, 1F)
	);
	private static final VoxelShape SHAPE_BOTTOM = Shapes.create(
		new AABB(0F, 0F, 0F, 1F, 1F / 16F, 1F)
	);
	private static final VoxelShape SHAPE_TOP_SOUTH = mergeAllButFaceToHalf(DoubleBlockHalf.UPPER, Direction.SOUTH);
	private static final VoxelShape SHAPE_TOP_NORTH = mergeAllButFaceToHalf(DoubleBlockHalf.UPPER, Direction.NORTH);
	private static final VoxelShape SHAPE_TOP_EAST = mergeAllButFaceToHalf(DoubleBlockHalf.UPPER, Direction.EAST);
	private static final VoxelShape SHAPE_TOP_WEST = mergeAllButFaceToHalf(DoubleBlockHalf.UPPER, Direction.WEST);
	private static final VoxelShape SHAPE_BOTTOM_SOUTH = mergeAllButFaceToHalf(DoubleBlockHalf.LOWER, Direction.SOUTH);
	private static final VoxelShape SHAPE_BOTTOM_NORTH = mergeAllButFaceToHalf(DoubleBlockHalf.LOWER, Direction.NORTH);
	private static final VoxelShape SHAPE_BOTTOM_EAST = mergeAllButFaceToHalf(DoubleBlockHalf.LOWER, Direction.EAST);
	private static final VoxelShape SHAPE_BOTTOM_WEST = mergeAllButFaceToHalf(DoubleBlockHalf.LOWER, Direction.WEST);

	private static VoxelShape mergeAllButFaceToHalf(DoubleBlockHalf half, Direction face)
	{
		List<VoxelShape> merge = switch (face)
		{
			case SOUTH -> Arrays.asList(SHAPE_NORTH, SHAPE_EAST, SHAPE_WEST);
			case WEST  -> Arrays.asList(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH);
			case EAST  -> Arrays.asList(SHAPE_NORTH, SHAPE_SOUTH, SHAPE_WEST);
			default    -> Arrays.asList(SHAPE_SOUTH, SHAPE_EAST, SHAPE_WEST);
		};

		var shape = half == DoubleBlockHalf.UPPER ? SHAPE_TOP : SHAPE_BOTTOM;
		for (var next : merge) {
			shape = Shapes.join(shape, next, BooleanOp.OR);
		}

		return shape;
	}


	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
							   @Nonnull CollisionContext context)
	{
		var top = state.getValue(HALF) == DoubleBlockHalf.UPPER;
		return switch (state.getValue(FACING).getOpposite())
		{
			case SOUTH -> top ? SHAPE_TOP_SOUTH : SHAPE_BOTTOM_SOUTH;
			case WEST -> top ? SHAPE_TOP_WEST : SHAPE_BOTTOM_WEST;
			case EAST -> top ? SHAPE_TOP_EAST : SHAPE_BOTTOM_EAST;
			default -> top ? SHAPE_TOP_NORTH : SHAPE_BOTTOM_NORTH;
		};
	}

	public void toggleDoor(boolean open, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull Level level)
	{
		// Only takes in UPPER pos.
		var newState = state.setValue(OPEN, open);
		var lowerPos = pos.below();
		var update = Block.UPDATE_IMMEDIATE | Block.UPDATE_CLIENTS;

		level.setBlock(pos, newState, update);
		if (level.getBlockState(lowerPos).getBlock() == this)
		{
			level.setBlock(lowerPos, newState, update);
		}
	}

	private boolean canOpen(@Nonnull BlockPos pos, @Nonnull Level level)
	{
		// Only takes in UPPER pos, surgery is above it.
		if (level.getBlockEntity(pos.above()) instanceof TileEntitySurgery surgery)
		{
			return surgery.canOpen();
		}

		return true;
	}

	private void notifySurgeon(@Nonnull BlockPos pos, @Nonnull Level level)
	{
		// Only takes in UPPER pos, surgery is above it.
		if (level.getBlockEntity(pos.above()) instanceof TileEntitySurgery surgery)
		{
			surgery.notifyChange();
		}
	}
}
