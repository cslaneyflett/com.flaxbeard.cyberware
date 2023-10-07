package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityBeacon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class BlockBeacon extends Block implements EntityBlock, SimpleWaterloggedBlock
{
	private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BlockBeacon(Properties pProperties)
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
		return new TileEntityBeacon(pPos, pState);
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor)
	{
		super.onNeighborChange(state, level, pos, neighbor);

		if (pos.relative(Direction.DOWN) == neighbor &&
			!level.getBlockState(neighbor).isCollisionShapeFullBlock(level, neighbor))
		{
			Level fullLevel = (Level) level;
			BlockBeacon.dropResources(state, fullLevel, pos);
			this.destroy(fullLevel, pos, state);
		}
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

		return Objects.requireNonNull(super.getStateForPlacement(context))
			.setValue(FACING, context.getHorizontalDirection().getOpposite())
			.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}
}
