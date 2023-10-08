package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemExpCapsule extends Item
{
	public ItemExpCapsule(Properties pProperties)
	{
		super(pProperties);
	}

	@Override
	public void getSubItems(@Nonnull CreativeModeTab tab, @Nonnull NonNullList<ItemStack> list)
	{
		if (this.getCreativeTabs().contains(tab))
		{
			ItemStack stack = new ItemStack(this);
			CompoundTag tagCompound = new CompoundTag();
			tagCompound.putInt("xp", 100);
			stack.setTag(tagCompound);
			list.add(stack);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public boolean hasEffect(ItemStack stack)
	{
		return true;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player entityPlayer,
															   @Nonnull InteractionHand hand)
	{
		ItemStack stack = entityPlayer.getItemInHand(hand);

		int xp = 0;
		CompoundTag tagCompound = stack.getTag();
		if (tagCompound != null
			&& tagCompound.contains("xp")
		)
		{
			xp = tagCompound.getInt("xp");
		}

		if (!entityPlayer.isCreative())
		{
			stack.shrink(1);
		}

		entityPlayer.giveExperiencePoints(xp);

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		int xp = 0;
		CompoundTag tagCompound = stack.getTag();
		if (tagCompound != null
			&& tagCompound.contains("xp"))
		{
			xp = tagCompound.getInt("xp");
		}
		String before = I18n.get("cyberware.tooltip.exp_capsule.before");
		if (!before.isEmpty()) before += " ";

		String after = I18n.get("cyberware.tooltip.exp_capsule.after");
		if (!after.isEmpty()) after = " " + after;

		tooltip.add(Component.literal(ChatFormatting.RED + before + xp + after));
	}
}
