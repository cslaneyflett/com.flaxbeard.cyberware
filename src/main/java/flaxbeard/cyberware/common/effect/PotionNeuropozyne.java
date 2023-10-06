package flaxbeard.cyberware.common.effect;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import flaxbeard.cyberware.Cyberware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionNeuropozyne extends MobEffectInstance
{
	private static final ResourceLocation resource = new ResourceLocation(Cyberware.MODID + ":textures/gui/potions" +
		".png");
	private int iconIndex;

	public PotionNeuropozyne(String name, boolean isBadEffectIn, int liquidColorIn, int iconIndex)
	{
		super(isBadEffectIn, liquidColorIn);
		setPotionName("cyberware.potion." + name);
		setRegistryName(new ResourceLocation(Cyberware.MODID, name));
		// ForgeRegistries.POTIONS.register(this);
		this.iconIndex = iconIndex;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInventoryEffect(int x, int y, MobEffect effect, Minecraft mc)
	{
		render(x + 6, y + 7, 1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderHUDEffect(int x, int y, MobEffect effect, Minecraft mc, float alpha)
	{
		render(x + 3, y + 3, alpha);
	}

	@OnlyIn(Dist.CLIENT)
	private void render(int x, int y, float alpha)
	{
		Minecraft.getInstance().renderEngine.bindTexture(resource);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buf = tesselator.getBuffer();
		buf.begin(7, DefaultVertexFormat.POSITION_TEX);
		GlStateManager.color(1, 1, 1, alpha);

		int textureX = iconIndex % 8 * 18;
		int textureY = 198 + iconIndex / 8 * 18;

		buf.pos(x, y + 18, 0).tex(textureX * 0.00390625, (textureY + 18) * 0.00390625).endVertex();
		buf.pos(x + 18, y + 18, 0).tex((textureX + 18) * 0.00390625, (textureY + 18) * 0.00390625).endVertex();
		buf.pos(x + 18, y, 0).tex((textureX + 18) * 0.00390625, textureY * 0.00390625).endVertex();
		buf.pos(x, y, 0).tex(textureX * 0.00390625, textureY * 0.00390625).endVertex();

		tesselator.draw();
	}
}
