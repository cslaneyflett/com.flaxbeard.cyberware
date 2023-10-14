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

import javax.annotation.Nonnull;
import java.util.List;

public class SkinBoxModel extends JavaModel
{
	public static final ModelLayerLocation SKIN_LAYER = new ModelLayerLocation(new ResourceLocation(Cyberware.MODID, "skin_box"), "main");
	private static final ResourceLocation SKIN_TEXTURE = new ResourceLocation(Cyberware.MODID, "textures/models/skin.png");
	protected static final ModelPartData BOX = new ModelPartData("box", CubeListBuilder.create()
		.texOffs(0, 0)
		.addBox(-6F, -4.5F, -6F, 12, 9, 12),
		PartPose.rotation(0, 0, 0)
	);

	public static LayerDefinition createLayerDefinition()
	{
		return createLayerDefinition(48, 21, BOX);
	}

	private final List<ModelPart> parts;
	private RenderType skinRenderType;

	public SkinBoxModel(EntityModelSet entityModelSet)
	{
		this(entityModelSet.bakeLayer(SKIN_LAYER));
		skinRenderType = renderType(SKIN_TEXTURE);
	}

	protected SkinBoxModel(ModelPart root)
	{
		super(RenderType::entitySolid);
		parts = getRenderableParts(root, BOX);
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
			poseStack, getVertexConsumer(renderer, skinRenderType, hasEffect),
			light, overlayLight, 1, 1, 1, 1
		);
	}
}
