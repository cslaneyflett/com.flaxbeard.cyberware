package flaxbeard.cyberware.api.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface IDeconstructable
{
	boolean canDestroy(@Nonnull ItemStack stack);

	@Nonnull
	NonNullList<ItemStack> getComponents(@Nonnull ItemStack stack);
}
