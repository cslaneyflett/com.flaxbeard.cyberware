package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ISpecialBattery;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemCreativeBattery extends ItemCyberware implements ISpecialBattery
{
	public ItemCreativeBattery(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.LOWER_ORGANS);
	}

	@Override
	public int add(@Nonnull ItemStack battery, @Nonnull ItemStack power, int amount, boolean simulate)
	{
		return amount;
	}

	@Override
	public int extract(@Nonnull ItemStack battery, int amount, boolean simulate)
	{
		return amount;
	}

	@Override
	public int getStoredEnergy(@Nonnull ItemStack battery)
	{
		return 999999;
	}

	@Override
	public int getPowerCapacity(@Nonnull ItemStack battery)
	{
		return 999999;
	}

	@Override
	public boolean canHoldQuality(@Nonnull ItemStack stack, @Nonnull Quality quality)
	{
		return quality == CyberwareAPI.QUALITY_MANUFACTURED;
	}
}
