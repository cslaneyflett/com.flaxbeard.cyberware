package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ISpecialBattery;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.items.LowerOrgans;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemDenseBattery extends ItemCyberware implements ISpecialBattery
{
	public ItemDenseBattery(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.LOWER_ORGANS);
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return CyberwareAPI.getCyberware(other).getSlot(other) == BodyRegionEnum.LOWER_ORGANS
			&& stack.is(LowerOrgans.BATTERY.get());
	}

	@Override
	public int add(@Nonnull ItemStack battery, @Nonnull ItemStack power, int amount, boolean simulate)
	{
		if (power == ItemStack.EMPTY)
		{
			int amountToAdd = Math.min(getPowerCapacity(battery) - getStoredEnergy(battery), amount);
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
	public int extract(@Nonnull ItemStack battery, int amount, boolean simulate)
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
	public int getStoredEnergy(@Nonnull ItemStack battery)
	{
		CompoundTag data = CyberwareAPI.getCyberwareNBT(battery);

		if (!data.contains("power"))
		{
			data.putInt("power", 0);
		}
		return data.getInt("power");
	}

	@Override
	public int getPowerCapacity(@Nonnull ItemStack battery)
	{
		return LibConstants.DENSE_BATTERY_CAPACITY;
	}
}
