package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityBeaconLarge;
import flaxbeard.cyberware.common.registry.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBeaconLarge extends HorizontalDirectionalBlock implements EntityBlock
{
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BlockBeaconLarge(Properties pProperties)
	{
		super(pProperties);

		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(WATERLOGGED, false)
		);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState)
	{
		return new TileEntityBeaconLarge(pPos, pState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state,
																  @Nonnull BlockEntityType<T> type)
	{
		return type == BlockEntities.BEACON_LARGE.get() ? TileEntityBeaconLarge::tick : null;
	}

	private static final VoxelShape eastWest = Shapes.create(
		new AABB(5F / 16F, 0F, 3F / 16F, 11F / 16F, 1F, 13F / 16F)
	);
	private static final VoxelShape northSouth = Shapes.create(
		new AABB(3F / 16F, 0F, 5F / 16F, 13F / 16F, 1F, 11F / 16F)
	);
	// private static final AABB middle = new AABB(6.5F / 16F, 0F, 6.5F / 16F, 9.5F / 16F, 1F, 9.5F / 16F);

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
							   @Nonnull CollisionContext context)
	{
		Direction facing = state.getValue(FACING);
		return facing == Direction.NORTH || facing == Direction.SOUTH
			? eastWest
			: northSouth;
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
