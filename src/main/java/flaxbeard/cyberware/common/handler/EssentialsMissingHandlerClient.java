package flaxbeard.cyberware.common.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware.BodyRegionEnum;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb.EnumSide;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.item.ItemCyberlimb;
import flaxbeard.cyberware.common.registry.items.CyberLimbs;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class EssentialsMissingHandlerClient
{
	public static final EssentialsMissingHandlerClient INSTANCE = new EssentialsMissingHandlerClient();
	//	@OnlyIn(Dist.CLIENT)
	//	private static final RenderPlayerCyberware renderSmallArms =
	//		new RenderPlayerCyberware(Minecraft.getInstance().getRenderManager(), true);
	//	@OnlyIn(Dist.CLIENT)
	//	public static final RenderPlayerCyberware renderLargeArms =
	//		new RenderPlayerCyberware(Minecraft.getInstance().getRenderManager(), false);

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleMissingSkin(RenderPlayerEvent.Pre event)
	{
		if (!CyberwareConfig.INSTANCE.ENABLE_CUSTOM_PLAYER_MODEL.get()) return;

		Player entityPlayer = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;

		boolean hasLeftLeg = cyberwareUserData.hasEssential(BodyRegionEnum.LEG, EnumSide.LEFT);
		boolean hasRightLeg = cyberwareUserData.hasEssential(BodyRegionEnum.LEG, EnumSide.RIGHT);
		boolean hasLeftArm = cyberwareUserData.hasEssential(BodyRegionEnum.ARM, EnumSide.LEFT);
		boolean hasRightArm = cyberwareUserData.hasEssential(BodyRegionEnum.ARM, EnumSide.RIGHT);

		var cyberarmLeft = CyberLimbs.CYBERARM_LEFT.get();
		boolean robotLeftArm =
			cyberwareUserData.isCyberwareInstalled(cyberarmLeft.getDefaultInstance());
		var cyberarmRight = CyberLimbs.CYBERARM_RIGHT.get();
		boolean robotRightArm =
			cyberwareUserData.isCyberwareInstalled(cyberarmRight.getDefaultInstance());
		var cyberlegLeft = CyberLimbs.CYBERLEG_LEFT.get();
		boolean robotLeftLeg =
			cyberwareUserData.isCyberwareInstalled(cyberlegLeft.getDefaultInstance());
		var cyberlegRight = CyberLimbs.CYBERLEG_RIGHT.get();
		boolean robotRightLeg =
			cyberwareUserData.isCyberwareInstalled(cyberlegRight.getDefaultInstance());

		var renderPlayer = event.getRenderer();
		var mc = Minecraft.getInstance();

		//		if (!(renderPlayer instanceof RenderPlayerCyberware))
		//		{
		//			var tick = event.getPartialTick();
		//			var pos = event.getEntity().getPosition(tick);
		//
		//			boolean useSmallArms = renderPlayer.smallArms; // TODO: bSlimModel??
		//			RenderPlayerCyberware renderToUse = useSmallArms ? renderSmallArms : renderLargeArms;
		//
		//			boolean hasNoSkin = !cyberwareUserData.hasEssential(BodyRegionEnum.SKIN);
		//			if (hasNoSkin)
		//			{
		//				event.setCanceled(true);
		//				renderToUse.doMuscles = true;
		//			}
		//
		//			boolean hasNoLegs = !hasRightLeg && !hasLeftLeg;
		//			if (hasNoLegs)
		//			{
		//				// Hide pants + shoes
		//				if (!pants.containsKey(entityPlayer.getId()))
		//				{
		//					pants.put(
		//						entityPlayer.getId(),
		//						entityPlayer.getItemBySlot(EquipmentSlot.LEGS)
		//					);
		//					entityPlayer.setItemSlot(
		//						EquipmentSlot.LEGS,
		//						ItemStack.EMPTY
		//					);
		//				}
		//				if (!shoes.containsKey(entityPlayer.getId()))
		//				{
		//					shoes.put(
		//						entityPlayer.getId(),
		//						entityPlayer.getItemBySlot(EquipmentSlot.FEET)
		//					);
		//					entityPlayer.setItemSlot(
		//						EquipmentSlot.FEET,
		//						ItemStack.EMPTY
		//					);
		//				}
		//			}
		//
		//			if (!hasRightLeg || !hasLeftLeg || !hasRightArm || !hasLeftArm
		//				|| robotRightLeg || robotLeftLeg || robotRightArm || robotLeftArm)
		//			{
		//				event.setCanceled(true);
		//
		//
		//				boolean leftArmRusty =
		//					robotLeftArm && cyberarmLeft.getQuality(cyberwareUserData.getCyberware(cyberarmLeft.getDefaultInstance())) == CyberwareAPI.QUALITY_SCAVENGED;
		//				boolean rightArmRusty =
		//					robotRightArm && cyberarmRight.getQuality(cyberwareUserData.getCyberware(cyberarmRight.getDefaultInstance())) == CyberwareAPI.QUALITY_SCAVENGED;
		//				boolean leftLegRusty =
		//					robotLeftLeg && cyberlegLeft.getQuality(cyberwareUserData.getCyberware(cyberlegLeft.getDefaultInstance())) == CyberwareAPI.QUALITY_SCAVENGED;
		//				boolean rightLegRusty =
		//					robotRightLeg && cyberlegRight.getQuality(cyberwareUserData.getCyberware(cyberlegRight.getDefaultInstance())) == CyberwareAPI.QUALITY_SCAVENGED;
		//
		//				// Human/body pass
		//				renderToUse.doRobo = false;
		//				renderToUse.doRusty = false;
		//
		//				renderToUse.doRender((Player) entityPlayer, pos.x(), pos.y() - (hasNoLegs ?
		//						(11F / 16F) :
		//						0),
		//					pos.z(), entityPlayer.rotationYaw, tick
		//				);
		//
		//				// TODO: missing item
		//				if (!cyberwareUserData.isCyberwareInstalled(CyberwareContent.skinUpgrades.getCachedStack(ItemSkinUpgrade.META_SYNTHETIC_SKIN)))
		//				{
		//					var mainModel = renderToUse.getModel();
		//					mainModel.body.visible = false;
		//					mainModel.head.visible = false;
		//
		//					// Manufactured 'ware pass
		//					mainModel.leftArm.visible = robotLeftArm && !leftArmRusty;
		//					mainModel.rightArm.visible = robotRightArm && !rightArmRusty;
		//					mainModel.leftLeg.visible = robotLeftLeg && !leftLegRusty;
		//					mainModel.rightLeg.visible = robotRightLeg && !rightLegRusty;
		//
		//					if (mainModel.leftArm.visible ||
		//						mainModel.rightArm.visible ||
		//						mainModel.leftLeg.visible ||
		//						mainModel.rightLeg.visible)
		//					{
		//						renderToUse.doRobo = true;
		//						renderToUse.doRusty = false;
		//						renderToUse.doRender(entityPlayer, pos.x(),
		//							pos.y() - (hasNoLegs ? (11F / 16F) : 0), pos.z(),
		//							entityPlayer.rotationYaw, tick
		//						);
		//					}
		//
		//					// Rusty 'ware pass
		//					mainModel.leftArm.visible = leftArmRusty;
		//					mainModel.rightArm.visible = rightArmRusty;
		//					mainModel.leftLeg.visible = leftLegRusty;
		//					mainModel.rightLeg.visible = rightLegRusty;
		//
		//					if (mainModel.leftArm.visible ||
		//						mainModel.rightArm.visible ||
		//						mainModel.leftLeg.visible ||
		//						mainModel.rightLeg.visible)
		//					{
		//						renderToUse.doRobo = true;
		//						renderToUse.doRusty = true;
		//						renderToUse.doRender(entityPlayer, pos.x(),
		//							pos.y() - (hasNoLegs ? (11F / 16F) : 0), pos.z(),
		//							entityPlayer.rotationYaw, tick
		//						);
		//					}
		//
		//					// restore defaults
		//					mainModel.body.visible = true;
		//					mainModel.head.visible = true;
		//					mainModel.leftArm.visible = true;
		//					mainModel.rightArm.visible = true;
		//					mainModel.leftLeg.visible = true;
		//					mainModel.rightLeg.visible = true;
		//				}
		//			} else if (hasNoSkin)
		//			{
		//				renderToUse.doRender(entityPlayer, pos.x(), pos.y(), pos.z(),
		//					entityPlayer.rotationYaw, tick
		//				);
		//			}
		//
		//			if (hasNoSkin)
		//			{
		//				renderToUse.doMuscles = false;
		//			}
		//		}

		if (!hasLeftLeg)
		{
			renderPlayer.getModel().leftLeg.visible = false;
		}

		if (!hasRightLeg)
		{
			renderPlayer.getModel().rightLeg.visible = false;
		}

		if (!hasLeftArm)
		{
			renderPlayer.getModel().leftArm.visible = false;

			// Hide the main or offhand item if no arm there
			if (mc.options.mainHand().get() == HumanoidArm.LEFT)
			{
				if (!mainHand.containsKey(entityPlayer.getId())
					&& entityPlayer.getInventory().selected < 10)
				{
					mainHand.put(
						entityPlayer.getId(),
						entityPlayer.getInventory().getItem(
							entityPlayer.getInventory().selected
						)
					);
					entityPlayer.getInventory().setItem(
						entityPlayer.getInventory().selected,
						ItemStack.EMPTY
					);
				}
			} else
			{
				if (!offHand.containsKey(entityPlayer.getId()))
				{
					offHand.put(entityPlayer.getId(), entityPlayer.getItemBySlot(EquipmentSlot.OFFHAND));
					entityPlayer.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
				}
			}
		}

		if (!hasRightArm)
		{
			renderPlayer.getModel().rightArm.visible = false;

			// Hide the main or offhand item if no arm there
			if (mc.options.mainHand().get() == HumanoidArm.RIGHT)
			{
				if (!mainHand.containsKey(entityPlayer.getId())
					&& entityPlayer.getInventory().selected < 10)
				{
					mainHand.put(
						entityPlayer.getId(),
						entityPlayer.getInventory().getItem(
							entityPlayer.getInventory().selected
						)
					);

					entityPlayer.getInventory().setItem(
						entityPlayer.getInventory().selected,
						ItemStack.EMPTY
					);
				}
			} else
			{
				if (!offHand.containsKey(entityPlayer.getId()))
				{
					offHand.put(entityPlayer.getId(), entityPlayer.getItemBySlot(EquipmentSlot.OFFHAND));
					entityPlayer.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
				}
			}
		}
	}

	private static final Map<Integer, ItemStack> mainHand = new HashMap<>();
	private static final Map<Integer, ItemStack> offHand = new HashMap<>();
	private static final Map<Integer, ItemStack> pants = new HashMap<>();
	private static final Map<Integer, ItemStack> shoes = new HashMap<>();

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handleMissingSkin(RenderPlayerEvent.Post event)
	{
		if (!CyberwareConfig.INSTANCE.ENABLE_CUSTOM_PLAYER_MODEL.get()) return;

		event.getRenderer().getModel().leftArm.visible = true;
		event.getRenderer().getModel().rightArm.visible = true;
		event.getRenderer().getModel().leftLeg.visible = true;
		event.getRenderer().getModel().rightLeg.visible = true;

		Player entityPlayer = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData != null)
		{
			if (pants.containsKey(entityPlayer.getId()))
			{
				entityPlayer.setItemSlot(
					EquipmentSlot.LEGS,
					pants.remove(entityPlayer.getId())
				);
			}

			if (shoes.containsKey(entityPlayer.getId()))
			{
				entityPlayer.setItemSlot(
					EquipmentSlot.FEET,
					shoes.remove(entityPlayer.getId())
				);
			}

			if (mainHand.containsKey(entityPlayer.getId()))
			{
				entityPlayer.getInventory().setItem(
					entityPlayer.getInventory().selected,
					mainHand.remove(entityPlayer.getId())
				);
			}

			if (offHand.containsKey(entityPlayer.getId()))
			{
				entityPlayer.setItemSlot(EquipmentSlot.OFFHAND, offHand.remove(entityPlayer.getId()));
			}

			if (!cyberwareUserData.hasEssential(BodyRegionEnum.ARM, EnumSide.LEFT))
			{
				event.getRenderer().getModel().leftArm.visible = false;
			}

			if (!cyberwareUserData.hasEssential(BodyRegionEnum.ARM, EnumSide.RIGHT))
			{
				event.getRenderer().getModel().rightArm.visible = true;
			}
		}
	}

	private static boolean missingArm = false;
	private static boolean missingSecondArm = false;
	private static boolean hasRoboLeft = false;
	private static boolean hasRoboRight = false;
	private static HumanoidArm oldHand;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handleMissingEssentials(LivingEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase != Minecraft.getInstance().player) return;

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			var settings = Minecraft.getInstance().options;
			boolean stillMissingArm = false;
			boolean stillMissingSecondArm = false;

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

			// TODO: missing item
			boolean hasSkin =
				false;//cyberwareUserData.isCyberwareInstalled(CyberwareContent.skinUpgrades.getCachedStack(ItemSkinUpgrade.META_SYNTHETIC_SKIN));
			hasRoboLeft = !armLeft.isEmpty() && !hasSkin;
			hasRoboRight = !armRight.isEmpty() && !hasSkin;
			boolean hasRightArm = cyberwareUserData.hasEssential(BodyRegionEnum.ARM, EnumSide.RIGHT) && !rightUnpowered;
			boolean hasLeftArm = cyberwareUserData.hasEssential(BodyRegionEnum.ARM, EnumSide.LEFT) && !leftUnpowered;

			if (!hasRightArm)
			{
				if (settings.mainHand().get() != HumanoidArm.LEFT)
				{
					// TODO: cant set
					oldHand = settings.mainHand().get();
					settings.mainHand().set(HumanoidArm.LEFT);
					// settings.sendSettingsToServer();
				}

				missingArm = true;
				stillMissingArm = true;

				if (!hasLeftArm)
				{
					missingSecondArm = true;
					stillMissingSecondArm = true;
				}
			} else if (!hasLeftArm)
			{
				if (settings.mainHand().get() != HumanoidArm.RIGHT)
				{
					// TODO: cant set
					oldHand = settings.mainHand().get();
					settings.mainHand().set(HumanoidArm.RIGHT);
					// settings.sendSettingsToServer();
				}

				missingArm = true;
				stillMissingArm = true;
			}

			if (!stillMissingArm)
			{
				missingArm = false;
				if (oldHand != null)
				{
					settings.mainHand().set(oldHand);
					// settings.sendSettingsToServer();
					oldHand = null;
				}
			}

			if (!stillMissingSecondArm)
			{
				missingSecondArm = false;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void handleRenderHand(RenderHandEvent event)
	{
		if (!CyberwareConfig.INSTANCE.ENABLE_CUSTOM_PLAYER_MODEL.get()) // || FMLClientHandler.instance().hasOptifine())
			return;

		var mc = Minecraft.getInstance();

		if (missingArm || missingSecondArm || hasRoboLeft || hasRoboRight)
		{
			float partialTicks = event.getPartialTick();
			var entityRenderer = mc.getEntityRenderDispatcher();
			event.setCanceled(true);

			boolean isSleeping = mc.getCameraEntity() instanceof LivingEntity camera
				&& camera.isSleeping();

			if (mc.options.getCameraType() != CameraType.FIRST_PERSON
				&& !isSleeping
				&& !mc.options.hideGui)
			{
				assert mc.player != null;
				if (!mc.player.isSpectator())
				{
					//					entityRenderer.enableLightmap();
					renderItemInFirstPerson(event.getPoseStack(), partialTicks);
					//					entityRenderer.disableLightmap();
				}
			}
		}
	}

	public static <T> T firstNonNull(@Nullable T first, @Nullable T second)
	{
		return first != null ? first : checkNotNull(second);
	}

	@OnlyIn(Dist.CLIENT)
	private void renderItemInFirstPerson(PoseStack poseStack, float partialTicks)
	{
		//		var mc = Minecraft.getInstance();
		//		ItemRenderer itemRenderer = mc.getItemRenderer();
		//		AbstractClientPlayer abstractClientPlayer = mc.player;
		//		assert abstractClientPlayer != null;
		//
		//		float swingProgress = abstractClientPlayer.getAttackAnim(partialTicks);
		//
		//		InteractionHand enumhand = firstNonNull(abstractClientPlayer.swingingArm, InteractionHand.MAIN_HAND);
		//
		//		float rotationPitch =
		//			abstractClientPlayer.prevRotationPitch + (abstractClientPlayer.rotationPitch - abstractClientPlayer.prevRotationPitch) * partialTicks;
		//		float rotationYaw =
		//			abstractClientPlayer.prevRotationYaw + (abstractClientPlayer.rotationYaw - abstractClientPlayer.prevRotationYaw) * partialTicks;
		//		boolean doRenderMainHand = true;
		//		boolean doRenderOffHand = true;
		//
		//		InteractionHand enumhand1 = abstractClientPlayer.getUsedItemHand();
		//		if (abstractClientPlayer.isHandActive()) // TODO: ??? itemstack empty maybe, wtf is this
		//		{
		//			ItemStack itemstack = abstractClientPlayer.getItemInHand(enumhand1);
		//
		//			if (!itemstack.isEmpty() && itemstack.getItem() == Items.BOW)
		//			// Forge: Data watcher can desync and cause
		//			// this to NPE...
		//			{
		//				doRenderMainHand = enumhand1 == InteractionHand.MAIN_HAND;
		//				doRenderOffHand = !doRenderMainHand;
		//			}
		//		}
		//
		//		rotateArroundXAndY(rotationPitch, rotationYaw);
		//		setLightmap(partialTicks);
		//		rotateArm(partialTicks);
		//		// deprecated in 1.16
		//		// GlStateManager.enableRescaleNormal();
		//
		//		RenderCyberlimbHand.INSTANCE.itemStackMainHand = itemRenderer.itemStackMainHand;
		//		RenderCyberlimbHand.INSTANCE.itemStackOffHand = itemRenderer.itemStackOffHand;
		//
		//		if (doRenderMainHand && !missingSecondArm)
		//		{
		//			float f3 = enumhand == InteractionHand.MAIN_HAND ? swingProgress : 0.0F;
		//			float f5 =
		//				1.0F - (itemRenderer.prevEquippedProgressMainHand + (itemRenderer.equippedProgressMainHand - itemRenderer.prevEquippedProgressMainHand) * partialTicks);
		//			RenderCyberlimbHand.INSTANCE.leftRobot = hasRoboLeft;
		//			RenderCyberlimbHand.INSTANCE.rightRobot = hasRoboRight;
		//			RenderCyberlimbHand.INSTANCE.renderItemInFirstPerson(abstractClientPlayer, partialTicks, rotationPitch,
		//				InteractionHand.MAIN_HAND, f3,
		//				itemRenderer.itemStackMainHand, f5
		//			);
		//		}
		//
		//		if (doRenderOffHand && !missingArm)
		//		{
		//			float f4 = enumhand == InteractionHand.OFF_HAND ? swingProgress : 0.0F;
		//			float f6 =
		//				1.0F - (itemRenderer.prevEquippedProgressOffHand + (itemRenderer.equippedProgressOffHand - itemRenderer.prevEquippedProgressOffHand) * partialTicks);
		//			RenderCyberlimbHand.INSTANCE.leftRobot = hasRoboLeft;
		//			RenderCyberlimbHand.INSTANCE.rightRobot = hasRoboRight;
		//			RenderCyberlimbHand.INSTANCE.renderItemInFirstPerson(abstractClientPlayer, partialTicks, rotationPitch,
		//				InteractionHand.OFF_HAND, f4, itemRenderer.itemStackOffHand,
		//				f6
		//			);
		//		}

		// GlStateManager.disableRescaleNormal();
		//		RenderHelper.disableStandardItemLighting();
	}

	@OnlyIn(Dist.CLIENT)
	private void rotateAroundXAndY(PoseStack poseStack, float angle, float angleY)
	{
		poseStack.pushPose();
		// TODO: dunno
		poseStack.mulPose(Vector3f.XP.rotationDegrees(angle));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(angleY));
		//		poseStack.rotate(angleY, 0.0F, 1.0F, 0.0F);
		//		RenderSystem.enableStandardItemLighting();
		poseStack.popPose();
	}

	@OnlyIn(Dist.CLIENT)
	private void setLightmap(float partialTick)
	{
		var mc = Minecraft.getInstance();
		assert mc.player != null;
		assert mc.level != null;

		Player entityPlayer = mc.player;
		var playerPos = entityPlayer.getPosition(partialTick);
		var pos = new BlockPos(
			playerPos.x,
			playerPos.y + (double) entityPlayer.getEyeHeight(),
			playerPos.z
		);

		int i = mc.level.getLightEmission(pos);
		float f = (float) (i & 65535);
		float f1 = (float) (i >> 16);
		//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
	}

	@OnlyIn(Dist.CLIENT)
	private void rotateArm(float p_187458_1_)
	{
		//		var mc = Minecraft.getInstance();
		//		AbstractClientPlayer entityPlayerSP = mc.player;
		//		assert entityPlayerSP != null;
		//
		//		float f =
		//			entityPlayerSP.prevRenderArmPitch + (entityPlayerSP.renderArmPitch - entityPlayerSP.prevRenderArmPitch) * p_187458_1_;
		//		float f1 =
		//			entityPlayerSP.prevRenderArmYaw + (entityPlayerSP.renderArmYaw - entityPlayerSP.prevRenderArmYaw) * p_187458_1_;
		//		GlStateManager.rotate((entityPlayerSP.rotationPitch - f) * 0.1F, 1.0F, 0.0F, 0.0F);
		//		GlStateManager.rotate((entityPlayerSP.rotationYaw - f1) * 0.1F, 0.0F, 1.0F, 0.0F);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void handleWorldUnload(LevelEvent.Unload event)
	{
		if (missingArm)
		{
			var settings = Minecraft.getInstance().options;
			missingArm = false;

			settings.mainHand().set(oldHand);
		}
	}
}
