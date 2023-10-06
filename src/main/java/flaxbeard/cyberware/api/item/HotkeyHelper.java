package flaxbeard.cyberware.api.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class HotkeyHelper
{
	public static void assignHotkey(ICyberwareUserData cyberwareUserData, ItemStack stack, int key)
	{
		removeHotkey(cyberwareUserData, stack);

		cyberwareUserData.addHotkey(key, stack);
		CyberwareAPI.getCyberwareNBT(stack).putInt("hotkey", key);
	}

	public static void removeHotkey(ICyberwareUserData cyberwareUserData, int key)
	{
		ItemStack stack = cyberwareUserData.getHotkey(key);
		removeHotkey(cyberwareUserData, stack);
	}

	public static void removeHotkey(ICyberwareUserData cyberwareUserData, ItemStack stack)
	{
		int hotkey = getHotkey(stack);

		if (hotkey != -1)
		{
			cyberwareUserData.removeHotkey(hotkey);
			CyberwareAPI.getCyberwareNBT(stack).remove("hotkey");
		}
	}

	public static int getHotkey(ItemStack stack)
	{
		if (stack.isEmpty()) return -1;

		CompoundTag tagCompound = CyberwareAPI.getCyberwareNBT(stack);
		if (!tagCompound.contains("hotkey"))
		{
			return -1;
		}

		return tagCompound.getInt("hotkey");
	}
}
