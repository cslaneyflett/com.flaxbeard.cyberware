package flaxbeard.cyberware.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.world.entity.Entity;

public class ModelSurgeryChamber extends ModelBase
{
	public ModelRenderer left;
	public ModelRenderer right;

	public ModelSurgeryChamber()
	{
		this.textureWidth = 14;
		this.textureHeight = 29;

		this.left = new ModelRenderer(this, 0, 0);
		this.left.addBox(0F, -22F, -1F, 6, 28, 1);

		this.right = new ModelRenderer(this, 0, 0);
		this.right.addBox(-6F, -22F, -1F, 6, 28, 1);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
					   float headPitch, float scale)
	{
		this.left.render(scale);
	}

	public void renderRight(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
							float headPitch, float scale)
	{
		this.right.render(scale);
	}

	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
