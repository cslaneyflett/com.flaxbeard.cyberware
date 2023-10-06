package flaxbeard.cyberware.api.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class EnableDisableHelper
{
	public static final String ENABLED_STR = "~enabled";

	public static boolean isEnabled(ItemStack stack)
	{
		if (stack.isEmpty()) return false;

		CompoundTag tagCompound = CyberwareAPI.getCyberwareNBT(stack);
		if (!tagCompound.contains(ENABLED_STR))
		{
			return true;
		}

		return tagCompound.getBoolean(ENABLED_STR);
	}

	public static void toggle(ItemStack stack)
	{
		CompoundTag tagCompound = CyberwareAPI.getCyberwareNBT(stack);
		if (isEnabled(stack))
		{
			tagCompound.putBoolean(ENABLED_STR, false);
		} else
		{
			tagCompound.remove(ENABLED_STR);
		}
	}

	public static String getUnlocalizedLabel(ItemStack stack)
	{
		if (isEnabled(stack))
		{
			return "cyberware.gui.active.disable";
		} else
		{
			return "cyberware.gui.active.enable";
		}
	}
}
