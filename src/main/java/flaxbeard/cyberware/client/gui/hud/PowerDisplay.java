package flaxbeard.cyberware.client.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.hud.HudElementBase;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.handler.HudHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;

public class PowerDisplay extends HudElementBase
{
	private final static float[] colorLowPowerFloats = {1.0F, 0.0F, 0.125F};
	private final static int colorLowPowerHex = 0xFF0020;
	private static float cache_percentFull = 0;
	private static int cache_power_capacity = 0;
	private static int cache_power_stored = 0;
	private static int cache_power_production = 0;
	private static int cache_power_consumption = 0;
	private static float[] cache_hudColor = colorLowPowerFloats;
	private static int cache_hudColorHex = 0x00FFFF;

	public PowerDisplay()
	{
		super("cyberware:power");
		setDefaultX(5);
		setDefaultY(5);
		setHeight(25);
		setWidth(101);
	}

	@Override
	public void renderElement(int x, int y, Player entityPlayer, PoseStack poseStack, Window window,
							  boolean isHUDjackAvailable, boolean isConfigOpen, float partialTicks)
	{
		if (isHidden()
			|| !isHUDjackAvailable)
		{
			return;
		}

		boolean isRightAnchored = getHorizontalAnchor() == EnumAnchorHorizontal.RIGHT;

		if (entityPlayer.tickCount % 20 == 0)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData == null) return;

			cache_percentFull = cyberwareUserData.getPercentFull();
			cache_power_capacity = cyberwareUserData.getCapacity();
			cache_power_stored = cyberwareUserData.getStoredPower();
			cache_power_production = cyberwareUserData.getProduction();
			cache_power_consumption = cyberwareUserData.getConsumption();
			cache_hudColor = cyberwareUserData.getHudColor();
			cache_hudColorHex = cyberwareUserData.getHudColorHex();
		}

		if (cache_power_capacity == 0) return;

		boolean isLowPower = cache_percentFull <= 0.2F;
		boolean isCriticalPower = cache_percentFull <= 0.05F;

		if (isCriticalPower
			&& entityPlayer.tickCount % 4 == 0)
		{
			return;
		}

		float[] colorFloats = isLowPower ? colorLowPowerFloats : cache_hudColor;
		int colorHex = isLowPower ? colorLowPowerHex : cache_hudColorHex;

		poseStack.pushPose();

		Font fontRenderer = Minecraft.getInstance().font;

		// battery icon
		RenderSystem.setShaderTexture(0, HudHandler.HUD_TEXTURE);
		//RenderSystem.disableAlpha(); // TODO uhhh
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		int uOffset = isLowPower ? 39 : 0;
		int xOffset = isRightAnchored ? (x + getWidth() - 13) : x;
		int yBatterySize = Math.round(21F * cache_percentFull);

		RenderSystem.setShaderColor(colorFloats[0], colorFloats[1], colorFloats[2], 1.0F);

		// battery top part
		ClientUtils.drawTexturedModalRect(xOffset, y, uOffset, 0, 13, 2 + (21 - yBatterySize));
		// battery background
		ClientUtils.drawTexturedModalRect(xOffset, y + 2 + (21 - yBatterySize), 13 + uOffset, 2 + (21 - yBatterySize),
			13, yBatterySize + 2
		);
		// battery foreground
		ClientUtils.drawTexturedModalRect(xOffset, y + 2 + (21 - yBatterySize), 26 + uOffset, 2 + (21 - yBatterySize),
			13, yBatterySize + 2
		);

		// storage stats
		String textPowerStorage = cache_power_stored + " / " + cache_power_capacity;
		int xPowerStorage = isRightAnchored ? x + getWidth() - 15 - fontRenderer.width(textPowerStorage) :
			x + 15;
		fontRenderer.drawShadow(poseStack, textPowerStorage, xPowerStorage, y + 4, colorHex);

		// progression stats
		String textPowerProgression = "-" + cache_power_consumption + " / +" + cache_power_production;
		int xPowerProgression = isRightAnchored ?
			x + getWidth() - 15 - fontRenderer.width(textPowerProgression) : x + 15;
		fontRenderer.drawShadow(poseStack, textPowerProgression, xPowerProgression, y + 14, colorHex);

		poseStack.popPose();
	}
}
