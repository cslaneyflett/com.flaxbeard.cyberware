package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemNeuropozyne extends Item
{
	public ItemNeuropozyne(String name)
	{
		super();

		setRegistryName(name);
		// ForgeRegistries.ITEMS.register(this);
		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);

		setMaxDamage(0);

		CyberwareContent.items.add(this);
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player entityPlayer,
															   @Nonnull EnumHand hand)
	{
		ItemStack stack = entityPlayer.getHeldItem(hand);

		if (!entityPlayer.capabilities.isCreativeMode)
		{
			stack.shrink(1);
		}

		entityPlayer.addEffect(new MobEffect(CyberwareContent.neuropozyneEffect, 24000, 0, false, false));

		return new InteractionResultHolder<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		String neuropozyne = I18n.get("cyberware.tooltip.neuropozyne");

		tooltip.add(Component.literal(ChatFormatting.BLUE + neuropozyne));
	}
}
