package flaxbeard.cyberware.common.handler;

import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.systems.RenderSystem;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware.BodyRegionEnum;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb.EnumSide;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.item.ItemCyberlimb;
import flaxbeard.cyberware.common.registry.CWMobEffects;
import flaxbeard.cyberware.common.registry.items.CyberLimbs;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
	private static final Map<Integer, Integer> timesLungs = new HashMap<>();
	private static final UUID idMissingLegSpeedAttribute = UUID.fromString("fe00fdea-5044-11e6-beb8-9e71128cae77");
	private static final HashMultimap<Attribute, AttributeModifier> multimapMissingLegSpeedAttribute;

	static
	{
		multimapMissingLegSpeedAttribute = HashMultimap.create();
		multimapMissingLegSpeedAttribute.put(
			Attributes.MOVEMENT_SPEED,
			new AttributeModifier(idMissingLegSpeedAttribute, "Missing leg speed",
				-100F, AttributeModifier.Operation.ADDITION
			)
		);
	}

	private final Map<Integer, Boolean> last = new HashMap<>();
	private final Map<Integer, Boolean> lastClient = new HashMap<>();

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

		if (!cyberwareUserData.hasEssential(BodyRegionEnum.CRANIUM))
		{
			entityLivingBase.hurt(brainless, Integer.MAX_VALUE);
		}

		if (entityLivingBase instanceof Player
			&& entityLivingBase.tickCount % 20 == 0)
		{
			int tolerance = cyberwareUserData.getTolerance(entityLivingBase);

			if (tolerance <= 0)
			{
				entityLivingBase.hurt(noessence, Integer.MAX_VALUE);
			}

			if (tolerance < CyberwareConfig.INSTANCE.CRITICAL_ESSENCE.get()
				&& entityLivingBase.tickCount % 100 == 0
				&& !entityLivingBase.hasEffect(CWMobEffects.NEUROPOZYNE.get()))
			{
				entityLivingBase.addEffect(new MobEffectInstance(CWMobEffects.REJECTION.get(), 110, 0, true, false));
				entityLivingBase.hurt(lowessence, 2F);
			}

			if (!cyberwareUserData.hasEssential(BodyRegionEnum.EYES))
			{
				entityLivingBase.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40));
			}
		}

		int numMissingLegs = 0;
		int numMissingLegsVisible = 0;

		if (!cyberwareUserData.hasEssential(BodyRegionEnum.LEG, EnumSide.LEFT))
		{
			numMissingLegs++;
			numMissingLegsVisible++;
		}
		if (!cyberwareUserData.hasEssential(BodyRegionEnum.LEG, EnumSide.RIGHT))
		{
			numMissingLegs++;
			numMissingLegsVisible++;
		}

		ItemStack legLeft =
			cyberwareUserData.getCyberware(CyberLimbs.CYBERLEG_LEFT.get().getDefaultInstance());
		if (!legLeft.isEmpty()
			&& !ItemCyberlimb.isPowered(legLeft))
		{
			numMissingLegs++;
		}

		ItemStack legRight =
			cyberwareUserData.getCyberware(CyberLimbs.CYBERLEG_RIGHT.get().getDefaultInstance());
		if (!legRight.isEmpty()
			&& !ItemCyberlimb.isPowered(legRight))
		{
			numMissingLegs++;
		}

		if (entityLivingBase instanceof Player)
		{
			if (numMissingLegsVisible == 2)
			{
				// TODO: cant set these
				//				entityLivingBase.height = 1.8F - (10F / 16F);
				//				((Player) entityLivingBase).eyeHeight =
				//					((Player) entityLivingBase).getEyeHeight() - (10F / 16F);
				//
				//				AABB axisalignedbb = entityLivingBase.getBoundingBox();
				//				entityLivingBase.setBoundingBox(new AABB(
				//					axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
				//					axisalignedbb.minX + entityLivingBase.getBbWidth(), axisalignedbb.minY + entityLivingBase.getBbHeight(),
				//					axisalignedbb.minZ + entityLivingBase.getBbWidth()
				//				));

				if (entityLivingBase.level.isClientSide())
				{
					lastClient.put(entityLivingBase.getId(), true);
				} else
				{
					last.put(entityLivingBase.getId(), true);
				}
			} else if (last(entityLivingBase.level.isClientSide(), entityLivingBase))
			{
				// TODO: cant set these
				//				entityLivingBase.height = 1.8F;
				//				((Player) entityLivingBase).eyeHeight =
				//					((Player) entityLivingBase).getEyeHeight();
				//
				//				AABB axisalignedbb = entityLivingBase.getBoundingBox();
				//				entityLivingBase.setBoundingBox(new AABB(
				//					axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
				//					axisalignedbb.minX + entityLivingBase.getBbWidth(), axisalignedbb.minY + entityLivingBase.getBbHeight(),
				//					axisalignedbb.minZ + entityLivingBase.getBbWidth()
				//				));

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
			entityLivingBase.getAttributes().addTransientAttributeModifiers(multimapMissingLegSpeedAttribute);
		} else if (numMissingLegs >= 1
			|| entityLivingBase.tickCount % 20 == 0)
		{
			entityLivingBase.getAttributes().removeAttributeModifiers(multimapMissingLegSpeedAttribute);
		}

		if (!cyberwareUserData.hasEssential(BodyRegionEnum.HEART))
		{
			entityLivingBase.hurt(heartless, Integer.MAX_VALUE);
		}

		if (!cyberwareUserData.hasEssential(BodyRegionEnum.BONE))
		{
			entityLivingBase.hurt(spineless, Integer.MAX_VALUE);
		}

		if (!cyberwareUserData.hasEssential(BodyRegionEnum.MUSCLE))
		{
			entityLivingBase.hurt(nomuscles, Integer.MAX_VALUE);
		}

		if (!cyberwareUserData.hasEssential(BodyRegionEnum.LUNGS))
		{
			if (getLungsTime(entityLivingBase) >= 20)
			{
				timesLungs.put(entityLivingBase.getId(), entityLivingBase.tickCount);
				entityLivingBase.hurt(DamageSource.DROWN, 2F);
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

			if (!cyberwareUserData.hasEssential(BodyRegionEnum.LEG, EnumSide.LEFT))
			{
				numMissingLegs++;
			}
			if (!cyberwareUserData.hasEssential(BodyRegionEnum.LEG, EnumSide.RIGHT))
			{
				numMissingLegs++;
			}

			ItemStack legLeft =
				cyberwareUserData.getCyberware(CyberLimbs.CYBERLEG_LEFT.get().getDefaultInstance());
			if (!legLeft.isEmpty() && !ItemCyberlimb.isPowered(legLeft))
			{
				numMissingLegs++;
			}

			ItemStack legRight =
				cyberwareUserData.getCyberware(CyberLimbs.CYBERLEG_RIGHT.get().getDefaultInstance());
			if (!legRight.isEmpty() && !ItemCyberlimb.isPowered(legRight))
			{
				numMissingLegs++;
			}

			if (numMissingLegs == 2)
			{
				// TODO: this may be very cursed
				var md = entityLivingBase.getDeltaMovement();
				entityLivingBase.setDeltaMovement(md.x, 0.2F, md.y);
			}
		}
	}

	private int getLungsTime(@Nonnull LivingEntity entityLivingBase)
	{
		Integer timeLungs = timesLungs.computeIfAbsent(entityLivingBase.getId(), k -> entityLivingBase.tickCount);
		return entityLivingBase.tickCount - timeLungs;
	}

	private static final Map<Integer, Integer> mapHunger = new HashMap<>();
	private static final Map<Integer, Float> mapSaturation = new HashMap<>();

	@SubscribeEvent
	public void handleEatFoodTick(LivingEntityUseItemEvent.Tick event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ItemStack stack = event.getItem();

		if (entityLivingBase == null) return;

		if (entityLivingBase instanceof Player entityPlayer
			&& !stack.isEmpty()
			&& stack.getItem().getUseAnimation(stack) == UseAnim.EAT)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);

			if (cyberwareUserData != null && !cyberwareUserData.hasEssential(BodyRegionEnum.LOWER_ORGANS))
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

		if (entityLivingBase instanceof Player entityPlayer
			&& !stack.isEmpty()
			&& stack.getItem().getUseAnimation(stack) == UseAnim.EAT)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);

			if (cyberwareUserData != null && !cyberwareUserData.hasEssential(BodyRegionEnum.LOWER_ORGANS))
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
					FoodData foodStats = entityPlayer.getFoodData();
					foodStats.setSaturation(saturation);
				}
			}
		}
	}

	public static final ResourceLocation BLACK_PX = new ResourceLocation(Cyberware.MODID + ":textures/gui/blackpx" +
		".png");

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void overlayPre(TickEvent.ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().player != null)
		{
			Player entityPlayer = Minecraft.getInstance().player;

			entityPlayer.getAttributes().removeAttributeModifiers(multimapMissingLegSpeedAttribute);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void overlayPre(RenderGuiOverlayEvent.Pre event)
	{
		// TODO ???
		if (false)//event.getOverlay() == ElementType.ALL)
		{
			var poseStack = event.getPoseStack();
			Player entityPlayer = Minecraft.getInstance().player;
			if (entityPlayer == null) return;

			var windowWidth = event.getWindow().getWidth();
			var windowHeight = event.getWindow().getHeight();

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null
				&& !cyberwareUserData.hasEssential(BodyRegionEnum.EYES)
				&& !entityPlayer.isCreative())
			{
				poseStack.pushPose();
				RenderSystem.enableBlend();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F);
				RenderSystem.setShaderTexture(0, BLACK_PX);
				ClientUtils.drawTexturedModalRect(0, 0, 0, 0,
					windowWidth, windowHeight
				);
				poseStack.popPose();
			}

			if (TileEntitySurgery.workingOnPlayer)
			{
				float trans = 1.0F;
				float ticks = TileEntitySurgery.playerProgressTicks + event.getPartialTick();
				if (ticks < 20F)
				{
					trans = ticks / 20F;
				} else if (ticks > 60F)
				{
					trans = (80F - ticks) / 20F;
				}

				RenderSystem.enableBlend();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, trans);
				RenderSystem.setShaderTexture(0, BLACK_PX);
				ClientUtils.drawTexturedModalRect(0, 0, 0, 0,
					windowWidth, windowHeight
				);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.disableBlend();
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
			if (!cyberwareUserData.hasEssential(BodyRegionEnum.SKIN)
				&& (!event.getSource().isBypassMagic()
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

	private void processEvent(Event event, InteractionHand hand, Player entityPlayer, ICyberwareUserData cyberwareUserData)
	{
		HumanoidArm mainHand = entityPlayer.getMainArm();
		HumanoidArm offHand = ((mainHand == HumanoidArm.LEFT) ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
		EnumSide correspondingMainHand = ((mainHand == HumanoidArm.RIGHT) ? EnumSide.RIGHT : EnumSide.LEFT);
		EnumSide correspondingOffHand = ((offHand == HumanoidArm.RIGHT) ? EnumSide.RIGHT : EnumSide.LEFT);

		boolean leftUnpowered = false;
		ItemStack armLeft =
			cyberwareUserData.getCyberware(CyberLimbs.CYBERARM_LEFT.get().getDefaultInstance());
		if (!armLeft.isEmpty() && !ItemCyberlimb.isPowered(armLeft))
		{
			leftUnpowered = true;
		}

		boolean rightUnpowered = false;
		ItemStack armRight =
			cyberwareUserData.getCyberware(CyberLimbs.CYBERARM_RIGHT.get().getDefaultInstance());
		if (!armRight.isEmpty() && !ItemCyberlimb.isPowered(armRight))
		{
			rightUnpowered = true;
		}

		if (hand == InteractionHand.MAIN_HAND && (!cyberwareUserData.hasEssential(BodyRegionEnum.ARM, correspondingMainHand) || leftUnpowered))
		{
			event.setCanceled(true);
		} else if (hand == InteractionHand.OFF_HAND && (!cyberwareUserData.hasEssential(BodyRegionEnum.ARM, correspondingOffHand) || rightUnpowered))
		{
			event.setCanceled(true);
		}
	}
}
