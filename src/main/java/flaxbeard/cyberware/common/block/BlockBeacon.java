package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityBeacon;
import flaxbeard.cyberware.common.registry.CWBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBeacon extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock
{
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BlockBeacon(Properties pProperties)
	{
		super(pProperties);

		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(WATERLOGGED, false)
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder
			.add(FACING)
			.add(WATERLOGGED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState)
	{
		return new TileEntityBeacon(pPos, pState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state,
																  @Nonnull BlockEntityType<T> type)
	{
		return type == CWBlockEntities.BEACON.get() ? TileEntityBeacon::tick : null;
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Override
	public boolean canSurvive(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull BlockPos pos)
	{
		var below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	private static final VoxelShape SHAPE = Shapes.create(
		new AABB(1F / 16F, 0F, 1F / 16F, 15F / 16F, 4F / 16F, 15F / 16F)
	);

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
							   @Nonnull CollisionContext context)
	{
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
	{
		var fluidState = context.getLevel().getFluidState(context.getClickedPos());

		return this.defaultBlockState()
			.setValue(FACING, context.getHorizontalDirection().getOpposite())
			.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}
}
