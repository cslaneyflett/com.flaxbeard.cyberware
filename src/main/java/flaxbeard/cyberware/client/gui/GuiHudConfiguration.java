package flaxbeard.cyberware.client.gui;

import com.mojang.blaze3d.platform.Window;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.hud.CyberwareHudDataEvent;
import flaxbeard.cyberware.api.hud.CyberwareHudEvent;
import flaxbeard.cyberware.api.hud.IHudElement;
import flaxbeard.cyberware.api.hud.IHudElement.EnumAnchorHorizontal;
import flaxbeard.cyberware.api.hud.IHudElement.EnumAnchorVertical;
import flaxbeard.cyberware.api.item.IHudjack;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.client.gui.hud.HudNBTData;
import flaxbeard.cyberware.common.handler.HudHandler;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.SyncHudDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiHudConfiguration extends GuiScreen
{
	IHudElement dragging = null;
	IHudElement hoveredElement = null;
	int offsetX = 0;
	int offsetY = 0;
	Window sr = null;
	Minecraft mc = null;
	boolean clicked = false;

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		Minecraft mc = Minecraft.getInstance();
		sr = mc.getWindow();

		GlStateManager.pushMatrix();

		boolean active = false;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(mc.player);
		if (cyberwareUserData != null)
		{
			List<ItemStack> hudjackItems = cyberwareUserData.getHudjackItems();
			for (ItemStack stack : hudjackItems)
			{
				if (((IHudjack) CyberwareAPI.getCyberware(stack)).isActive(stack))
				{
					active = true;
					break;
				}
			}
		}

		CyberwareHudEvent hudEvent = new CyberwareHudEvent(sr, active);
		MinecraftForge.EVENT_BUS.post(hudEvent);
		List<IHudElement> elements = hudEvent.getElements();

		hoveredElement = null;
		for (IHudElement element : elements)
		{
			if (hoveredElement == null
				&& dragging == null)
			{
				int elemX = getAbsoluteX(sr, element);
				int elemY = getAbsoluteY(sr, element);

				if (isPointInRegion(elemX, elemY, element.getWidth(), element.getHeight(), mouseX, mouseY))
				{
					hoveredElement = element;
					offsetX = mouseX - elemX;
					offsetY = mouseY - elemY;
				}
			}
		}

		for (IHudElement element : elements)
		{
			drawBox(element, mouseX, mouseY);
			drawButtons(element, mouseX, mouseY);
		}

		for (IHudElement element : elements)
		{
			drawButtonTooltips(element, mouseX, mouseY);
		}

		if (dragging != null)
		{
			int moveToX = mouseX - offsetX;
			int moveToY = mouseY - offsetY;

			setXFromAbsolute(sr, dragging, moveToX);
			setYFromAbsolute(sr, dragging, moveToY);

			if (mc.gameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
			{
				dragging.setX(Math.round(dragging.getX() / 5F) * 5);
				dragging.setY(Math.round(dragging.getY() / 5F) * 5);
			}

			List<String> l = new ArrayList<>();
			l.add(dragging.getX() + ", " + dragging.getY());
			ClientUtils.drawHoveringText(this, l, mouseX, mouseY, mc.fontRenderer);
		} else if (hoveredElement != null)
		{
			List<String> l = new ArrayList<>();
			l.add(hoveredElement.getX() + ", " + hoveredElement.getY());
			ClientUtils.drawHoveringText(this, l, mouseX, mouseY, mc.fontRenderer);
		}

		GlStateManager.popMatrix();
		clicked = false;
	}

	private void drawBox(IHudElement element, int mouseX, int mouseY)
	{
		Minecraft.getInstance().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);

		int elemX = getAbsoluteX(sr, element) - 1;
		int elemY = getAbsoluteY(sr, element) - 1;

		GlStateManager.pushMatrix();
		float[] color = CyberwareAPI.getHUDColor();
		GlStateManager.color(color[0], color[1], color[2]);

		if (element == dragging
			|| element == hoveredElement)
		{
			boolean right = element.getHorizontalAnchor() == EnumAnchorHorizontal.RIGHT;
			boolean bottom = element.getVerticalAnchor() == EnumAnchorVertical.BOTTOM;

			int elemPosX = element.getX();
			int pos = right ? getAbsoluteX(sr, element) + element.getWidth() : 0;
			int posY = bottom ? elemY + element.getHeight() + 1 : elemY;
			posY = Math.max(1, posY);
			posY = Math.min(sr.getGuiScaledHeight() - 2, posY);

			while (elemPosX >= 2)
			{
				ClientUtils.drawTexturedModalRect(pos, posY, 255, 0, 1, 1);

				pos += 2;
				elemPosX -= 2;
			}
			ClientUtils.drawTexturedModalRect(pos, posY, 255, 0, elemPosX, 1);

			int elemPosY = element.getY();
			pos = bottom ? getAbsoluteY(sr, element) + element.getHeight() : 0;
			int posX = right ? elemX + element.getWidth() + 1 : elemX;
			posX = Math.max(1, posX);
			posX = Math.min(sr.getGuiScaledWidth() - 2, posX);

			while (elemPosY >= 2)
			{
				ClientUtils.drawTexturedModalRect(posX, pos, 255, 0, 1, 1);

				pos += 2;
				elemPosY -= 2;
			}

			ClientUtils.drawTexturedModalRect(posX, pos, 255, 0, 1, elemPosY);
		}

		boolean shift = (mc.player.tickCount / 4) % 2 == 0;
		int one = shift ? 254 : 255;
		int two = shift ? 255 : 254;

		int width = element.getWidth() + 2;
		int pos = 0;
		while (width >= 2)
		{
			ClientUtils.drawTexturedModalRect(elemX + pos, elemY, one, 0, 1, 1);
			ClientUtils.drawTexturedModalRect(elemX + pos + 1, elemY, two, 0, 1, 1);

			ClientUtils.drawTexturedModalRect(elemX + pos, elemY + element.getHeight() + 1, one, 0, 1, 1);
			ClientUtils.drawTexturedModalRect(elemX + pos + 1, elemY + element.getHeight() + 1, two, 0, 1, 1);

			pos += 2;
			width -= 2;
		}
		ClientUtils.drawTexturedModalRect(elemX, elemY, one, 0, width, 1);
		ClientUtils.drawTexturedModalRect(elemX, elemY + element.getHeight() + 1, 255, 0, width, 1);

		int height = element.getHeight() + 2;
		pos = 0;
		while (height >= 2)
		{
			ClientUtils.drawTexturedModalRect(elemX, elemY + pos, one, 0, 1, 1);
			ClientUtils.drawTexturedModalRect(elemX, elemY + pos + 1, two, 0, 1, 1);

			ClientUtils.drawTexturedModalRect(elemX + element.getWidth() + 1, elemY + pos, one, 0, 1, 1);
			ClientUtils.drawTexturedModalRect(elemX + element.getWidth() + 1, elemY + pos + 1, two, 0, 1, 1);

			pos += 2;
			height -= 2;
		}

		ClientUtils.drawTexturedModalRect(elemX, elemY + pos, one, 0, 1, height);
		ClientUtils.drawTexturedModalRect(elemX + element.getWidth() + 1, elemY + pos, two, 0, 1, height);

		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F);
	}

	protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
	{
		return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1
			&& pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
	}

	private void drawButtonTooltips(IHudElement element, int mouseX, int mouseY)
	{
		Minecraft.getInstance().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);

		int elemX = getAbsoluteX(sr, element) - 1;
		int elemY = getAbsoluteY(sr, element) - 1;

		int buttonsY = (elemY + element.getHeight() + 10 > sr.getGuiScaledHeight()) ? elemY - 11 :
			(elemY + element.getHeight() + 4);
		int buttonsX = elemX + 5;

		boolean showHideHover = false;
		boolean hidden = false;
		if (element.canHide())
		{
			showHideHover = isPointInRegion(buttonsX, buttonsY, 9, 9, mouseX, mouseY);
			hidden = element.isHidden();
			buttonsX += 11;
		}

		boolean upDownHover = isPointInRegion(buttonsX, buttonsY, 9, 9, mouseX, mouseY);
		boolean down = element.getVerticalAnchor() != EnumAnchorVertical.BOTTOM;
		buttonsX += 11;

		boolean leftRightHover = isPointInRegion(buttonsX, buttonsY, 9, 9, mouseX, mouseY);
		boolean right = element.getHorizontalAnchor() != EnumAnchorHorizontal.RIGHT;
		buttonsX += 11;

		boolean resetHover = isPointInRegion(buttonsX, buttonsY, 9, 9, mouseX, mouseY);

		if (upDownHover)
		{
			List<String> l = new ArrayList<>();
			l.add(I18n.get(down ? "cyberware.gui.stickDown" : "cyberware.gui.stickUp"));
			ClientUtils.drawHoveringText(this, l, mouseX, mouseY, mc.fontRenderer);

			if (clicked)
			{
				flipVertical(element);
			}
		}

		if (showHideHover)
		{
			List<String> l = new ArrayList<>();
			l.add(I18n.get(hidden ? "cyberware.gui.show" : "cyberware.gui.hide"));
			ClientUtils.drawHoveringText(this, l, mouseX, mouseY, mc.fontRenderer);

			if (clicked)
			{
				element.setHidden(!hidden);
			}
		}

		if (resetHover)
		{
			List<String> l = new ArrayList<>();
			l.add(I18n.get("cyberware.gui.reset_hud"));
			ClientUtils.drawHoveringText(this, l, mouseX, mouseY, mc.fontRenderer);

			if (clicked)
			{
				element.reset();
			}
		}

		if (leftRightHover)
		{
			List<String> l = new ArrayList<>();
			l.add(I18n.get(right ? "cyberware.gui.stick_right" : "cyberware.gui.stick_left"));
			ClientUtils.drawHoveringText(this, l, mouseX, mouseY, mc.fontRenderer);

			if (clicked)
			{
				flipHorizontal(element);
			}
		}
	}

	private void drawButtons(IHudElement element, int mouseX, int mouseY)
	{
		Minecraft.getInstance().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);

		int elemX = getAbsoluteX(sr, element) - 1;
		int elemY = getAbsoluteY(sr, element) - 1;

		int buttonsY = (elemY + element.getHeight() + 10 > sr.getGuiScaledHeight()) ? elemY - 11 :
			(elemY + element.getHeight() + 4);
		int buttonsX = elemX + 5;

		if (element.canHide())
		{
			boolean showHideHover = isPointInRegion(buttonsX, buttonsY, 9, 9, mouseX, mouseY);
			boolean hidden = element.isHidden();
			ClientUtils.drawTexturedModalRect(buttonsX, buttonsY, showHideHover ^ hidden ? 125 : 116, 0, 9, 9);
			buttonsX += 11;
		}

		boolean upDownHover = isPointInRegion(buttonsX, buttonsY, 9, 9, mouseX, mouseY);
		boolean down = element.getVerticalAnchor() != EnumAnchorVertical.BOTTOM;
		ClientUtils.drawTexturedModalRect(buttonsX, buttonsY, down ^ upDownHover ? 80 : 89, 0, 9, 9);
		buttonsX += 11;

		boolean leftRightHover = isPointInRegion(buttonsX, buttonsY, 9, 9, mouseX, mouseY);
		boolean right = element.getHorizontalAnchor() != EnumAnchorHorizontal.RIGHT;
		ClientUtils.drawTexturedModalRect(buttonsX, buttonsY, right ^ leftRightHover ? 98 : 107, 0, 9, 9);
		buttonsX += 11;

		boolean resetHover = isPointInRegion(buttonsX, buttonsY, 9, 9, mouseX, mouseY);
		ClientUtils.drawTexturedModalRect(buttonsX, buttonsY, 134, 0, 9, 9);
		buttonsX += 11;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		if (mouseButton == 0)
		{
			if (dragging == null)
			{
				dragging = hoveredElement;
			}
			clicked = true;
		}
		if (mouseButton == 1
			&& hoveredElement != null)
		{
			flipVertical(hoveredElement);
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton)
	{
		if (mouseButton == 0
			&& dragging != null)
		{
			dragging = null;
		}
	}

	public static int getAbsoluteX(Window sr, IHudElement element)
	{
		if (element.getHorizontalAnchor() == EnumAnchorHorizontal.RIGHT)
		{
			return sr.getGuiScaledWidth() - element.getX() - element.getWidth();
		}
		return element.getX();
	}

	public static int getAbsoluteY(Window sr, IHudElement element)
	{
		if (element.getVerticalAnchor() == EnumAnchorVertical.BOTTOM)
		{
			return sr.getGuiScaledHeight() - element.getY() - element.getHeight();
		}
		return element.getY();
	}

	public static void setXFromAbsolute(Window sr, IHudElement element, int x)
	{
		if (element.getHorizontalAnchor() == EnumAnchorHorizontal.RIGHT)
		{
			element.setX(sr.getGuiScaledWidth() - x - element.getWidth());
		} else
		{
			element.setX(x);
		}
	}

	public static void setYFromAbsolute(Window sr, IHudElement element, int y)
	{
		if (element.getVerticalAnchor() == EnumAnchorVertical.BOTTOM)
		{
			element.setY(sr.getGuiScaledHeight() - y - element.getHeight());
		} else
		{
			element.setY(y);
		}
	}

	private void flipVertical(IHudElement element)
	{
		int y = getAbsoluteY(sr, element);
		element.setVerticalAnchor(element.getVerticalAnchor() == EnumAnchorVertical.BOTTOM ? EnumAnchorVertical.TOP :
			EnumAnchorVertical.BOTTOM);
		setYFromAbsolute(sr, element, y);
	}

	private void flipHorizontal(IHudElement element)
	{
		int x = getAbsoluteX(sr, element);
		element.setHorizontalAnchor(element.getHorizontalAnchor() == EnumAnchorHorizontal.RIGHT ?
			EnumAnchorHorizontal.LEFT : EnumAnchorHorizontal.RIGHT);
		setXFromAbsolute(sr, element, x);
	}

	@Override
	public void updateScreen()
	{
		if (mc != null
			&& mc.gameSettings != null)
		{
			if (mc.gameSettings.isKeyDown(mc.gameSettings.keyBindInventory))
			{
				mc.displayGuiScreen(null);
			}
		}
		super.updateScreen();
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	public void onGuiClosed()
	{
		CompoundTag tagCompound = new CompoundTag();

		CyberwareHudDataEvent hudEvent = new CyberwareHudDataEvent();
		MinecraftForge.EVENT_BUS.post(hudEvent);
		List<IHudElement> elements = hudEvent.getElements();

		for (IHudElement element : elements)
		{
			HudNBTData elementData = new HudNBTData(new CompoundTag());
			element.save(elementData);
			tagCompound.put(element.getUniqueName(), elementData.getTag());
		}

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(mc.player);
		if (cyberwareUserData != null)
		{
			cyberwareUserData.setHudData(tagCompound);
		}

		CyberwarePacketHandler.INSTANCE.sendToServer(new SyncHudDataPacket(tagCompound));
	}
}
