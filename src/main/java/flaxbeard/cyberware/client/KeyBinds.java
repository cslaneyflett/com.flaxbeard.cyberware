package flaxbeard.cyberware.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

public class KeyBinds
{
	public static KeyMapping menu;

	public static void register(RegisterKeyMappingsEvent event)
	{
		menu = new KeyMapping("cyberware.keybinds.menu", InputConstants.KEY_R, "cyberware.keybinds.category");
		event.register(menu);
	}
}
