package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ISpecialBattery;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemDenseBattery extends ItemCyberware implements ISpecialBattery
{
	public ItemDenseBattery(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, EnumSlot.LOWER_ORGANS);
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return other.getItem() == CyberwareContent.lowerOrgansUpgrades
			&& CyberwareItemMetadata.get(stack) == ItemLowerOrgansUpgrade.META_BATTERY;
	}

	@Override
	public int add(ItemStack battery, ItemStack power, int amount, boolean simulate)
	{
		if (power == ItemStack.EMPTY)
		{
			int amountToAdd = Math.min(getCapacity(battery) - getStoredEnergy(battery), amount);
			if (!simulate)
			{
				CompoundTag data = CyberwareAPI.getCyberwareNBT(battery);
				data.putInt("power", data.getInt("power") + amountToAdd);
			}
			return amountToAdd;
		}
		return 0;
	}

	@Override
	public int extract(ItemStack battery, int amount, boolean simulate)
	{
		int amountToSub = Math.min(getStoredEnergy(battery), amount);
		if (!simulate)
		{
			CompoundTag data = CyberwareAPI.getCyberwareNBT(battery);
			data.putInt("power", data.getInt("power") - amountToSub);
		}
		return amountToSub;
	}

	@Override
	public int getStoredEnergy(ItemStack battery)
	{
		CompoundTag data = CyberwareAPI.getCyberwareNBT(battery);

		if (!data.contains("power"))
		{
			data.putInt("power", 0);
		}
		return data.getInt("power");
	}

	@Override
	public int getCapacity(ItemStack battery)
	{
		return LibConstants.DENSE_BATTERY_CAPACITY;
	}
}
