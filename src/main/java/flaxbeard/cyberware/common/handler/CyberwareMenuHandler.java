package flaxbeard.cyberware.common.handler;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.client.KeyBinds;
import flaxbeard.cyberware.client.gui.GuiCyberwareMenu;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.OpenRadialMenuPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class CyberwareMenuHandler
{
	public static final CyberwareMenuHandler INSTANCE = new CyberwareMenuHandler();
	private Minecraft mc = Minecraft.getInstance();
	int wasInScreen = 0;
	public static boolean wasSprinting = false;
	private static List<Integer> lastPressed = new ArrayList<>();
	private static List<Integer> pressed = new ArrayList<>();

	@SubscribeEvent
	public void tick(ClientTickEvent event)
	{
		if (event.phase == Phase.START)
		{
			if (!KeyBinds.menu.isPressed()
				&& mc.currentScreen == null
				&& wasInScreen > 0)
			{
				KeyConflictContext inGame = KeyConflictContext.IN_GAME;
				mc.gameSettings.keyBindForward.setKeyConflictContext(inGame);
				mc.gameSettings.keyBindLeft.setKeyConflictContext(inGame);
				mc.gameSettings.keyBindBack.setKeyConflictContext(inGame);
				mc.gameSettings.keyBindRight.setKeyConflictContext(inGame);
				mc.gameSettings.keyBindJump.setKeyConflictContext(inGame);
				mc.gameSettings.keyBindSneak.setKeyConflictContext(inGame);
				mc.gameSettings.keyBindSprint.setKeyConflictContext(inGame);

				if (wasSprinting)
				{
					mc.player.setSprinting(wasSprinting);
				}
				wasInScreen--;
			}
		}
		if (event.phase == Phase.END)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(mc.player);
			if (mc.player != null
				&& mc.currentScreen == null
				&& cyberwareUserData != null)
			{
				for (int keyCode : cyberwareUserData.getHotkeys())
				{
					if (isPressed(cyberwareUserData, keyCode))
					{
						pressed.add(keyCode);
						if (!lastPressed.contains(keyCode))
						{
							ClientUtils.useActiveItemClient(mc.player, cyberwareUserData.getHotkey(keyCode));
						}
					}
				}

				lastPressed = pressed;
				pressed = new ArrayList<>();
			}

			if (mc.player != null
				&& cyberwareUserData.getNumActiveItems() > 0
				&& KeyBinds.menu.isPressed()
				&& mc.currentScreen == null)
			{
				KeyConflictContext gui = KeyConflictContext.GUI;
				mc.gameSettings.keyBindForward.setKeyConflictContext(gui);
				mc.gameSettings.keyBindLeft.setKeyConflictContext(gui);
				mc.gameSettings.keyBindBack.setKeyConflictContext(gui);
				mc.gameSettings.keyBindRight.setKeyConflictContext(gui);
				mc.gameSettings.keyBindJump.setKeyConflictContext(gui);
				mc.gameSettings.keyBindSneak.setKeyConflictContext(gui);
				mc.gameSettings.keyBindSprint.setKeyConflictContext(gui);

				mc.displayGuiScreen(new GuiCyberwareMenu());
				cyberwareUserData.setOpenedRadialMenu(true);
				CyberwarePacketHandler.INSTANCE.sendToServer(new OpenRadialMenuPacket());

				wasInScreen = 5;
			} else if (wasInScreen > 0
				&& mc.currentScreen instanceof GuiCyberwareMenu)
			{
				wasSprinting = mc.player.isSprinting();
			}
		}
	}

	private boolean isPressed(ICyberwareUserData cyberwareUserData, int keyCode)
	{
		if (keyCode < 0)
		{
			keyCode = keyCode + 100;
			return Mouse.isButtonDown(keyCode);
		} else if (keyCode > 900)
		{
			boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

			keyCode = keyCode - 900;
			return Keyboard.isKeyDown(keyCode) && shiftPressed;
		} else
		{
			if (cyberwareUserData.getHotkey(keyCode + 900) != null)
			{
				boolean shiftPressed =
					Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

				return Keyboard.isKeyDown(keyCode) && !shiftPressed;
			}
			return Keyboard.isKeyDown(keyCode);
		}
	}
}
