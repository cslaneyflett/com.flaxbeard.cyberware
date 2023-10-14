package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityScanner;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.registry.CWBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockScanner extends Block implements EntityBlock
{
	public BlockScanner(Properties pProperties)
	{
		super(pProperties);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState)
	{
		return new TileEntityScanner(pPos, pState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state,
																  @Nonnull BlockEntityType<T> type)
	{
		return type == CWBlockEntities.SCANNER.get() ? TileEntityScanner::tick : null;
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nonnull
	@Override
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
								 @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit)
	{
		if (level.getBlockEntity(pos) instanceof TileEntityScanner blockEntity)
		{
			if (player.isCreative() &&
				player.isShiftKeyDown())
			{
				blockEntity.ticks = CyberwareConfig.INSTANCE.SCANNER_TIME.get() - 200;
			}

			if (!level.isClientSide)
			{
				NetworkHooks.openScreen((ServerPlayer) player, state.getMenuProvider(level, pos));
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		return InteractionResult.FAIL;
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Nullable
	@Override
	public MenuProvider getMenuProvider(@Nonnull BlockState pState, @Nonnull Level pLevel, @Nonnull BlockPos pPos)
	{
		// TODO
		return super.getMenuProvider(pState, pLevel, pPos);
	}

	@SuppressWarnings("deprecation") // Only deprecated for call, not override.
	@Override
	public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean moving)
	{
		if (level.getBlockEntity(pos) instanceof TileEntityScanner blockEntity)
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
