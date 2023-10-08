package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ISpecialBattery;
import flaxbeard.cyberware.api.item.ICyberware;
import net.minecraft.world.item.ItemStack;

public class ItemCreativeBattery extends ItemCyberware implements ISpecialBattery
{
	public ItemCreativeBattery(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, EnumSlot.LOWER_ORGANS);
	}

	@Override
	public int add(ItemStack battery, ItemStack power, int amount, boolean simulate)
	{
		return amount;
	}

	@Override
	public int extract(ItemStack battery, int amount, boolean simulate)
	{
		return amount;
	}

	@Override
	public int getStoredEnergy(ItemStack battery)
	{
		return 999999;
	}

	@Override
	public int getCapacity(ItemStack battery)
	{
		return 999999;
	}

	@Override
	public boolean canHoldQuality(ItemStack stack, Quality quality)
	{
		return quality == CyberwareAPI.QUALITY_MANUFACTURED;
	}
}
