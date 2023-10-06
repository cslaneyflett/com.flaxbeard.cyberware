package flaxbeard.cyberware.client.gui;

import com.google.common.collect.ImmutableSet;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.HotkeyHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.client.KeyBinds;
import flaxbeard.cyberware.common.handler.HudHandler;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.SyncHotkeyPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Collections;

public class GuiCyberwareMenu extends GuiScreen
{
	Minecraft mc = Minecraft.getInstance();
	boolean movedWheel = false;
	int selectedPart = -1;
	int lastMousedOverPart = -1;
	private boolean editing = false;
	private boolean color = false;
	private float radiusBase = 100F;
	private float innerRadiusBase = 40F;
	private boolean close = false;
	private GuiTextField hex;
	private float[][] colorOptions = new float[][]{
		new float[]{0.0F, 1.0F, 1.0F},
		new float[]{76F / 255F, 1.0F, 0.0F},
		new float[]{1.0F, 216F / 255F, 0.0F},
		new float[]{1.0F, 182F / 255F, 66F / 255F},
		new float[]{212F / 255F, 119F / 255F, 1.0F},
		new float[]{1.0F, 0.0F, 0.0F},
		new float[]{61F / 255F, 174F / 255F, 1.0F},
		new float[]{1.0F, 89F / 255F, 232F / 255F},
		new float[]{28F / 255F, 1.0F, 156F / 255F},
		new float[]{1.0F, 1.0F, 1.0F}
	};
	private static final int ROW_SIZE = 5;

	public GuiCyberwareMenu()
	{
		Keyboard.enableRepeatEvents(true);
	}

	@Override
	public void initGui()
	{
		super.initGui();
		int numRows = ((colorOptions.length + ROW_SIZE - 1) / ROW_SIZE);
		hex = new GuiTextField(2, fontRenderer, width / 2 - 70, height / 2 - 100 + (30 * numRows), 140, 20);
		String s = Integer.toHexString(CyberwareAPI.getHUDColorHex()).toUpperCase();
		while (s.length() < 6)
		{
			s = "0" + s;
		}
		hex.setText(s);
		hex.setEnabled(false);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);

		int d = Mouse.getDWheel();
		if (!movedWheel
			&& d != 0
			&& !editing
			&& !color)
		{
			movedWheel = true;
			if (selectedPart == -1
				&& d > 0)
			{
				d = 0;
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate((width / 2F) + radiusBase, (height / 2F) - radiusBase, 0F);
		GlStateManager.scale(2F, 2F, 2F);
		float[] mainColor = CyberwareAPI.getHUDColor();
		GlStateManager.color(mainColor[0], mainColor[1], mainColor[2]);
		Minecraft.getInstance().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);
		ClientUtils.drawTexturedModalRect(1, 0, 81, 10, 7, 8);
		ClientUtils.drawTexturedModalRect(0, 9, 88, 10, 8, 8);
		ClientUtils.drawTexturedModalRect(0, 18, 97, 10, 8, 8);

		GlStateManager.color(1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();

		if (color)
		{
			hex.drawTextBox();
			for (int n = 0; n < colorOptions.length; n++)
			{
				float[] colorOption = colorOptions[n];
				GlStateManager.color(colorOption[0], colorOption[1], colorOption[2]);

				int row = n / ROW_SIZE;
				int col = n % ROW_SIZE;

				int xOffset = -10 + (col - (ROW_SIZE / 2)) * 30;
				int yOffset = -100 + 30 * row;
				Minecraft.getInstance().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);
				ClientUtils.drawTexturedModalRect(width / 2 + xOffset, height / 2 + yOffset, 0, 236, 20, 20);
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F);
		} else
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(mc.player);
			if (cyberwareUserData == null) return;

			GlStateManager.pushMatrix();
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			int centerX = width / 2;
			int centerY = height / 2;

			int piePieces = cyberwareUserData.getNumActiveItems();

			if (movedWheel
				&& !editing)
			{
				selectedPart = (selectedPart - Integer.signum(d));
				while (selectedPart < 0)
				{
					selectedPart += piePieces;
				}
				selectedPart = selectedPart % piePieces;
			}

			float degreesPerPiece = 360F / piePieces;

			int maxStepsPerTrial = 5;

			float mouseDist =
				(float) Math.sqrt((centerX - mouseX) * (centerX - mouseX) + (centerY - mouseY) * (centerY - mouseY));
			float mouseAngle =
				(float) ((Math.atan2(mouseY - centerY, mouseX - centerX) * 180F / Math.PI) + 360F) % 360F;

			for (int piece = 0; piece < piePieces; piece++)
			{

				float rotation = (degreesPerPiece * piece + 270) % 360;

				if (!editing)
				{
					if (mouseDist > innerRadiusBase)
					{
						movedWheel = false;
						if (piePieces == 1
							|| (mouseAngle > rotation
							&& mouseAngle < rotation + degreesPerPiece
							&& lastMousedOverPart != piece))
						{
							lastMousedOverPart = piece;
							selectedPart = piece;
						}
					} else
					{
						lastMousedOverPart = -1;
						if (!movedWheel)
						{
							selectedPart = -1;
						}
					}
				}

				boolean selected = piece == selectedPart;

				for (int deg = (int) (degreesPerPiece + .5F); deg > 0; deg -= maxStepsPerTrial)
				{
					float radius = radiusBase + (selected ? 10 : 0);
					float innerRadius = innerRadiusBase + (selected ? 10 : 0);

					int stepsPerTrial = Math.min(maxStepsPerTrial, deg);
					GL11.glBegin(GL11.GL_TRIANGLE_FAN);

					float alpha = selected ? 0.8F : 0.5F;

					ItemStack stack = cyberwareUserData.getActiveItems().get(piece);
					float[] itemColor = ((IMenuItem) stack.getItem()).getColor(stack);
					float[] color = (itemColor == null) ? mainColor : itemColor;
					GlStateManager.color(color[0], color[1], color[2], alpha);

					double radians = ((rotation + deg) / 180F) * Math.PI;

					float xS = centerX + ((float) Math.cos(radians) * innerRadius);
					float yS = centerY + ((float) Math.sin(radians) * innerRadius);
					GL11.glVertex2f(xS, yS);

					for (int i = 0; i <= stepsPerTrial; i++)
					{
						radians = ((rotation + deg - i) / 180F) * Math.PI;
						float x = centerX + ((float) Math.cos(radians) * radius);
						float y = centerY + ((float) Math.sin(radians) * radius);
						GL11.glVertex2f(x, y);
					}

					radians = ((rotation + deg - stepsPerTrial) / 180F) * Math.PI;

					xS = centerX + ((float) Math.cos(radians) * innerRadius);
					yS = centerY + ((float) Math.sin(radians) * innerRadius);
					GL11.glVertex2f(xS, yS);

					GL11.glEnd();
				}
			}
			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableTexture2D();
			GlStateManager.popMatrix();

			float scale = piePieces > 8F ? (piePieces > 16F ? .5F : 1F) : 2F;
			boolean unicode = fontRenderer.getUnicodeFlag();
			if (scale < 1.0F)
			{
				fontRenderer.setUnicodeFlag(true);
			}
			float itemRadiusBase = innerRadiusBase + (radiusBase - innerRadiusBase) / 2F;

			for (int piece = 0; piece < piePieces; piece++)
			{
				ItemStack stack = cyberwareUserData.getActiveItems().get(piece);

				float itemRadius = (piece == selectedPart ? itemRadiusBase + 10F : itemRadiusBase);
				float rotation = (degreesPerPiece * (piece + .5F) + 270) % 360;
				double radians = ((rotation) / 180F) * Math.PI;
				float offset = (16 / 2F) * scale;
				int boundKey = HotkeyHelper.getHotkey(stack);
				float yOffset = boundKey == -1 ? (fontRenderer.FONT_HEIGHT / 2F) : 0;
				float xS = centerX + ((float) Math.cos(radians) * itemRadius);
				float yS = centerY + ((float) Math.sin(radians) * itemRadius);
				GlStateManager.pushMatrix();
				GlStateManager.translate(xS - offset, yS - offset + yOffset, 0);
				GlStateManager.scale(scale, scale, scale);
				itemRender.renderItemIntoGUI(cyberwareUserData.getActiveItems().get(piece), 0, 0);
				GlStateManager.popMatrix();

				if (piece == selectedPart
					&& editing)
				{
					if ((mc.player.tickCount / 4) % 2 == 0)
					{
						GlStateManager.pushMatrix();
						String str = "__";
						int i = fontRenderer.getStringWidth(str);

						GlStateManager.translate(xS - i / 2F, yS + offset, 0);
						fontRenderer.drawStringWithShadow(str, 0, 0, 0xFFFFFF);
						GlStateManager.popMatrix();
					}
				} else if (boundKey != -1)
				{
					GlStateManager.pushMatrix();

					String str;
					if (boundKey < 0)
					{
						boundKey = boundKey + 100;
						str = Mouse.getButtonName(boundKey);
					} else if (boundKey > 900)
					{
						str = "SHIFT + " + Keyboard.getKeyName(boundKey - 900);
					} else
					{
						str = Keyboard.getKeyName(boundKey);
					}
					int i = fontRenderer.getStringWidth(str);

					GlStateManager.translate(xS - i / 2F, yS + offset, 0);
					fontRenderer.drawStringWithShadow(str, 0, 0, 0xFFFFFF);
					GlStateManager.popMatrix();
				}

				GlStateManager.pushMatrix();
				String str = I18n.get(((IMenuItem) stack.getItem()).getUnlocalizedLabel(stack));
				int i = fontRenderer.getStringWidth(str);

				GlStateManager.translate(xS - i / 2F, yS - offset + yOffset - fontRenderer.FONT_HEIGHT, 0);
				fontRenderer.drawStringWithShadow(str, 0, 0, 0xFFFFFF);
				GlStateManager.popMatrix();
			}

			fontRenderer.setUnicodeFlag(unicode);

			if (selectedPart >= 0)
			{
				GlStateManager.pushMatrix();
				String str = cyberwareUserData.getActiveItems().get(selectedPart).getDisplayName();
				GlStateManager.translate(((width - fontRenderer.getStringWidth(str)) / 2F),
					(height / 2F) - 30 - radiusBase, 0F
				);
				fontRenderer.drawStringWithShadow(str, 0, 0, 0xFFFFFF);
				GlStateManager.popMatrix();
			}
		}

		GlStateManager.pushMatrix();

		int sx = (int) ((width / 2F) + radiusBase);
		int sy = (int) ((height / 2F) - radiusBase);

		if (isPointInRegion(sx + 1 * 2, sy, 6 * 2, 8 * 2, mouseX, mouseY))
		{
			drawHoveringText(Arrays.asList(
				I18n.get("cyberware.gui.keybind.0"),
				I18n.get("cyberware.gui.keybind.1"),
				I18n.get("cyberware.gui.keybind.2"),
				I18n.get("cyberware.gui.keybind.3"),
				I18n.get("cyberware.gui.keybind.4")
			), mouseX, mouseY, fontRenderer);
		} else if (isPointInRegion(sx, sy + 9 * 2, 8 * 2, 8 * 2, mouseX, mouseY))
		{
			drawHoveringText(Collections.singletonList(I18n.get("cyberware.gui.open_hud_config")), mouseX, mouseY,
				fontRenderer
			);
		} else if (isPointInRegion(sx, sy + 18 * 2, 8 * 2, 8 * 2, mouseX, mouseY))
		{
			drawHoveringText(Collections.singletonList(I18n.get("cyberware.gui.open_color_change")), mouseX, mouseY,
				fontRenderer
			);
		}

		GlStateManager.popMatrix();
	}

	protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
	{
		return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1
			&& pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	{
		hex.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 0
			&& !editing)
		{
			int sx = (int) ((width / 2F) + radiusBase);
			int sy = (int) ((height / 2F) - radiusBase);
			if (isPointInRegion(sx, sy + 9 * 2, 8 * 2, 8 * 2, mouseX, mouseY))
			{
				mc.displayGuiScreen(new GuiHudConfiguration());
				return;
			}
			if (isPointInRegion(sx, sy + 18 * 2, 8 * 2, 8 * 2, mouseX, mouseY))
			{
				color = !color;
				hex.setEnabled(color);
				return;
			}
			if (color)
			{
				for (int n = 0; n < colorOptions.length; n++)
				{
					float[] colorOption = colorOptions[n];

					int row = n / ROW_SIZE;
					int col = n % ROW_SIZE;

					int xOffset = -10 + (col - (ROW_SIZE / 2)) * 30;
					int yOffset = -100 + 30 * row;
					if (isPointInRegion(width / 2 + xOffset, height / 2 + yOffset, 20, 20, mouseX, mouseY))
					{
						CyberwareAPI.setHUDColor(colorOption);
						String s = Integer.toHexString(CyberwareAPI.getHUDColorHex()).toUpperCase();
						while (s.length() < 6)
						{
							s = "0" + s;
						}
						hex.setText(s);
						return;
					}
				}
			}

			if (!color)
			{
				editing = true;
			}
		}
		if (mouseButton == 1)
		{
			if (editing)
			{
				editing = false;
			} else if (selectedPart != -1)
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(mc.player);
				if (cyberwareUserData != null)
				{
					HotkeyHelper.removeHotkey(cyberwareUserData, cyberwareUserData.getActiveItems().get(selectedPart));

					CyberwarePacketHandler.INSTANCE.sendToServer(new SyncHotkeyPacket(
						selectedPart,
						Integer.MAX_VALUE
					));
				}
			}
		}

		if (mouseButton > 1
			&& editing
			&& selectedPart != -1)
		{
			assignHotkey(mouseButton - 100);
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		hex.updateCursorCounter();
		if (close)
		{
			mc.displayGuiScreen(null);
		}
		if (mc != null && mc.gameSettings != null)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(mc.player);
			if (KeyBinds.menu != null
				&& (!GameSettings.isKeyDown(KeyBinds.menu)
				&& !editing
				&& !color)
				|| (cyberwareUserData != null
				&& cyberwareUserData.getNumActiveItems() < 1))
			{
				if (cyberwareUserData != null
					&& selectedPart != -1
					&& !editing)
				{
					ItemStack hki = cyberwareUserData.getActiveItems().get(selectedPart);
					ClientUtils.useActiveItemClient(mc.player, hki);
				}
				mc.displayGuiScreen(null);
			}

			ImmutableSet<KeyMapping> set = ImmutableSet.of(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft
				, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak,
				mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump
			);
			for (KeyMapping keybind : set)
			{
				boolean ee = false;
				int key = keybind.getKeyCode();

				if (!editing)
				{
					if (key < 0)
					{
						int button = 100 + key;
						ee = Mouse.isButtonDown(button);
					} else
					{
						ee = Keyboard.isKeyDown(key);
					}
				}
				KeyMapping.setKeyBindState(key, ee);
			}
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		CyberwareAPI.syncHUDColor();
		//mc.thePlayer.setSprinting(MiscHandler.wasSprinting);
	}

	private void assignHotkey(int code)
	{
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(mc.player);
		if (cyberwareUserData != null)
		{
			HotkeyHelper.removeHotkey(cyberwareUserData, code);
			HotkeyHelper.assignHotkey(cyberwareUserData, cyberwareUserData.getActiveItems().get(selectedPart), code);

			CyberwarePacketHandler.INSTANCE.sendToServer(new SyncHotkeyPacket(selectedPart, code));
		}
		editing = false;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{

		if (keyCode == Keyboard.KEY_ESCAPE)
		{
			if (editing)
			{
				editing = false;
			} else
			{
				close = true;
			}
		} else if (keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT)
		{

		} else if (keyCode == KeyBinds.menu.getKeyCode())
		{

		} else if (selectedPart != -1
			&& editing)
		{
			boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
			assignHotkey(keyCode + (shiftPressed ? 900 : 0));
			/*
			for (int i = 0; i < mc.gameSettings.keyBindsHotbar.length; i++)
			{
				KeyBinding kb = mc.gameSettings.keyBindsHotbar[i];
				if (kb.getKeyCode() == keyCode)
				{
					ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapability(mc.thePlayer);
					
					if (HotkeyHelper.getHotkey(cyberwareUserData.getActiveItems().get(selectedPart)) != i)
					{
						HotkeyHelper.removeHotkey(cyberwareUserData, i);
						HotkeyHelper.assignHotkey(cyberwareUserData, cyberwareUserData.getActiveItems().get
						(selectedPart), i);
						CyberwarePacketHandler.INSTANCE.sendToServer(new SyncHotkeyPacket(selectedPart, i));
					}

					return;
				}
			}
			*/
		}

		hex.textboxKeyTyped(typedChar, keyCode);
		if (hex.getText().length() >= 12)
		{
			hex.setText(hex.getText().substring(6, 12));
		}
		if (hex.getText().length() > 6)
		{
			hex.setText(hex.getText().substring(0, 6));
		}
		hex.setText(hex.getText().replaceAll("[^0-9AaBbCcDdEeFf]", ""));
		if (hex.getText().length() == 6)
		{
			CyberwareAPI.setHUDColor(Integer.parseInt(hex.getText(), 16));
		}
	}
}
