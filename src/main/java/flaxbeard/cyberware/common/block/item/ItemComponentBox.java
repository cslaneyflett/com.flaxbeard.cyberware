package flaxbeard.cyberware.common.block.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemComponentBox extends ItemBlockCyberware
{
	public ItemComponentBox(Block block)
	{
		super(block, new Properties()
			.stacksTo(1)
		);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext useOnContext)
	{
		Player entityPlayer = useOnContext.getPlayer();
		assert entityPlayer != null;

		ItemStack itemStackIn = useOnContext.getItemInHand();
		// TODO: gui
		//		entityPlayer.openGui(Cyberware.INSTANCE, 6, worldIn, 0, 0, 0);
		return InteractionResult.PASS;
	}

	@Nonnull
	@Override
	public InteractionResult place(BlockPlaceContext placeContext)
	{
		Player entityPlayer = placeContext.getPlayer();
		assert entityPlayer != null;

		if (entityPlayer.isShiftKeyDown())
		{
			InteractionResult res = super.place(placeContext);
			if (res == InteractionResult.SUCCESS && entityPlayer.isCreative())
			{
				entityPlayer.getInventory().removeItem(placeContext.getItemInHand());
			}
			return res;
		} else
		{
			//			entityPlayer.openGui(Cyberware.INSTANCE, 6, worldIn, 0, 0, 0);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag advanced)
	{
		tooltip.add(Component.literal(ChatFormatting.GRAY + I18n.get("cyberware.tooltip.component_box")));
		tooltip.add(Component.literal(ChatFormatting.GRAY + I18n.get("cyberware.tooltip.component_box2")));
	}
}
