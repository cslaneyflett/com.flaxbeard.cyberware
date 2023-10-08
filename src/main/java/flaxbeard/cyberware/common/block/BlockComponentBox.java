package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockComponentBox extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock
{
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public BlockItem itemBlock;

	public BlockComponentBox(Properties pProperties)
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
		return new TileEntityComponentBox(pPos, pState);
	}

	private static final VoxelShape eastWest = Shapes.create(
		new AABB(1F / 16F, 0F, 4F / 16F, 15F / 16F, 10F / 16F, 12F / 16F)
	);
	private static final VoxelShape northSouth = Shapes.create(
		new AABB(4F / 16F, 0F, 1F / 16F, 12F / 16F, 10F / 16F, 15F / 16F)
	);

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

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
								 @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit)
	{
		if (level.getBlockEntity(pos) instanceof TileEntityComponentBox blockEntity)
		{
			if (player.isShiftKeyDown())
			{
				ItemStack toDrop = this.getStack(blockEntity);
				Inventory inventory = player.getInventory();

				if (inventory.getSelected().isEmpty())
				{
					inventory.add(inventory.selected, toDrop);
				} else if (!inventory.add(toDrop))
				{
					var entity = new ItemEntity(
						level,
						pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D,
						toDrop
					);
					level.addFreshEntity(entity);
				}

				blockEntity.doDrop = false;
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			} else
			{
				// TODO
				// player.openGui(Cyberware.INSTANCE, 5, world, pos.getX(), pos.getY(), pos.getZ());
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.FAIL;
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Override
	public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean moving)
	{
		if (level.getBlockEntity(pos) instanceof TileEntityComponentBox blockEntity)
		{
			level.addFreshEntity(new ItemEntity(
				level, pos.getX(), pos.getY(), pos.getZ(),
				getStack(blockEntity)
			));
		}
	}

	private ItemStack getStack(TileEntityComponentBox box)
	{
		ItemStack stackToDrop = new ItemStack(itemBlock);

		CompoundTag tagCompound = new CompoundTag();
		tagCompound.put("contents", box.slots.serializeNBT());
		stackToDrop.setTag(tagCompound);

		if (box.hasCustomName())
		{
			stackToDrop.setHoverName(Component.literal(box.getName()));
		}

		return stackToDrop;
	}

	// TODO
	// onBlockPlacedBy -> blockEntity.setCustomInventoryName(stack.getDisplayName())
}
