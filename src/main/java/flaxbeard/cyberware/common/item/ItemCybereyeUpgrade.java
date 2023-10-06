package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IHudjack;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.misc.NNLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ItemCybereyeUpgrade extends ItemCyberware implements IMenuItem, IHudjack
{
	public static final int META_NIGHT_VISION = 0;
	public static final int META_UNDERWATER_VISION = 1;
	public static final int META_HUDJACK = 2;
	public static final int META_TARGETING = 3;
	public static final int META_ZOOM = 4;

	public ItemCybereyeUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
	{
		if (CyberwareItemMetadata.matches(stack, META_TARGETING))
		{
			return NNLUtil.fromArray(new ItemStack[][]{
				new ItemStack[]{CyberwareContent.cybereyes.getCachedStack(0)},
				new ItemStack[]{getCachedStack(META_HUDJACK)}});
		}

		return NNLUtil.fromArray(new ItemStack[][]{
			new ItemStack[]{CyberwareContent.cybereyes.getCachedStack(0)}});
	}

	private static int cache_tickExisted = -1;
	private static boolean cache_isHighlighting = false;
	private static AABB cache_aabbHighlight = new AABB(0, 0, 0, 1, 1, 1);
	private static final List<LivingEntity> entitiesInRange = new ArrayList<>(16);
	private static final float HIGHLIGHT_RANGE = 25F;

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
			ItemStack itemStackTargeting = cyberwareUserData.getCyberware(getCachedStack(META_TARGETING));
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

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void handleFog(FogDensity event)
	{
		Player entityPlayer = Minecraft.getInstance().player;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;

		if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_UNDERWATER_VISION)))
		{
			// TODO: isInFluidType
			if (entityPlayer.isInsideOfMaterial(Material.WATER))
			{
				event.setDensity(0.01F);
				event.setCanceled(true);
			} else if (entityPlayer.isInsideOfMaterial(Material.LAVA))
			{
				event.setDensity(0.7F);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void handleNightVision(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		ItemStack itemStackNightVision = cyberwareUserData.getCyberware(getCachedStack(META_NIGHT_VISION));

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

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void handleWaterVision(RenderBlockOverlayEvent event)
	{
		Player entityPlayer = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;
		ItemStack itemStackUnderwaterVision = cyberwareUserData.getCyberware(getCachedStack(META_UNDERWATER_VISION));

		if (!itemStackUnderwaterVision.isEmpty()
			&& (event.getBlockForOverlay().getMaterial() == Material.WATER
			|| event.getBlockForOverlay().getMaterial() == Material.LAVA))
		{
			event.setCanceled(true);
		}
	}

	private static boolean inUse = false;
	private static boolean wasInUse = false;
	private static int zoomSettingOn = 0;
	private static float fov = 0F;
	private static float sensitivity = 0F;
	private static Player player = null;

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void tickStart(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (event.phase == TickEvent.Phase.START)
		{
			wasInUse = inUse;

			Player entityPlayer = mc.player;

			if (!inUse && !wasInUse)
			{
				fov = mc.gameSettings.fovSetting;
				sensitivity = mc.gameSettings.mouseSensitivity;
			}

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null
				&& cyberwareUserData.isCyberwareInstalled(getCachedStack(META_ZOOM)))
			{
				player = entityPlayer;

				if (mc.gameSettings.thirdPersonView == 0)
				{
					switch (zoomSettingOn)
					{
						case 0:
							mc.gameSettings.fovSetting = fov;
							mc.gameSettings.mouseSensitivity = sensitivity;
							break;

						case 1:
							mc.gameSettings.fovSetting = fov;
							mc.gameSettings.mouseSensitivity = sensitivity;
							int i = 0;
							while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 2.0F)) > 2.5F && i < 200)
							{
								mc.gameSettings.fovSetting -= 2.5F;
								mc.gameSettings.mouseSensitivity -= 0.01F;
								i++;
							}
							break;

						case 2:
							mc.gameSettings.fovSetting = fov;
							mc.gameSettings.mouseSensitivity = sensitivity;
							i = 0;
							while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 5.0F)) > 2.5F && i < 200)
							{
								mc.gameSettings.fovSetting -= 2.5F;
								mc.gameSettings.mouseSensitivity -= 0.01F;
								i++;
							}
							break;

						case 3:
							mc.gameSettings.fovSetting = fov;
							mc.gameSettings.mouseSensitivity = sensitivity;
							i = 0;
							while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 12.0F)) > 2.5F && i < 200)
							{
								mc.gameSettings.fovSetting -= 2.5F;
								mc.gameSettings.mouseSensitivity -= 0.01F;
								i++;
							}
							break;
					}
				}
			} else
			{
				zoomSettingOn = 0;
			}
			inUse = zoomSettingOn != 0;

			if (!inUse && wasInUse)
			{

				// TODO: ForgeHooksClient.getFieldOfViewModifier
				mc.gameSettings.fovSetting = fov;
				mc.gameSettings.mouseSensitivity = sensitivity;
			}
		}
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_NIGHT_VISION
			|| CyberwareItemMetadata.get(stack) == META_HUDJACK
			|| CyberwareItemMetadata.get(stack) == META_TARGETING
			|| CyberwareItemMetadata.get(stack) == META_ZOOM;
	}

	@Override
	public void use(Entity entity, ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_ZOOM)
		{
			if (entity == player)
			{
				if (entity.isShiftKeyDown())
				{
					zoomSettingOn = (zoomSettingOn + 4 - 1) % 4;
				} else
				{
					zoomSettingOn = (zoomSettingOn + 1) % 4;
				}
			}
			return;
		}
		EnableDisableHelper.toggle(stack);
	}

	@Override
	public String getUnlocalizedLabel(ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_ZOOM)
		{
			return "cyberware.gui.active.zoom";
		}
		return EnableDisableHelper.getUnlocalizedLabel(stack);
	}

	private static final float[] f = new float[]{1.0F, 0.0F, 0.0F};

	@Override
	public float[] getColor(ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_ZOOM)
		{
			return null;
		}
		return EnableDisableHelper.isEnabled(stack) ? f : null;
	}

	@Override
	public boolean isActive(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_HUDJACK
			&& EnableDisableHelper.isEnabled(stack);
	}
}
