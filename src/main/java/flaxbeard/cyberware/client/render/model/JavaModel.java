package flaxbeard.cyberware.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// nicked from mekanism
abstract public class JavaModel extends Model
{
	public JavaModel(Function<ResourceLocation, RenderType> renderType)
	{
		super(renderType);
	}

	protected static VertexConsumer getVertexConsumer(@Nonnull MultiBufferSource renderer, @Nonnull RenderType renderType,
													  boolean hasEffect)
	{
		return ItemRenderer.getFoilBufferDirect(renderer, renderType, false, hasEffect);
	}

	protected static void setRotation(ModelPart model, float x, float y, float z)
	{
		model.xRot = x;
		model.yRot = y;
		model.zRot = z;
	}

	protected static void renderPartsToBuffer(List<ModelPart> parts, PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer,
											  int light, int overlayLight, float red, float green, float blue, float alpha)
	{
		for (ModelPart part : parts)
		{
			part.render(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
		}
	}

	protected static List<ModelPart> getRenderableParts(ModelPart root, ModelPartData... modelPartData)
	{
		List<ModelPart> parts = new ArrayList<>(modelPartData.length);
		for (ModelPartData partData : modelPartData)
		{
			parts.add(partData.getFromRoot(root));
		}
		return parts;
	}

	protected static LayerDefinition createLayerDefinition(int textureWidth, int textureHeight, ModelPartData... parts)
	{
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		for (ModelPartData part : parts)
		{
			part.addToDefinition(partdefinition);
		}
		return LayerDefinition.create(meshdefinition, textureWidth, textureHeight);
	}
}
