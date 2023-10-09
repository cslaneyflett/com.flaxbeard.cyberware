package flaxbeard.cyberware.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

public class ItemExpCapsule extends Item
{
	public ItemExpCapsule(Properties pProperties)
	{
		super(pProperties);
	}

	@OnlyIn(Dist.CLIENT)
	public boolean hasEffect(ItemStack stack)
	{
		return true;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level pLevel, @Nonnull Player pPlayer, @Nonnull InteractionHand pUsedHand)
	{
		ItemStack stack = pPlayer.getItemInHand(pUsedHand);

		int xp = 0;
		CompoundTag tagCompound = stack.getTag();
		if (tagCompound != null
			&& tagCompound.contains("xp")
		)
		{
			xp = tagCompound.getInt("xp");
		}

		if (!pPlayer.isCreative())
		{
			stack.shrink(1);
		}

		pPlayer.giveExperiencePoints(xp);

		return super.use(pLevel, pPlayer, pUsedHand);
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
