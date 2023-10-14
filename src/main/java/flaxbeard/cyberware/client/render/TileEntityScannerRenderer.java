package flaxbeard.cyberware.client.render;

// TODO https://github.com/mekanism/Mekanism/blob/1.19.x/src/main/java/mekanism/client/render/tileentity/RenderPersonalChest.java
public class TileEntityScannerRenderer // extends TileEntitySpecialRenderer<TileEntityScanner>
{
	//	private static ModelScanner model = new ModelScanner();
	//	private static final String texture = "cyberware:textures/models/scanner.png";
	//
	//	@Override
	//	public void render(TileEntityScanner te, double x, double y, double z, float partialTicks, int destroyStage,
	//					   float alpha)
	//	{
	//		if (te != null)
	//		{
	//			GlStateManager.pushMatrix();
	//			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	//			GlStateManager.translate(x + .5, y + .5, z + .5);
	//
	//			BlockState state = te.getLevel().getBlockState(te.getPos());
	//			if (state.getBlock() == CyberwareContent.scanner)
	//			{
	//				ItemStack stack = te.slots.getStackInSlot(0);
	//				if (!stack.isEmpty())
	//				{
	//					Minecraft.getInstance().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	//					GlStateManager.pushMatrix();
	//					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	//					GlStateManager.translate(0F, -1.6F / 16F, 0F);
	//					GlStateManager.scale(.8F, .8F, .8F);
	//					GlStateManager.rotate(90F, 1F, 0F, 0F);
	//					Minecraft.getInstance().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
	//					GlStateManager.popMatrix();
	//				}
	//				ClientUtils.bindTexture(texture);
	//
	//				int difference = Math.abs(te.x - te.lastX);
	//				float timeToTake = difference * 3;
	//				float time = Math.min(timeToTake, te.ticks + partialTicks - te.ticksMove);
	//				float progress = (float) Math.cos((Math.PI / 2) * (1F - (time / timeToTake)));
	//				if (difference == 0)
	//				{
	//					progress = 1.0F;
	//				}
	//				GlStateManager.translate(0F, 0F, ((te.lastX + (te.x - te.lastX) * progress) + 1.5F) * 1F / 16F);
	//
	//				model.render(null, 0, 0, 0, 0, 0, .0625f);
	//
	//				int difference2 = Math.abs(te.z - te.lastZ);
	//				float timeToTake2 = difference2 * 3;
	//				float time2 = Math.max(0, Math.min(timeToTake2, te.ticks + partialTicks - te.ticksMove/* - timeToTake
	//				 */));
	//				float progress2 = (float) Math.cos((Math.PI / 2) * (1F - (time2 / timeToTake2)));
	//				if (difference2 == 0)
	//				{
	//					progress2 = 1.0F;
	//				}
	//				GlStateManager.translate(((te.lastZ + (te.z - te.lastZ) * progress2) + .5F) * 1F / 16F, 0F, 0F);
	//
	//				model.renderScanner(null, 0, 0, 0, 0, 0, .0625f);
	//
	//				if (te.ticks > 0 && (progress2 >= 1F && progress >= 1F) && (((int) (te.ticks + partialTicks)) % 2F == 0))
	//				{
	//					GlStateManager.enableBlend();
	//					model.renderBeam(null, 0, 0, 0, 0, 0, 0.0625F);
	//					GlStateManager.disableBlend();
	//				}
	//
	//
	//				GlStateManager.popMatrix();
	//			}
	//		}
	//	}
}
