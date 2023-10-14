package flaxbeard.cyberware.client.render;

public class RenderPlayerCyberware // extends PlayerRenderer
{
	//	public boolean doMuscles = false;
	//	public boolean doRobo = false;
	//	public boolean doRusty = false;
	//
	//	public RenderPlayerCyberware(EntityRendererProvider.Context pContext, boolean pUseSlimModel)
	//	{
	//		super(pContext, pUseSlimModel);
	//	}
	//
	//
	//	private static final ResourceLocation muscles = new ResourceLocation(Cyberware.MODID, "textures/models" +
	//		"/player_muscles.png");
	//	private static final ResourceLocation robo = new ResourceLocation(Cyberware.MODID, "textures/models/player_robot" +
	//		".png");
	//	private static final ResourceLocation roboRust = new ResourceLocation(Cyberware.MODID, "textures/models" +
	//		"/player_rusty_robot.png");
	//
	//	@Override
	//	public ResourceLocation getEntityTexture(AbstractClientPlayer entity)
	//	{
	//		return doRusty ? roboRust : doRobo ? robo :
	//			doMuscles ? muscles : super.getEntityTexture(entity);
	//	}
	//
	//	public void setMainModel(ModelPlayer modelPlayer)
	//	{
	//		mainModel = modelPlayer;
	//	}
	//
	//	private static ModelClaws claws = new ModelClaws(0.0F);
	//
	//	@Override
	//	public void renderRightHand(@Nonnull PoseStack pMatrixStack, @Nonnull MultiBufferSource pBuffer, int pCombinedLight,
	//								@Nonnull AbstractClientPlayer clientPlayer)
	//	{
	//		RenderSystem.setShaderTexture(0, robo);
	//		super.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, clientPlayer);
	//
	//		if (Minecraft.getInstance().options.mainHand().get() != HumanoidArm.RIGHT
	//			|| !clientPlayer.getMainHandItem().isEmpty())
	//		{
	//			return;
	//		}
	//
	//		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(clientPlayer);
	//		if (cyberwareUserData == null) return;
	//
	//		ItemStack itemStackClaws =
	//			cyberwareUserData.getCyberware(ArmUpgrades.CLAWS.get().getDefaultInstance());
	//		if (!itemStackClaws.isEmpty()
	//			&& cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERARM_RIGHT.get().getDefaultInstance())
	//			&& EnableDisableHelper.isEnabled(itemStackClaws))
	//		{
	//			pMatrixStack.pushPose();
	//
	//			assert Minecraft.getInstance().player != null;
	//			float percent =
	//				(Minecraft.getInstance().player.tickCount + Minecraft.getInstance().getPartialTick() - ItemHandUpgrade.clawsTime) / 4F;
	//			percent = Math.min(1.0F, percent);
	//			percent = Math.max(0F, percent);
	//			percent = (float) Math.sin(percent * Math.PI / 2F);
	//			claws.claw1.rotateAngleY = 0.00F;
	//			claws.claw1.rotateAngleZ = 0.07F;
	//			claws.claw1.rotateAngleX = 0.00F;
	//			claws.claw1.setRotationPoint(-5.0F, -5.0F + (7F * percent), 0.0F);
	//			claws.claw1.render(0.0625F);
	//			pMatrixStack.popPose();
	//		}
	//	}
	//
	//	@Override
	//	public void renderLeftHand(@Nonnull PoseStack pMatrixStack, @Nonnull MultiBufferSource pBuffer, int pCombinedLight,
	//							   @Nonnull AbstractClientPlayer clientPlayer)
	//	{
	//		RenderSystem.setShaderTexture(0, robo);
	//		super.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, clientPlayer);
	//
	//		if (Minecraft.getInstance().options.mainHand().get() != HumanoidArm.LEFT
	//			|| !clientPlayer.getMainHandItem().isEmpty())
	//		{
	//			return;
	//		}
	//
	//		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(clientPlayer);
	//		if (cyberwareUserData == null) return;
	//
	//		ItemStack itemStackClaws =
	//			cyberwareUserData.getCyberware(ArmUpgrades.CLAWS.get().getDefaultInstance());
	//		if (!itemStackClaws.isEmpty()
	//			&& cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERARM_LEFT.get().getDefaultInstance())
	//			&& EnableDisableHelper.isEnabled(itemStackClaws))
	//		{
	//			pMatrixStack.pushPose();
	//
	//			float percent =
	//				((Minecraft.getInstance().player.tickCount + Minecraft.getInstance().getPartialTick() - ItemHandUpgrade.clawsTime) / 4F);
	//			percent = Math.min(1.0F, percent);
	//			percent = Math.max(0F, percent);
	//			percent = (float) Math.sin(percent * Math.PI / 2F);
	//			claws.claw1.rotateAngleY = 0.00F;
	//			claws.claw1.rotateAngleZ = 0.07F;
	//			claws.claw1.rotateAngleX = 0.00F;
	//			claws.claw1.setRotationPoint(-5.0F, -5.0F + (7F * percent), 0.0F);
	//			claws.claw1.render(0.0625F);
	//
	//			pMatrixStack.popPose();
	//		}
	//	}
	//
	//	public void doRender(@Nonnull AbstractClientPlayer entity, double x, double y, double z, float entityYaw,
	//						 float partialTicks)
	//	{
	//		if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderPlayerEvent.Pre(entity, this, partialTicks, x, y, z)))
	//			return;
	//		if (!entity.isLocalPlayer()
	//			|| renderManager.renderViewEntity == entity)
	//		{
	//			double d0 = y;
	//
	//			if (entity.isShiftKeyDown())
	//			{
	//				d0 = y - 0.125D;
	//			}
	//
	//			setModelVisibilities(entity);
	//			GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
	//			doRenderELB(entity, x, d0, z, entityYaw, partialTicks);
	//			GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
	//		}
	//		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
	//			new RenderPlayerEvent.Post(
	//				entity, this, partialTicks, x, y, z
	//				// (Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight
	//			)
	//		);
	//	}
	//
	//	public void doRenderELB(AbstractClientPlayer entity, double x, double y, double z, float entityYaw,
	//							float partialTicks)
	//	{
	//		// if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event
	//		// .RenderLivingEvent.Pre<>(entity, this, partialTicks, x, y, z))) return;
	//		GlStateManager.pushMatrix();
	//		GlStateManager.disableCull();
	//		mainModel.swingProgress = getSwingProgress(entity, partialTicks);
	//		boolean shouldSit =
	//			entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
	//		mainModel.isRiding = shouldSit;
	//		mainModel.isChild = entity.isChild();
	//
	//		ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
	//		ItemStack body = entity.getItemBySlot(EquipmentSlot.CHEST);
	//		ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
	//		ItemStack shoes = entity.getItemBySlot(EquipmentSlot.FEET);
	//		ItemStack heldItem = InventoryPlayer.isHotbar(entity.inventory.currentItem) ?
	//			entity.inventory.mainInventory.get(entity.inventory.currentItem) : null;
	//		ItemStack offHand = entity.inventory.offHandInventory.get(0);
	//
	//		if (doRobo)
	//		{
	//			entity.inventory.armorInventory.set(EquipmentSlot.HEAD.getIndex(), ItemStack.EMPTY);
	//			entity.inventory.armorInventory.set(EquipmentSlot.CHEST.getIndex(), ItemStack.EMPTY);
	//			entity.inventory.armorInventory.set(EquipmentSlot.LEGS.getIndex(), ItemStack.EMPTY);
	//			entity.inventory.armorInventory.set(EquipmentSlot.FEET.getIndex(), ItemStack.EMPTY);
	//			entity.inventory.mainInventory.set(entity.inventory.currentItem, ItemStack.EMPTY);
	//			entity.inventory.offHandInventory.set(0, ItemStack.EMPTY);
	//		}
	//
	//		try
	//		{
	//			float f = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
	//			float f1 = interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
	//			float netHeadYaw = f1 - f;
	//
	//			if (shouldSit
	//				&& entity.getVehicle() instanceof LivingEntity)
	//			{
	//				LivingEntity entitylivingbase = (LivingEntity) entity.getVehicle();
	//				f = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset,
	//					partialTicks
	//				);
	//				netHeadYaw = f1 - f;
	//				float f3 = Mth.wrapDegrees(netHeadYaw);
	//
	//				if (f3 < -85.0F)
	//				{
	//					f3 = -85.0F;
	//				}
	//
	//				if (f3 >= 85.0F)
	//				{
	//					f3 = 85.0F;
	//				}
	//
	//				f = f1 - f3;
	//
	//				if (f3 * f3 > 2500.0F)
	//				{
	//					f += f3 * 0.2F;
	//				}
	//
	//				netHeadYaw = f1 - f;
	//			}
	//
	//			float headPitch =
	//				entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
	//			renderLivingAt(entity, x, y, z);
	//			float ageInTicks = handleRotationFloat(entity, partialTicks);
	//			applyRotations(entity, ageInTicks, f, partialTicks);
	//			float scaleFactor = prepareScale(entity, partialTicks);
	//			float limbSwingAmount = 0.0F;
	//			float limbSwing = 0.0F;
	//
	//			if (!entity.isRiding())
	//			{
	//				limbSwingAmount =
	//					entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
	//				limbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
	//
	//				if (entity.isChild())
	//				{
	//					limbSwing *= 3.0F;
	//				}
	//
	//				if (limbSwingAmount > 1.0F)
	//				{
	//					limbSwingAmount = 1.0F;
	//				}
	//				netHeadYaw = f1 - f;
	//			}
	//
	//			GlStateManager.enableAlpha();
	//			mainModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
	//			mainModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
	//				entity
	//			);
	//
	//			if (renderOutlines)
	//			{
	//				boolean wasTeamColorChanged = setScoreTeamColor(entity);
	//				GlStateManager.enableColorMaterial();
	//				GlStateManager.enableOutlineMode(getTeamColor(entity));
	//
	//				if (!renderMarker)
	//				{
	//					renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	//				}
	//
	//				if (!entity.isSpectator())
	//				{
	//					renderLayers(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
	//						scaleFactor
	//					);
	//				}
	//
	//				GlStateManager.disableOutlineMode();
	//				GlStateManager.disableColorMaterial();
	//
	//				if (wasTeamColorChanged)
	//				{
	//					unsetScoreTeamColor();
	//				}
	//			} else
	//			{
	//				boolean wasBrightnessChanged = setDoRenderBrightness(entity, partialTicks);
	//				renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	//				if (wasBrightnessChanged)
	//				{
	//					unsetBrightness();
	//				}
	//
	//				GlStateManager.depthMask(true);
	//
	//				if (!entity.isSpectator())
	//				{
	//					renderLayers(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
	//						scaleFactor
	//					);
	//				}
	//			}
	//
	//			GlStateManager.disableRescaleNormal();
	//		} catch (Exception exception)
	//		{
	//			Cyberware.logger.error("Disabling custom render until next restart");
	//			CyberwareConfig.INSTANCE.ENABLE_CUSTOM_PLAYER_MODEL.set(false);
	//			exception.printStackTrace();
	//			Cyberware.logger.error(String.format("Exception while rendering %s with %s",
	//				entity, this
	//			));
	//		}
	//
	//		entity.inventory.armorInventory.set(EquipmentSlot.HEAD.getIndex(), head);
	//		entity.inventory.armorInventory.set(EquipmentSlot.CHEST.getIndex(), body);
	//		entity.inventory.armorInventory.set(EquipmentSlot.LEGS.getIndex(), legs);
	//		entity.inventory.armorInventory.set(EquipmentSlot.FEET.getIndex(), shoes);
	//		if (heldItem != null)
	//		{
	//			entity.inventory.mainInventory.set(entity.inventory.currentItem, heldItem);
	//		}
	//		entity.inventory.offHandInventory.set(0, offHand);
	//
	//		//		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	//		GlStateManager.enableTexture2D();
	//		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	//		GlStateManager.enableCull();
	//		GlStateManager.popMatrix();
	//
	//		// From Render.class
	//		if (!renderOutlines)
	//		{
	//			renderName(entity, x, y, z);
	//		}
	//		// net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event
	//		// .RenderLivingEvent.Post<>(entity, this, partialTicks, x, y, z));
	//	}
	//
	//	private void setModelVisibilities(@Nonnull AbstractClientPlayer clientPlayer)
	//	{
	//		ModelPlayer modelplayer = getMainModel();
	//
	//		if (clientPlayer.isSpectator())
	//		{
	//			modelplayer.setVisible(false);
	//			modelplayer.bipedHead.showModel = true;
	//			modelplayer.bipedHeadwear.showModel = true;
	//		} else
	//		{
	//			ItemStack itemstack = clientPlayer.getHeldItemMainhand();
	//			ItemStack itemstack1 = clientPlayer.getHeldItemOffhand();
	//			modelplayer.setVisible(true);
	//			modelplayer.bipedHeadwear.showModel = !modelplayer.bipedHead.isHidden
	//				&& clientPlayer.isWearing(EnumPlayerModelParts.HAT);
	//			modelplayer.bipedBodyWear.showModel = !modelplayer.bipedBody.isHidden
	//				&& clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
	//			modelplayer.bipedLeftLegwear.showModel = !modelplayer.bipedLeftLeg.isHidden
	//				&& clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
	//			modelplayer.bipedRightLegwear.showModel = !modelplayer.bipedRightLeg.isHidden
	//				&& clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
	//			modelplayer.bipedLeftArmwear.showModel = !modelplayer.bipedLeftArm.isHidden
	//				&& clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
	//			modelplayer.bipedRightArmwear.showModel = !modelplayer.bipedRightArm.isHidden
	//				&& clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
	//			modelplayer.isSneak = clientPlayer.isShiftKeyDown();
	//			ModelBiped.ArmPose modelbiped$armpose = ModelBiped.ArmPose.EMPTY;
	//			ModelBiped.ArmPose modelbiped$armpose1 = ModelBiped.ArmPose.EMPTY;
	//
	//			if (!itemstack.isEmpty())
	//			{
	//				modelbiped$armpose = ModelBiped.ArmPose.ITEM;
	//
	//				if (clientPlayer.getItemInUseCount() > 0)
	//				{
	//					EnumAction enumaction = itemstack.getItemUseAction();
	//
	//					if (enumaction == EnumAction.BLOCK)
	//					{
	//						modelbiped$armpose = ModelBiped.ArmPose.BLOCK;
	//					} else if (enumaction == EnumAction.BOW)
	//					{
	//						modelbiped$armpose = ModelBiped.ArmPose.BOW_AND_ARROW;
	//					}
	//				}
	//			}
	//
	//			if (!itemstack1.isEmpty())
	//			{
	//				modelbiped$armpose1 = ModelBiped.ArmPose.ITEM;
	//
	//				if (clientPlayer.getItemInUseCount() > 0)
	//				{
	//					EnumAction enumaction1 = itemstack1.getItemUseAction();
	//
	//					if (enumaction1 == EnumAction.BLOCK)
	//					{
	//						modelbiped$armpose1 = ModelBiped.ArmPose.BLOCK;
	//					}
	//				}
	//			}
	//
	//			if (clientPlayer.getPrimaryHand() == HumanoidArm.RIGHT)
	//			{
	//				modelplayer.rightArmPose = modelbiped$armpose;
	//				modelplayer.leftArmPose = modelbiped$armpose1;
	//			} else
	//			{
	//				modelplayer.rightArmPose = modelbiped$armpose1;
	//				modelplayer.leftArmPose = modelbiped$armpose;
	//			}
	//		}
	//	}
}
