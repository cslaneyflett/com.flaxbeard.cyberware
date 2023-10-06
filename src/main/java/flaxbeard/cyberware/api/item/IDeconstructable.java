package flaxbeard.cyberware.api.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IDeconstructable
{
	public boolean canDestroy(ItemStack stack);

	public NonNullList<ItemStack> getComponents(ItemStack stack);
}
