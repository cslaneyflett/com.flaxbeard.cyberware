package flaxbeard.cyberware.api;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface ISpecialBattery
{
	int add(@Nonnull ItemStack battery, @Nonnull ItemStack power, int amount, boolean simulate);

	int extract(@Nonnull ItemStack battery, int amount, boolean simulate);

	int getStoredEnergy(@Nonnull ItemStack battery);

	int getPowerCapacity(@Nonnull ItemStack battery);
}
