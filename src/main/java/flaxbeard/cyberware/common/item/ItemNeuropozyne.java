package flaxbeard.cyberware.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemNeuropozyne extends PotionItem
{
	public @Nonnull ItemStack getDefaultInstance() {
		return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
	}

	public ItemNeuropozyne(String name)
	{
		super(new Properties());
//
//		setRegistryName(name);
//		// ForgeRegistries.ITEMS.register(this);
//		setTranslationKey(Cyberware.MODID + "." + name);
//
//		setCreativeTab(Cyberware.creativeTab);
//
//		setMaxDamage(0);
//
//		CyberwareContent.items.add(this);
	}
//
//	@Nonnull
//	@Override
//	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player entityPlayer,
//															   @Nonnull EnumHand hand)
//	{
//		ItemStack stack = entityPlayer.getHeldItem(hand);
//
//		if (!entityPlayer.capabilities.isCreativeMode)
//		{
//			stack.shrink(1);
//		}
//
//		entityPlayer.addEffect(new MobEffectInstance(CyberwareContent.neuropozyneEffect, 24000, 0, false, false));
//
//		return new InteractionResultHolder<>(EnumActionResult.SUCCESS, stack);
//	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		String neuropozyne = I18n.get("cyberware.tooltip.neuropozyne");

		tooltip.add(Component.literal(ChatFormatting.BLUE + neuropozyne));
	}
}
