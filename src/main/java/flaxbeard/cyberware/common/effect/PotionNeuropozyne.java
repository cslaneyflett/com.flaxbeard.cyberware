package flaxbeard.cyberware.common.effect;

import flaxbeard.cyberware.Cyberware;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class PotionNeuropozyne extends MobEffect
{
	private static final ResourceLocation resource = new ResourceLocation(Cyberware.MODID + ":textures/gui/potions" +
		".png");
	private final int iconIndex;

	public PotionNeuropozyne(MobEffectCategory pCategory, int pColor, int iconIndex)
	{
		super(pCategory, pColor);
		this.iconIndex = iconIndex;
	}


	//	@Override
	//	@OnlyIn(Dist.CLIENT)
	//	public void renderInventoryEffect(int x, int y, MobEffect effect, Minecraft mc)
	//	{
	//		render(x + 6, y + 7, 1);
	//	}
	//
	//	@Override
	//	@OnlyIn(Dist.CLIENT)
	//	public void renderHUDEffect(int x, int y, MobEffect effect, Minecraft mc, float alpha)
	//	{
	//		render(x + 3, y + 3, alpha);
	//	}
	//
	//	@OnlyIn(Dist.CLIENT)
	//	private void render(int x, int y, float alpha)
	//	{
	//		Minecraft.getInstance().renderEngine.bindTexture(resource);
	//		Tesselator tesselator = Tesselator.getInstance();
	//		BufferBuilder buf = tesselator.getBuffer();
	//		buf.begin(7, DefaultVertexFormat.POSITION_TEX);
	//		GlStateManager.color(1, 1, 1, alpha);
	//
	//		int textureX = iconIndex % 8 * 18;
	//		int textureY = 198 + iconIndex / 8 * 18;
	//
	//		buf.pos(x, y + 18, 0).tex(textureX * 0.00390625, (textureY + 18) * 0.00390625).endVertex();
	//		buf.pos(x + 18, y + 18, 0).tex((textureX + 18) * 0.00390625, (textureY + 18) * 0.00390625).endVertex();
	//		buf.pos(x + 18, y, 0).tex((textureX + 18) * 0.00390625, textureY * 0.00390625).endVertex();
	//		buf.pos(x, y, 0).tex(textureX * 0.00390625, textureY * 0.00390625).endVertex();
	//
	//		tesselator.draw();
	//	}
}
