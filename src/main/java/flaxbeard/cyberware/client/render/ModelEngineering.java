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

public class ModelEngineering extends JavaModel
{
	public static final ModelLayerLocation ENGINEERING_LAYER = new ModelLayerLocation(new ResourceLocation(Cyberware.MODID, "engineering_box"), "main");
	private static final ResourceLocation ENGINEERING_TEXTURE = new ResourceLocation(Cyberware.MODID, "textures/models/engineering.png");
	protected static final ModelPartData BAR = new ModelPartData("box", CubeListBuilder.create()
		.texOffs(0, 0)
		.addBox(-1F, 0F, -1F, 2, 7, 2)
	);
	protected static final ModelPartData HEAD = new ModelPartData("box", CubeListBuilder.create()
		.texOffs(0, 0)
		.addBox(-3F, -2F, -3F, 6, 2, 6),
		PartPose.rotation(0, 0, 0),
		BAR
	);

	public static LayerDefinition createLayerDefinition()
	{
		return createLayerDefinition(24, 17, HEAD);
	}

	private final List<ModelPart> parts;
	private RenderType textureRenderType;

	public ModelEngineering(EntityModelSet entityModelSet)
	{
		this(entityModelSet.bakeLayer(ENGINEERING_LAYER));
		textureRenderType = renderType(ENGINEERING_TEXTURE);
	}

	protected ModelEngineering(ModelPart root)
	{
		super(RenderType::entitySolid);
		parts = getRenderableParts(root, HEAD);
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
			poseStack, getVertexConsumer(renderer, textureRenderType, hasEffect),
			light, overlayLight, 1, 1, 1, 1
		);
	}
}
