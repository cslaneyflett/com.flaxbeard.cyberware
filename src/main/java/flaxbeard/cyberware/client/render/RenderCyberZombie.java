package flaxbeard.cyberware.client.render;

public class RenderCyberZombie // extends ZombieRenderer
{
	//	private static final ResourceLocation ZOMBIE = new ResourceLocation(Cyberware.MODID + ":textures/entity" +
	//		"/cyberzombie.png");
	//	private static final ResourceLocation HIGHLIGHT = new ResourceLocation(Cyberware.MODID + ":textures/entity" +
	//		"/cyberzombie_highlight.png");
	//	private static final ResourceLocation ZOMBIE_BRUTE = new ResourceLocation(Cyberware.MODID + ":textures/entity" +
	//		"/cyberzombie_brute.png");
	//	private static final ResourceLocation HIGHLIGHT_BRUTE = new ResourceLocation(Cyberware.MODID + ":textures/entity" +
	//		"/cyberzombie_brute_highlight" +
	//		".png");
	//
	//	public RenderCyberZombie(EntityRendererProvider.Context pContext, ModelLayerLocation pZombieLayer, ModelLayerLocation pInnerArmor, ModelLayerLocation pOuterArmor)
	//	{
	//		super(pContext, pZombieLayer, pInnerArmor, pOuterArmor);
	//	}
	//
	//
	//	//	// TODO: EntityRendererProvider.Context
	//	//	public RenderCyberZombie(RenderManager renderManagerIn)
	//	//	{
	//	//		super(renderManagerIn);
	//	//		layerRenderers.add(new LayerZombieHighlight(this));
	//	//	}
	//
	//	@OnlyIn(Dist.CLIENT)
	//	public static class LayerZombieHighlight<T extends EntityCyberZombie, M extends EntityModel<T>> extends RenderLayer<T, M>
	//	{
	//		private final RenderCyberZombie czRenderer;
	//
	//		// TODO: dunno
	//		public LayerZombieHighlight(RenderLayerParent<T, M> pRenderer)
	//		{
	//			super(pRenderer);
	//
	//			// this.czRenderer = pRenderer;
	//		}
	//
	//		//public LayerZombieHighlight(RenderCyberZombie spiderRendererIn)
	//		//{
	//		//	this.czRenderer = spiderRendererIn;
	//		//}
	//
	//		@Override
	//		public void render(@Nonnull PoseStack pPoseStack, @Nonnull MultiBufferSource pBuffer, int pPackedLight, @Nonnull T pLivingEntity,
	//						   float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks,
	//						   float pNetHeadYaw, float pHeadPitch)
	//		{
	//			// super.render(pPoseStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTick, pAgeInTicks, pNetHeadYaw, pHeadPitch);
	//
	//			if (pLivingEntity.isBrute())
	//			{
	//				RenderSystem.setShaderTexture(0, HIGHLIGHT_BRUTE);
	//			} else
	//			{
	//				RenderSystem.setShaderTexture(0, HIGHLIGHT);
	//			}
	//
	//			RenderSystem.enableBlend();
	//			//GlStateManager.disableAlpha();
	//			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
	//
	//			if (pLivingEntity.isInvisible())
	//			{
	//				RenderSystem.depthMask(false);
	//			} else
	//			{
	//				RenderSystem.depthMask(true);
	//			}
	//
	//			float i = 61680.0F;
	//			float j = i % 65536.0F;
	//			float k = i / 65536.0F;
	//			RenderSystem.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
	//			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	//			// TODO not sure
	//			// this.czRenderer.getModel().renderToBuffer(
	//			// 	pPoseStack, pBuffer, pPackedLight, pLivingEntity,
	//			// 	pLimbSwing, pLimbSwingAmount, pPartialTick, pAgeInTicks, pNetHeadYaw, pHeadPitch
	//			// );
	//			// this.czRenderer.getModel().renderToBuffer(
	//			// 	pPoseStack, pBuffer.getBuffer(RenderType.solid()), pLivingEntity,
	//			// 	pPackedLight,
	//			// );
	//			// renderToBuffer(
	//			// 	PoseStack, VertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha
	//			// );
	//			//i = pLivingEntity.getBrightnessForRender(partialTicks);
	//			i = pLivingEntity.getBrightnessForRender();
	//			j = i % 65536;
	//			k = i / 65536;
	//			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
	//			//this.czRenderer.setLightmap(pLivingEntity, partialTicks);
	//			this.czRenderer.setLightmap(pLivingEntity);
	//			RenderSystem.disableBlend();
	//			// RenderSystem.enableAlpha();
	//		}
	//
	//		public boolean shouldCombineTextures()
	//		{
	//			return false;
	//		}
	//	}
	//
	//	@Nonnull
	//	@Override
	//	public ResourceLocation getTextureLocation(@Nonnull Zombie pEntity)
	//	{
	//		EntityCyberZombie cz = (EntityCyberZombie) pEntity;
	//		if (cz.isBrute())
	//		{
	//			return ZOMBIE_BRUTE;
	//		}
	//		return ZOMBIE;
	//	}
	//
	//	@Override
	//	protected void preRenderCallback(@Nonnull final Zombie zombie, float partialTickTime)
	//	{
	//		EntityCyberZombie cz = (EntityCyberZombie) zombie;
	//		if (cz.getBbHeight() == (1.95F * 1.2F))
	//		{
	//			GlStateManager.scale(1.2F, 1.2F, 1.2F);
	//		}
	//	}
}