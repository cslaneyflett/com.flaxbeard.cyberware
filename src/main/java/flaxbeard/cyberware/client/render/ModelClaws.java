package flaxbeard.cyberware.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.client.render.model.JavaModel;
import flaxbeard.cyberware.client.render.model.ModelPartData;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ModelClaws extends JavaModel
{
	public static final ModelLayerLocation CLAWS_LAYER = new ModelLayerLocation(new ResourceLocation(Cyberware.MODID, "skin_box"), "main");
	// TODO: claws have no texture, what do we use??
	private static final ResourceLocation CLAWS_TEXTURE = new ResourceLocation(Cyberware.MODID, "claws_UNFINISHED");
	protected static final ModelPartData CLAW2 = new ModelPartData("claw2", CubeListBuilder.create()
		.texOffs(0, 0)
		.addBox(-2.5F, 10.0F, -0.3F, 1, 7, 1)
	);
	protected static final ModelPartData CLAW3 = new ModelPartData("claw3", CubeListBuilder.create()
		.texOffs(0, 0)
		.addBox(-2.5F, 10.0F, 1.2F, 1, 7, 1)
	);
	protected static final ModelPartData CLAW_MAIN = new ModelPartData("claw_main", CubeListBuilder.create()
		.texOffs(0, 0)
		.addBox(-2.5F, 10.0F, -1.8F, 1, 7, 1),
		PartPose.rotation(-5.0F, 2.0F, 0.0F),
		CLAW2, CLAW3
	);

	public static LayerDefinition createLayerDefinition()
	{
		return createLayerDefinition(64, 64, CLAW_MAIN);
	}

	private final List<ModelPart> parts;
	private RenderType clawsRenderType;

	// TODO: scaling was here before?
	public ModelClaws(EntityModelSet entityModelSet)
	{
		this(entityModelSet.bakeLayer(CLAWS_LAYER));
		clawsRenderType = renderType(CLAWS_TEXTURE);
	}

	protected ModelClaws(ModelPart root)
	{
		super(RenderType::entitySolid);
		parts = getRenderableParts(root, CLAW_MAIN);
	}

	@Override
	public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer,
							   int light, int overlayLight, float red, float green, float blue, float alpha)
	{
		renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
	}

	public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource renderer,
					   int light, int overlayLight, boolean hasEffect)
	{
		renderToBuffer(
			poseStack, getVertexConsumer(renderer, clawsRenderType, hasEffect),
			light, overlayLight, 1, 1, 1, 1
		);
	}


	//	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
	//					   float headPitch, float scale)
	//	{
	//		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
	//		GlStateManager.pushMatrix();
	//
	//		if (entity.isShiftKeyDown())
	//		{
	//			GlStateManager.translate(0.0F, 0.2F, 0.0F);
	//		}
	//
	//		claw1.render(scale);
	//
	//		GlStateManager.popMatrix();
	//	}
}