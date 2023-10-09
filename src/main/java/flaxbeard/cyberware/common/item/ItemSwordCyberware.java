package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.registry.items.Components;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

import javax.annotation.Nonnull;

public class ItemSwordCyberware extends SwordItem implements IDeconstructable
{
	public ItemSwordCyberware(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties)
	{
		super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
	}

	@Override
	public boolean canDestroy(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getComponents(@Nonnull ItemStack stack)
	{
		return NNLUtil.fromArray
			(new ItemStack[]{
				new ItemStack(Items.IRON_INGOT, 2),
				new ItemStack(Components.TITANIUM.get(), 1),
				new ItemStack(Components.PLATING.get(), 1)
			});
	}
}
