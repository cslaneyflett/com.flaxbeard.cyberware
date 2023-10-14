package flaxbeard.cyberware.common.handler;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
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
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.registry.items.Eyes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
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
	public static final Stack<NotificationInstance> notifications = new NotificationStack<>(5);
	private static final PowerDisplay powerDisplay = new PowerDisplay();
	private static final MissingPowerDisplay missingPowerDisplay = new MissingPowerDisplay();
	private static final NotificationDisplay notificationDisplay = new NotificationDisplay();

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
	private double cache_floatingFactor = 0.0F;
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
	public void onRender(@Nonnull RenderGuiOverlayEvent.Pre event)
	{
		if (event.getOverlay() == VanillaGuiOverlay.CHAT_PANEL.type())
		{
			drawHUD(event.getPoseStack(), event.getWindow(), event.getPartialTick());
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void drawHUD(PoseStack poseStack, Window window, float partialTick)
	{
		Minecraft mc = Minecraft.getInstance();
		AbstractClientPlayer entityPlayerSP = mc.player;
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
						if (CyberwareAPI.getCyberware(stack) == Eyes.CYBEREYE_BASE.get())
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

			CyberwareHudEvent hudEvent = new CyberwareHudEvent(window, isHUDjackAvailable);
			MinecraftForge.EVENT_BUS.post(hudEvent);
			cache_hudElements = hudEvent.getElements();
			cache_isHUDjackAvailable = hudEvent.isHudjackAvailable();
			cache_promptToOpenMenu = !cyberwareUserData.getActiveItems().isEmpty()
				&& !cyberwareUserData.hasOpenedRadialMenu();
			cache_hudColorHex = cyberwareUserData.getHudColorHex();
		}

		poseStack.pushPose();

		var motion = entityPlayerSP.getDeltaMovement();
		double accelLastY = lastVelY - lastLastVelY;
		double accelY = motion.y - lastVelY;
		double accelPitch =
			accelLastY + (accelY - accelLastY) * (partialTick + entityPlayerSP.tickCount - lastTickExisted) / 2F;

		// TODO
		double pitchCameraMove = 0.0D;
		//cache_floatingFactor * ((entityPlayerSP.prevRenderArmPitch + (entityPlayerSP.renderArmPitch - entityPlayerSP.prevRenderArmPitch) * partialTick) - entityPlayerSP.rotationPitch);
		double yawCameraMove = 0.0D;
		//cache_floatingFactor * ((entityPlayerSP.prevRenderArmYaw + (entityPlayerSP.renderArmYaw - entityPlayerSP.prevRenderArmYaw) * partialTick) - entityPlayerSP.rotationYaw);

		poseStack.translate(yawCameraMove, pitchCameraMove + accelPitch * 50F * cache_floatingFactor, 0);

		if (entityPlayerSP.tickCount > lastTickExisted + 1)
		{
			lastTickExisted = entityPlayerSP.tickCount;
			lastLastVelX = lastVelX;
			lastLastVelY = lastVelY;
			lastLastVelZ = lastVelZ;
			lastVelX = motion.x;
			lastVelY = motion.y;
			lastVelZ = motion.z;
		}

		for (IHudElement hudElement : cache_hudElements)
		{
			if (hudElement.getHeight() + GuiHudConfiguration.getAbsoluteY(window, hudElement) <= 3)
			{
				GuiHudConfiguration.setYFromAbsolute(window, hudElement, 0 - hudElement.getHeight() + 4);
			}

			if (GuiHudConfiguration.getAbsoluteY(window, hudElement) >= window.getGuiScaledHeight() - 3)
			{
				GuiHudConfiguration.setYFromAbsolute(window, hudElement,
					window.getGuiScaledHeight() - 4
				);
			}

			if (hudElement.getWidth() + GuiHudConfiguration.getAbsoluteX(window, hudElement) <= 3)
			{
				GuiHudConfiguration.setXFromAbsolute(window, hudElement, 0 - hudElement.getWidth() + 4);
			}

			if (GuiHudConfiguration.getAbsoluteX(window, hudElement) >= window.getGuiScaledWidth() - 3)
			{
				GuiHudConfiguration.setXFromAbsolute(window, hudElement,
					window.getGuiScaledWidth() - 4
				);
			}

			hudElement.render(entityPlayerSP, poseStack, window, cache_isHUDjackAvailable,
				mc.screen instanceof GuiHudConfiguration, partialTick
			);
		}

		// Display a prompt to the user to open the radial menu if they haven't yet
		if (cache_promptToOpenMenu)
		{
			String textOpenMenu = I18n.get("cyberware.gui.open_menu", KeyBinds.menu.getName());
			Font fontRenderer = mc.font;
			fontRenderer.draw(poseStack, textOpenMenu,
				window.getGuiScaledWidth() - fontRenderer.width(textOpenMenu) - 5, 5, cache_hudColorHex
			);
		}

		poseStack.popPose();
	}
}