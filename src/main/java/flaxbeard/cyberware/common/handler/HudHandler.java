package flaxbeard.cyberware.common.handler;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.hud.CyberwareHudDataEvent;
import flaxbeard.cyberware.api.hud.CyberwareHudEvent;
import flaxbeard.cyberware.api.hud.IHudElement;
import flaxbeard.cyberware.api.hud.IHudElement.EnumAnchorHorizontal;
import flaxbeard.cyberware.api.hud.IHudElement.EnumAnchorVertical;
import flaxbeard.cyberware.api.hud.NotificationInstance;
import flaxbeard.cyberware.api.item.IHudjack;
import flaxbeard.cyberware.client.KeyBinds;
import flaxbeard.cyberware.client.gui.GuiHudConfiguration;
import flaxbeard.cyberware.client.gui.hud.MissingPowerDisplay;
import flaxbeard.cyberware.client.gui.hud.NotificationDisplay;
import flaxbeard.cyberware.client.gui.hud.PowerDisplay;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class HudHandler
{
	public static final HudHandler INSTANCE = new HudHandler();

	// http://stackoverflow.com/a/16206356/1754640
	private static class NotificationStack<T> extends Stack<T>
	{
		private int maxSize;

		public NotificationStack(int size)
		{
			super();
			this.maxSize = size;
		}

		@Override
		public T push(T object)
		{
			while (this.size() >= maxSize)
			{
				this.remove(0);
			}
			return super.push(object);
		}
	}

	public static void addNotification(NotificationInstance notification)
	{
		notifications.push(notification);
	}

	public static final ResourceLocation HUD_TEXTURE = new ResourceLocation(Cyberware.MODID + ":textures/gui/hud.png");
	public static Stack<NotificationInstance> notifications = new NotificationStack<>(5);
	private static PowerDisplay powerDisplay = new PowerDisplay();
	private static MissingPowerDisplay missingPowerDisplay = new MissingPowerDisplay();
	private static NotificationDisplay notificationDisplay = new NotificationDisplay();

	static
	{
		notificationDisplay.setHorizontalAnchor(EnumAnchorHorizontal.LEFT);
		notificationDisplay.setVerticalAnchor(EnumAnchorVertical.BOTTOM);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void addHudElements(CyberwareHudEvent event)
	{
		if (event.isHudjackAvailable())
		{
			event.addElement(powerDisplay);
			event.addElement(missingPowerDisplay);
			event.addElement(notificationDisplay);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void saveHudElements(CyberwareHudDataEvent event)
	{
		event.addElement(powerDisplay);
		event.addElement(missingPowerDisplay);
		event.addElement(notificationDisplay);
	}

	private int cache_tickExisted = 0;
	private float cache_floatingFactor = 0.0F;
	private List<IHudElement> cache_hudElements = new ArrayList<>();
	private boolean cache_isHUDjackAvailable = false;
	private boolean cache_promptToOpenMenu = false;
	private int cache_hudColorHex = 0x00FFFF;
	private int lastTickExisted = 0;
	private double lastVelX = 0;
	private double lastVelY = 0;
	private double lastVelZ = 0;
	private double lastLastVelX = 0;
	private double lastLastVelY = 0;
	private double lastLastVelZ = 0;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onRender(@Nonnull RenderGameOverlayEvent.Pre event)
	{
		if (event.getType() == ElementType.CHAT)
		{
			drawHUD(event.getResolution(), event.getPartialTicks());
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void drawHUD(ScaledResolution scaledResolution, float partialTick)
	{
		Minecraft mc = Minecraft.getInstance();
		EntityPlayerSP entityPlayerSP = mc.player;
		if (entityPlayerSP == null) return;

		if (entityPlayerSP.tickCount != cache_tickExisted)
		{
			cache_tickExisted = entityPlayerSP.tickCount;

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayerSP);
			if (cyberwareUserData == null) return;

			cache_floatingFactor = 0.0F;
			boolean isHUDjackAvailable = false;

			List<ItemStack> listHUDjackItems = cyberwareUserData.getHudjackItems();
			for (ItemStack stack : listHUDjackItems)
			{
				if (((IHudjack) CyberwareAPI.getCyberware(stack)).isActive(stack))
				{
					isHUDjackAvailable = true;
					if (CyberwareConfig.INSTANCE.ENABLE_FLOAT.get())
					{
						if (CyberwareAPI.getCyberware(stack) == CyberwareContent.eyeUpgrades)
						{
							cache_floatingFactor = CyberwareConfig.INSTANCE.HUDLENS_FLOAT.get();
						} else
						{
							cache_floatingFactor = CyberwareConfig.INSTANCE.HUDJACK_FLOAT.get();
						}
					}
					break;
				}
			}

			CyberwareHudEvent hudEvent = new CyberwareHudEvent(scaledResolution, isHUDjackAvailable);
			MinecraftForge.EVENT_BUS.post(hudEvent);
			cache_hudElements = hudEvent.getElements();
			cache_isHUDjackAvailable = hudEvent.isHudjackAvailable();
			cache_promptToOpenMenu = cyberwareUserData.getActiveItems().size() > 0
				&& !cyberwareUserData.hasOpenedRadialMenu();
			cache_hudColorHex = cyberwareUserData.getHudColorHex();
		}

		GlStateManager.pushMatrix();

		double accelLastY = lastVelY - lastLastVelY;
		double accelY = entityPlayerSP.motionY - lastVelY;
		double accelPitch =
			accelLastY + (accelY - accelLastY) * (partialTick + entityPlayerSP.tickCount - lastTickExisted) / 2F;

		double pitchCameraMove =
			cache_floatingFactor * ((entityPlayerSP.prevRenderArmPitch + (entityPlayerSP.renderArmPitch - entityPlayerSP.prevRenderArmPitch) * partialTick) - entityPlayerSP.rotationPitch);
		double yawCameraMove =
			cache_floatingFactor * ((entityPlayerSP.prevRenderArmYaw + (entityPlayerSP.renderArmYaw - entityPlayerSP.prevRenderArmYaw) * partialTick) - entityPlayerSP.rotationYaw);

		GlStateManager.translate(yawCameraMove, pitchCameraMove + accelPitch * 50F * cache_floatingFactor, 0);

		if (entityPlayerSP.tickCount > lastTickExisted + 1)
		{
			lastTickExisted = entityPlayerSP.tickCount;
			lastLastVelX = lastVelX;
			lastLastVelY = lastVelY;
			lastLastVelZ = lastVelZ;
			lastVelX = entityPlayerSP.motionX;
			lastVelY = entityPlayerSP.motionY;
			lastVelZ = entityPlayerSP.motionZ;
		}

		for (IHudElement hudElement : cache_hudElements)
		{
			if (hudElement.getHeight() + GuiHudConfiguration.getAbsoluteY(scaledResolution, hudElement) <= 3)
			{
				GuiHudConfiguration.setYFromAbsolute(scaledResolution, hudElement, 0 - hudElement.getHeight() + 4);
			}

			if (GuiHudConfiguration.getAbsoluteY(scaledResolution, hudElement) >= scaledResolution.getScaledHeight() - 3)
			{
				GuiHudConfiguration.setYFromAbsolute(scaledResolution, hudElement,
					scaledResolution.getScaledHeight() - 4
				);
			}

			if (hudElement.getWidth() + GuiHudConfiguration.getAbsoluteX(scaledResolution, hudElement) <= 3)
			{
				GuiHudConfiguration.setXFromAbsolute(scaledResolution, hudElement, 0 - hudElement.getWidth() + 4);
			}

			if (GuiHudConfiguration.getAbsoluteX(scaledResolution, hudElement) >= scaledResolution.getScaledWidth() - 3)
			{
				GuiHudConfiguration.setXFromAbsolute(scaledResolution, hudElement,
					scaledResolution.getScaledWidth() - 4
				);
			}

			hudElement.render(entityPlayerSP, scaledResolution, cache_isHUDjackAvailable,
				mc.currentScreen instanceof GuiHudConfiguration, partialTick
			);
		}

		// Display a prompt to the user to open the radial menu if they haven't yet
		if (cache_promptToOpenMenu)
		{
			String textOpenMenu = I18n.get("cyberware.gui.open_menu", KeyBinds.menu.getDisplayName());
			Font fontRenderer = mc.fontRenderer;
			fontRenderer.drawStringWithShadow(textOpenMenu,
				scaledResolution.getScaledWidth() - fontRenderer.getStringWidth(textOpenMenu) - 5, 5, cache_hudColorHex
			);
		}

		GlStateManager.popMatrix();
	}
}