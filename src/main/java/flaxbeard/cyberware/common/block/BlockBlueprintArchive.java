package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityBeaconLarge;
import flaxbeard.cyberware.common.block.tile.TileEntityBlueprintArchive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBlueprintArchive extends HorizontalDirectionalBlock implements EntityBlock
{
	public BlockBlueprintArchive(Properties pProperties)
	{
		super(pProperties);

		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder
			.add(FACING);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState)
	{
		return new TileEntityBeaconLarge(pPos, pState);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
	{
		return this.defaultBlockState()
			.setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
								 @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit)
	{
		if (level.getBlockEntity(pos) instanceof TileEntityBlueprintArchive blockEntity)
		{
			// TODO
			// entityPlayer.openGui(Cyberware.INSTANCE, 4, world, pos.getX(), pos.getY(), pos.getZ())
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.FAIL;
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Override
	public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean moving)
	{
		if (level.getBlockEntity(pos) instanceof TileEntityBlueprintArchive blockEntity)
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
	}

	// TODO
	// onBlockPlacedBy -> blockEntity.setCustomInventoryName(stack.getDisplayName())
}
