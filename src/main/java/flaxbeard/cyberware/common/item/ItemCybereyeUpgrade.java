package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IHudjack;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.registry.items.Eyes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ItemCybereyeUpgrade extends ItemCyberware implements IMenuItem, IHudjack
{
	private static int zoomSettingOn = 0;
	private static Player player = null;

	public ItemCybereyeUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.EYES);
	}

	@Nonnull
	@Override
	public NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack)
	{
		var targettingItem = Eyes.TARGETING.get();
		if (stack.is(targettingItem))
		{
			return NNLUtil.fromArray(new ItemStack[][]{
				new ItemStack[]{targettingItem.getDefaultInstance()},
				new ItemStack[]{Eyes.HUDJACK.get().getDefaultInstance()}
			});
		}

		return NNLUtil.fromArray(new ItemStack[][]{
			new ItemStack[]{Eyes.CYBEREYE_BASE.get().getDefaultInstance()}
		});
	}

	private static int cache_tickExisted = -1;
	private static boolean cache_isHighlighting = false;
	private static AABB cache_aabbHighlight = new AABB(0, 0, 0, 1, 1, 1);
	private static final List<LivingEntity> entitiesInRange = new ArrayList<>(16);
	private static final float HIGHLIGHT_RANGE = 25F;

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return stack.is(Eyes.NIGHT_VISION.get())
			|| stack.is(Eyes.HUDJACK.get())
			|| stack.is(Eyes.TARGETING.get())
			|| stack.is(Eyes.ZOOM.get());
	}

	@Override
	public void use(Entity entity, ItemStack stack)
	{
		if (stack.is(Eyes.ZOOM.get()) && player != null)
		{
			if (player.isShiftKeyDown())
			{
				zoomSettingOn = (zoomSettingOn + 4 - 1) % 4;
			} else
			{
				zoomSettingOn = (zoomSettingOn + 1) % 4;
			}

			return;
		}
		EnableDisableHelper.toggle(stack);
	}

	@Override
	public String getUnlocalizedLabel(ItemStack stack)
	{
		if (stack.is(Eyes.ZOOM.get()))
		{
			return "cyberware.gui.active.zoom";
		}
		return EnableDisableHelper.getUnlocalizedLabel(stack);
	}

	private static final float[] f = new float[]{1.0F, 0.0F, 0.0F};

	@Override
	public float[] getColor(ItemStack stack)
	{
		if (stack.is(Eyes.ZOOM.get()))
		{
			return null;
		}
		return EnableDisableHelper.isEnabled(stack) ? f : null;
	}

	@Override
	public boolean isActive(ItemStack stack)
	{
		return stack.is(Eyes.HUDJACK.get())
			&& EnableDisableHelper.isEnabled(stack);
	}

	public static class EventHandler
	{
		@SubscribeEvent
		@OnlyIn(Dist.CLIENT)
		public void handleHighlight(TickEvent.RenderTickEvent event)
		{
			Player entityPlayer = Minecraft.getInstance().player;
			if (entityPlayer == null) return;

			if (entityPlayer.tickCount != cache_tickExisted)
			{
				cache_tickExisted = entityPlayer.tickCount;

				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
				if (cyberwareUserData == null) return;

				var targettingItem = Eyes.TARGETING.get();
				ItemStack itemStackTargeting = cyberwareUserData.getCyberware(targettingItem.getDefaultInstance());
				cache_isHighlighting = !itemStackTargeting.isEmpty()
					&& EnableDisableHelper.isEnabled(itemStackTargeting);

				if (cache_isHighlighting)
				{
					var pos = entityPlayer.position();
					cache_aabbHighlight = new AABB(
						pos.x - HIGHLIGHT_RANGE,
						pos.y - HIGHLIGHT_RANGE,
						pos.z - HIGHLIGHT_RANGE,
						pos.x + entityPlayer.getBbWidth() + HIGHLIGHT_RANGE,
						pos.y + entityPlayer.getBbHeight() + HIGHLIGHT_RANGE,
						pos.z + entityPlayer.getBbWidth() + HIGHLIGHT_RANGE
					);
				}
			}

			if (cache_isHighlighting)
			{
				if (event.phase == TickEvent.Phase.START)
				{
					entitiesInRange.clear();
					List<LivingEntity> entityLivingBases = entityPlayer.level.getEntitiesOfClass(
						LivingEntity.class,
						cache_aabbHighlight
					);
					double rangeSq = HIGHLIGHT_RANGE * HIGHLIGHT_RANGE;
					for (LivingEntity entityLivingBase : entityLivingBases)
					{
						if (entityPlayer.position().distanceToSqr(entityLivingBase.position()) <= rangeSq
							&& entityLivingBase != entityPlayer
							&& !entityLivingBase.isCurrentlyGlowing())
						{
							entityLivingBase.setGlowingTag(true);
							entitiesInRange.add(entityLivingBase);
						}
					}
				} else if (event.phase == TickEvent.Phase.END)
				{
					for (LivingEntity entityLivingBase : entitiesInRange)
					{
						entityLivingBase.setGlowingTag(false);
					}
					entitiesInRange.clear();
				}
			}
		}

		// TODO

		// @OnlyIn(Dist.CLIENT)
		// @SubscribeEvent
		// public void handleFog(EntityViewRenderEvent.RenderFogEvent event)
		// {
		// 	Player entityPlayer = Minecraft.getInstance().player;
		// 	ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		// 	if (cyberwareUserData == null) return;
		//
		// 	if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_UNDERWATER_VISION)))
		// 	{
		// 		// TODO: isInFluidType
		// 		if (entityPlayer.isInsideOfMaterial(Material.WATER))
		// 		{
		// 			event.setDensity(0.01F);
		// 			event.setCanceled(true);
		// 		} else if (entityPlayer.isInsideOfMaterial(Material.LAVA))
		// 		{
		// 			event.setDensity(0.7F);
		// 			event.setCanceled(true);
		// 		}
		// 	}
		// }

		@SubscribeEvent
		public void handleNightVision(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
			ItemStack itemStackNightVision = cyberwareUserData.getCyberware(Eyes.NIGHT_VISION.get().getDefaultInstance());

			if (!itemStackNightVision.isEmpty()
				&& EnableDisableHelper.isEnabled(itemStackNightVision))
			{
				entityLivingBase.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 53, true,
					false
				));
			} else
			{
				MobEffectInstance effect = entityLivingBase.getActiveEffectsMap().get(MobEffects.NIGHT_VISION);
				if (effect != null && effect.getAmplifier() == 53)
				{
					entityLivingBase.removeEffect(MobEffects.NIGHT_VISION);
				}
			}
		}

		// TODO
		//@SubscribeEvent
		//@OnlyIn(Dist.CLIENT)
		//public void handleWaterVision(RenderBlockOverlayEvent event)
		//{
		//	Player entityPlayer = event.getEntity();
		//	ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		//	if (cyberwareUserData == null) return;
		//	ItemStack itemStackUnderwaterVision = cyberwareUserData.getCyberware(Eyes.UNDERWATER_VISION.get().getDefaultInstance());
		//
		//	if (!itemStackUnderwaterVision.isEmpty()
		//		&& (event.getBlockForOverlay().getMaterial() == Material.WATER
		//		|| event.getBlockForOverlay().getMaterial() == Material.LAVA))
		//	{
		//		event.setCanceled(true);
		//	}
		//}
		//private static boolean inUse = false;
		//private static boolean wasInUse = false;
		//private static float fov = 0F;
		//private static float sensitivity = 0F;

		//@SubscribeEvent
		//@OnlyIn(Dist.CLIENT)
		//public void tickStart(TickEvent.ClientTickEvent event)
		//{
		//	Minecraft mc = Minecraft.getInstance();
		//	if (event.phase == TickEvent.Phase.START)
		//	{
		//		Player entityPlayer = mc.player;
		//
		//		if (!inUse && !wasInUse)
		//		{
		//			fov = mc.gameSettings.fovSetting;
		//			sensitivity = mc.gameSettings.mouseSensitivity;
		//		}
		//
		//		wasInUse = inUse;
		//
		//		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		//		if (cyberwareUserData != null
		//			&& cyberwareUserData.isCyberwareInstalled(Eyes.ZOOM.get().getDefaultInstance()))
		//		{
		//			player = entityPlayer;
		//
		//			if (mc.gameSettings.thirdPersonView == 0)
		//			{
		//				switch (zoomSettingOn)
		//				{
		//					case 0:
		//						mc.gameSettings.fovSetting = fov;
		//						mc.gameSettings.mouseSensitivity = sensitivity;
		//						break;
		//
		//					case 1:
		//						mc.gameSettings.fovSetting = fov;
		//						mc.gameSettings.mouseSensitivity = sensitivity;
		//						int i = 0;
		//						while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 2.0F)) > 2.5F && i < 200)
		//						{
		//							mc.gameSettings.fovSetting -= 2.5F;
		//							mc.gameSettings.mouseSensitivity -= 0.01F;
		//							i++;
		//						}
		//						break;
		//
		//					case 2:
		//						mc.gameSettings.fovSetting = fov;
		//						mc.gameSettings.mouseSensitivity = sensitivity;
		//						i = 0;
		//						while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 5.0F)) > 2.5F && i < 200)
		//						{
		//							mc.gameSettings.fovSetting -= 2.5F;
		//							mc.gameSettings.mouseSensitivity -= 0.01F;
		//							i++;
		//						}
		//						break;
		//
		//					case 3:
		//						mc.gameSettings.fovSetting = fov;
		//						mc.gameSettings.mouseSensitivity = sensitivity;
		//						i = 0;
		//						while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 12.0F)) > 2.5F && i < 200)
		//						{
		//							mc.gameSettings.fovSetting -= 2.5F;
		//							mc.gameSettings.mouseSensitivity -= 0.01F;
		//							i++;
		//						}
		//						break;
		//				}
		//			}
		//		} else
		//		{
		//			zoomSettingOn = 0;
		//		}
		//		inUse = zoomSettingOn != 0;
		//
		//		if (!inUse && wasInUse)
		//		{
		//
		//			// TODO: ForgeHooksClient.getFieldOfViewModifier
		//			mc.gameSettings.fovSetting = fov;
		//			mc.gameSettings.mouseSensitivity = sensitivity;
		//		}
		//	}
		//}
	}
}
