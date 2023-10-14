package flaxbeard.cyberware.common.handler;

import com.mojang.blaze3d.platform.InputConstants;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.client.KeyBinds;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.OpenRadialMenuPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CyberwareMenuHandler
{
	public static final CyberwareMenuHandler INSTANCE = new CyberwareMenuHandler();
	private Minecraft mc = Minecraft.getInstance();
	int wasInScreen = 0;
	public static boolean wasSprinting = false;
	private static List<Integer> lastPressed = new ArrayList<>();
	private static List<Integer> pressed = new ArrayList<>();

	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			if (!KeyBinds.menu.isDown()
				&& mc.screen == null
				&& wasInScreen > 0)
			{
				KeyConflictContext inGame = KeyConflictContext.IN_GAME;
				mc.options.keyUp.setKeyConflictContext(inGame);
				mc.options.keyLeft.setKeyConflictContext(inGame);
				mc.options.keyDown.setKeyConflictContext(inGame);
				mc.options.keyRight.setKeyConflictContext(inGame);
				mc.options.keyJump.setKeyConflictContext(inGame);
				mc.options.keyShift.setKeyConflictContext(inGame);
				mc.options.keySprint.setKeyConflictContext(inGame);

				if (wasSprinting)
				{
					mc.player.setSprinting(wasSprinting);
				}
				wasInScreen--;
			}
		}
		if (event.phase == TickEvent.Phase.END)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(mc.player);
			if (mc.player != null
				&& mc.screen == null
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
				&& Objects.requireNonNull(cyberwareUserData).getNumActiveItems() > 0
				&& KeyBinds.menu.isDown()
				&& mc.screen == null)
			{
				KeyConflictContext gui = KeyConflictContext.GUI;
				mc.options.keyUp.setKeyConflictContext(gui);
				mc.options.keyLeft.setKeyConflictContext(gui);
				mc.options.keyDown.setKeyConflictContext(gui);
				mc.options.keyRight.setKeyConflictContext(gui);
				mc.options.keyJump.setKeyConflictContext(gui);
				mc.options.keyShift.setKeyConflictContext(gui);
				mc.options.keySprint.setKeyConflictContext(gui);

				// TODO
				//				mc.setScreen(new GuiCyberwareMenu());
				cyberwareUserData.setOpenedRadialMenu(true);
				CyberwarePacketHandler.INSTANCE.sendToServer(new OpenRadialMenuPacket());

				wasInScreen = 5;
				// TODO
			} else if (wasInScreen > 0 && false)
			//				&& mc.screen instanceof GuiCyberwareMenu)
			{
				wasSprinting = mc.player.isSprinting();
			}
		}
	}

	private boolean isPressed(ICyberwareUserData cyberwareUserData, int keyCode)
	{
		var window = Minecraft.getInstance().getWindow().getWindow();

		if (keyCode < 0)
		{
			keyCode = keyCode + 100;
			return InputConstants.isKeyDown(window, keyCode);
		} else if (keyCode > 900)
		{
			boolean shiftPressed = InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);

			keyCode = keyCode - 900;
			return InputConstants.isKeyDown(window, keyCode) && shiftPressed;
		} else
		{
			if (cyberwareUserData.getHotkey(keyCode + 900) != null)
			{
				boolean shiftPressed =
					InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);

				return InputConstants.isKeyDown(window, keyCode) && !shiftPressed;
			}
			return InputConstants.isKeyDown(window, keyCode);
		}
	}
}
