package flaxbeard.cyberware.common.handler;

import com.google.common.collect.HashMultimap;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb.EnumSide;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.item.ItemCyberlimb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumAction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.FoodStats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EssentialsMissingHandler
{
	public static final DamageSource brainless =
		new DamageSource("cyberware.brainless").bypassArmor().bypassMagic().bypassInvul();
	public static final DamageSource heartless =
		new DamageSource("cyberware.heartless").bypassArmor().bypassMagic().bypassInvul();
	public static final DamageSource surgery = new DamageSource("cyberware.surgery").bypassArmor();
	public static final DamageSource spineless =
		new DamageSource("cyberware.spineless").bypassArmor().bypassMagic().bypassInvul();
	public static final DamageSource nomuscles =
		new DamageSource("cyberware.nomuscles").bypassArmor().bypassMagic().bypassInvul();
	public static final DamageSource noessence =
		new DamageSource("cyberware.noessence").bypassArmor().bypassMagic().bypassInvul();
	public static final DamageSource lowessence =
		new DamageSource("cyberware.lowessence").bypassArmor().bypassMagic().bypassInvul();
	public static final EssentialsMissingHandler INSTANCE = new EssentialsMissingHandler();
	private static Map<Integer, Integer> timesLungs = new HashMap<>();
	private static final UUID idMissingLegSpeedAttribute = UUID.fromString("fe00fdea-5044-11e6-beb8-9e71128cae77");
	private static final HashMultimap<String, AttributeModifier> multimapMissingLegSpeedAttribute;

	static
	{
		multimapMissingLegSpeedAttribute = HashMultimap.create();
		multimapMissingLegSpeedAttribute.put(
			Attributes.MOVEMENT_SPEED,
			new AttributeModifier(idMissingLegSpeedAttribute, "Missing leg speed",
				-100F, 0
			)
		);
	}

	private Map<Integer, Boolean> last = new HashMap<>();
	private Map<Integer, Boolean> lastClient = new HashMap<>();

	@SubscribeEvent
	public void triggerCyberwareEvent(LivingEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			CyberwareUpdateEvent cyberwareUpdateEvent = new CyberwareUpdateEvent(entityLivingBase, cyberwareUserData);
			MinecraftForge.EVENT_BUS.post(cyberwareUpdateEvent);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handleMissingEssentials(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		if (entityLivingBase.tickCount % 20 == 0)
		{
			cyberwareUserData.resetBuffer();
		}

		if (!cyberwareUserData.hasEssential(EnumSlot.CRANIUM))
		{
			entityLivingBase.attackEntityFrom(brainless, Integer.MAX_VALUE);
		}

		if (entityLivingBase instanceof Player
			&& entityLivingBase.tickCount % 20 == 0)
		{
			int tolerance = cyberwareUserData.getTolerance(entityLivingBase);

			if (tolerance <= 0)
			{
				entityLivingBase.attackEntityFrom(noessence, Integer.MAX_VALUE);
			}

			if (tolerance < CyberwareConfig.INSTANCE.CRITICAL_ESSENCE.get()
				&& entityLivingBase.tickCount % 100 == 0
				&& !entityLivingBase.hasEffect(CyberwareContent.neuropozyneEffect))
			{
				entityLivingBase.addEffect(new MobEffect(CyberwareContent.rejectionEffect, 110, 0, true, false));
				entityLivingBase.attackEntityFrom(lowessence, 2F);
			}

			if (!cyberwareUserData.hasEssential(EnumSlot.EYES))
			{
				entityLivingBase.addEffect(new MobEffect(MobEffects.BLINDNESS, 40));
			}
		}

		int numMissingLegs = 0;
		int numMissingLegsVisible = 0;

		if (!cyberwareUserData.hasEssential(EnumSlot.LEG, EnumSide.LEFT))
		{
			numMissingLegs++;
			numMissingLegsVisible++;
		}
		if (!cyberwareUserData.hasEssential(EnumSlot.LEG, EnumSide.RIGHT))
		{
			numMissingLegs++;
			numMissingLegsVisible++;
		}

		ItemStack legLeft =
			cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG));
		if (!legLeft.isEmpty()
			&& !ItemCyberlimb.isPowered(legLeft))
		{
			numMissingLegs++;
		}

		ItemStack legRight =
			cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG));
		if (!legRight.isEmpty()
			&& !ItemCyberlimb.isPowered(legRight))
		{
			numMissingLegs++;
		}

		if (entityLivingBase instanceof Player)
		{
			if (numMissingLegsVisible == 2)
			{
				entityLivingBase.height = 1.8F - (10F / 16F);
				((Player) entityLivingBase).eyeHeight =
					((Player) entityLivingBase).getDefaultEyeHeight() - (10F / 16F);
				AABB axisalignedbb = entityLivingBase.getEntityBoundingBox();
				entityLivingBase.setEntityBoundingBox(new AABB(
					axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
					axisalignedbb.minX + entityLivingBase.width, axisalignedbb.minY + entityLivingBase.height,
					axisalignedbb.minZ + entityLivingBase.width
				));

				if (entityLivingBase.level.isClientSide())
				{
					lastClient.put(entityLivingBase.getId(), true);
				} else
				{
					last.put(entityLivingBase.getId(), true);
				}
			} else if (last(entityLivingBase.level.isClientSide(), entityLivingBase))
			{
				entityLivingBase.height = 1.8F;
				((Player) entityLivingBase).eyeHeight = ((Player) entityLivingBase).getDefaultEyeHeight();
				AABB axisalignedbb = entityLivingBase.getEntityBoundingBox();
				entityLivingBase.setEntityBoundingBox(new AABB(
					axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
					axisalignedbb.minX + entityLivingBase.width, axisalignedbb.minY + entityLivingBase.height,
					axisalignedbb.minZ + entityLivingBase.width
				));

				if (entityLivingBase.level.isClientSide())
				{
					lastClient.put(entityLivingBase.getId(), false);
				} else
				{
					last.put(entityLivingBase.getId(), false);
				}
			}
		}

		if (numMissingLegs >= 1
			&& entityLivingBase.isOnGround())
		{
			entityLivingBase.getAttributeMap().applyAttributeModifiers(multimapMissingLegSpeedAttribute);
		} else if (numMissingLegs >= 1
			|| entityLivingBase.tickCount % 20 == 0)
		{
			entityLivingBase.getAttributeMap().removeAttributeModifiers(multimapMissingLegSpeedAttribute);
		}

		if (!cyberwareUserData.hasEssential(EnumSlot.HEART))
		{
			entityLivingBase.attackEntityFrom(heartless, Integer.MAX_VALUE);
		}

		if (!cyberwareUserData.hasEssential(EnumSlot.BONE))
		{
			entityLivingBase.attackEntityFrom(spineless, Integer.MAX_VALUE);
		}

		if (!cyberwareUserData.hasEssential(EnumSlot.MUSCLE))
		{
			entityLivingBase.attackEntityFrom(nomuscles, Integer.MAX_VALUE);
		}

		if (!cyberwareUserData.hasEssential(EnumSlot.LUNGS))
		{
			if (getLungsTime(entityLivingBase) >= 20)
			{
				timesLungs.put(entityLivingBase.getId(), entityLivingBase.tickCount);
				entityLivingBase.attackEntityFrom(DamageSource.DROWN, 2F);
			}
		} else if (entityLivingBase.tickCount % 20 == 0)
		{
			timesLungs.remove(entityLivingBase.getId());
		}
	}

	private boolean last(boolean remote, LivingEntity entityLivingBase)
	{
		if (remote)
		{
			if (!lastClient.containsKey(entityLivingBase.getId()))
			{
				lastClient.put(entityLivingBase.getId(), false);
			}
			return lastClient.get(entityLivingBase.getId());
		} else
		{
			if (!last.containsKey(entityLivingBase.getId()))
			{
				last.put(entityLivingBase.getId(), false);
			}
			return last.get(entityLivingBase.getId());
		}
	}

	@SubscribeEvent
	public void handleJump(LivingJumpEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			int numMissingLegs = 0;

			if (!cyberwareUserData.hasEssential(EnumSlot.LEG, EnumSide.LEFT))
			{
				numMissingLegs++;
			}
			if (!cyberwareUserData.hasEssential(EnumSlot.LEG, EnumSide.RIGHT))
			{
				numMissingLegs++;
			}

			ItemStack legLeft =
				cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG));
			if (!legLeft.isEmpty() && !ItemCyberlimb.isPowered(legLeft))
			{
				numMissingLegs++;
			}

			ItemStack legRight =
				cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG));
			if (!legRight.isEmpty() && !ItemCyberlimb.isPowered(legRight))
			{
				numMissingLegs++;
			}

			if (numMissingLegs == 2)
			{
				entityLivingBase.motionY = 0.2F;
			}
		}
	}

	private int getLungsTime(@Nonnull LivingEntity entityLivingBase)
	{
		Integer timeLungs = timesLungs.computeIfAbsent(entityLivingBase.getId(), k -> entityLivingBase.tickCount);
		return entityLivingBase.tickCount - timeLungs;
	}

	private static Map<Integer, Integer> mapHunger = new HashMap<>();
	private static Map<Integer, Float> mapSaturation = new HashMap<>();

	@SubscribeEvent
	public void handleEatFoodTick(LivingEntityUseItemEvent.Tick event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ItemStack stack = event.getItem();

		if (entityLivingBase == null) return;

		if (entityLivingBase instanceof Player
			&& !stack.isEmpty()
			&& stack.getItem().getItemUseAction(stack) == EnumAction.EAT)
		{
			Player entityPlayer = (Player) entityLivingBase;
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);

			if (cyberwareUserData != null && !cyberwareUserData.hasEssential(EnumSlot.LOWER_ORGANS))
			{
				mapHunger.put(entityPlayer.getId(), entityPlayer.getFoodData().getFoodLevel());
				mapSaturation.put(entityPlayer.getId(), entityPlayer.getFoodData().getSaturationLevel());
				return;
			}
		}

		mapHunger.remove(entityLivingBase.getId());
		mapSaturation.remove(entityLivingBase.getId());
	}

	@SubscribeEvent
	public void handleEatFoodEnd(LivingEntityUseItemEvent.Finish event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ItemStack stack = event.getItem();

		if (entityLivingBase instanceof Player
			&& !stack.isEmpty()
			&& stack.getItem().getItemUseAction(stack) == EnumAction.EAT)
		{
			Player entityPlayer = (Player) entityLivingBase;
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);

			if (cyberwareUserData != null && !cyberwareUserData.hasEssential(EnumSlot.LOWER_ORGANS))
			{
				Integer hunger = mapHunger.get(entityPlayer.getId());
				if (hunger != null)
				{
					entityPlayer.getFoodData().setFoodLevel(hunger);
				}

				Float saturation = mapSaturation.get(entityPlayer.getId());
				if (saturation != null)
				{
					// note: setFoodSaturationLevel() is client side only
					FoodStats foodStats = entityPlayer.getFoodData();
					CompoundTag tagCompound = new CompoundTag();
					foodStats.writeNBT(tagCompound);
					tagCompound.putFloat("foodSaturationLevel", saturation);
					foodStats.readNBT(tagCompound);
				}
			}
		}
	}

	public static final ResourceLocation BLACK_PX = new ResourceLocation(Cyberware.MODID + ":textures/gui/blackpx" +
		".png");

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void overlayPre(ClientTickEvent event)
	{
		if (event.phase == Phase.START
			&& Minecraft.getInstance() != null
			&& Minecraft.getInstance().player != null)
		{
			Player entityPlayer = Minecraft.getInstance().player;

			entityPlayer.getAttributeMap().removeAttributeModifiers(multimapMissingLegSpeedAttribute);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void overlayPre(RenderGameOverlayEvent.Pre event)
	{
		if (event.getType() == ElementType.ALL)
		{
			Player entityPlayer = Minecraft.getInstance().player;
			if (entityPlayer == null) return;

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null
				&& !cyberwareUserData.hasEssential(EnumSlot.EYES)
				&& !entityPlayer.isCreative())
			{
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
				Minecraft.getInstance().getTextureManager().bindTexture(BLACK_PX);
				ClientUtils.drawTexturedModalRect(0, 0, 0, 0, Minecraft.getInstance().displayWidth,
					Minecraft.getInstance().displayHeight
				);
				GlStateManager.popMatrix();
			}

			if (TileEntitySurgery.workingOnPlayer)
			{
				float trans = 1.0F;
				float ticks = TileEntitySurgery.playerProgressTicks + event.getPartialTicks();
				if (ticks < 20F)
				{
					trans = ticks / 20F;
				} else if (ticks > 60F)
				{
					trans = (80F - ticks) / 20F;
				}
				GlStateManager.enableBlend();
				GlStateManager.color(1.0F, 1.0F, 1.0F, trans);
				Minecraft.getInstance().getTextureManager().bindTexture(BLACK_PX);
				ClientUtils.drawTexturedModalRect(0, 0, 0, 0, Minecraft.getInstance().displayWidth,
					Minecraft.getInstance().displayHeight
				);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.disableBlend();
			}
		}
	}

	@SubscribeEvent
	public void handleMissingSkin(LivingHurtEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			if (!cyberwareUserData.hasEssential(EnumSlot.SKIN)
				&& (!event.getSource().isUnblockable()
				|| event.getSource() == DamageSource.FALL))
			{
				event.setAmount(event.getAmount() * 3F);
			}
		}
	}

	@SubscribeEvent
	public void handleEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		LivingEntity entityLivingBase = event.getEntity();

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			processEvent(event, event.getHand(), event.getEntity(), cyberwareUserData);
		}
	}

	@SubscribeEvent
	public void handleLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
	{
		LivingEntity entityLivingBase = event.getEntity();

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			processEvent(event, event.getHand(), event.getEntity(), cyberwareUserData);
		}
	}

	@SubscribeEvent
	public void handleRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		LivingEntity entityLivingBase = event.getEntity();

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			processEvent(event, event.getHand(), event.getEntity(), cyberwareUserData);
		}
	}

	@SubscribeEvent
	public void handleRightClickItem(PlayerInteractEvent.RightClickItem event)
	{
		LivingEntity entityLivingBase = event.getEntity();

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			processEvent(event, event.getHand(), event.getEntity(), cyberwareUserData);
		}
	}

	private void processEvent(Event event, EnumHand hand, Player entityPlayer, ICyberwareUserData cyberwareUserData)
	{
		EnumHandSide mainHand = entityPlayer.getPrimaryHand();
		EnumHandSide offHand = ((mainHand == EnumHandSide.LEFT) ? EnumHandSide.RIGHT : EnumHandSide.LEFT);
		EnumSide correspondingMainHand = ((mainHand == EnumHandSide.RIGHT) ? EnumSide.RIGHT : EnumSide.LEFT);
		EnumSide correspondingOffHand = ((offHand == EnumHandSide.RIGHT) ? EnumSide.RIGHT : EnumSide.LEFT);

		boolean leftUnpowered = false;
		ItemStack armLeft =
			cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM));
		if (!armLeft.isEmpty() && !ItemCyberlimb.isPowered(armLeft))
		{
			leftUnpowered = true;
		}

		boolean rightUnpowered = false;
		ItemStack armRight =
			cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM));
		if (!armRight.isEmpty() && !ItemCyberlimb.isPowered(armRight))
		{
			rightUnpowered = true;
		}

		if (hand == EnumHand.MAIN_HAND && (!cyberwareUserData.hasEssential(EnumSlot.ARM, correspondingMainHand) || leftUnpowered))
		{
			event.setCanceled(true);
		} else if (hand == EnumHand.OFF_HAND && (!cyberwareUserData.hasEssential(EnumSlot.ARM, correspondingOffHand) || rightUnpowered))
		{
			event.setCanceled(true);
		}
	}
}
