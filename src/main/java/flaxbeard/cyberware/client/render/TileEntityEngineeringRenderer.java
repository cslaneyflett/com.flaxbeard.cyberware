package flaxbeard.cyberware.client.render;

// TODO https://github.com/mekanism/Mekanism/blob/1.19.x/src/main/java/mekanism/client/render/tileentity/RenderPersonalChest.java
public class TileEntityEngineeringRenderer // implements BlockEntityRenderer<TileEntityEngineeringTable>
{
	//	private static ModelEngineering model = new ModelEngineering();
	//	private static final String texture = "cyberware:textures/models/engineering.png";

	//@Override
	//public void render(@Nonnull TileEntityEngineeringTable pBlockEntity, float pPartialTick, @Nonnull PoseStack pPoseStack,
	//				   @Nonnull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay)
	//	@Override
	//	public void render(TileEntityEngineeringTable te, double x, double y, double z, float partialTicks,
	//					   int destroyStage, float alpha)
	//	{
	//		if (te != null)
	//		{
	//			BlockState state = te.getLevel().getBlockState(te.getPos());
	//			if (state.getBlock() == CyberwareContent.engineering)
	//			{
	//				boolean showIcon = true;
	//				GlStateManager.pushMatrix();
	//				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	//				GlStateManager.translate(x + .5, y + .5, z + .5);
	//				GlStateManager.pushMatrix();
	//				float timeElapsed = Math.max(Math.min(
	//					22,
	//					Minecraft.getInstance().player.tickCount + partialTicks - te.clickedTime
	//				), 0);
	//				float amount;
	//				if (timeElapsed < 2)
	//				{
	//					amount = (timeElapsed / 2F);
	//				} else
	//				{
	//					timeElapsed -= 2;
	//
	//					if (timeElapsed < 15)
	//					{
	//						showIcon = false;
	//					}
	//					amount = 1F - (timeElapsed / 20F);
	//				}
	//				GlStateManager.translate(0F, amount * (-6F / 16F), 0F);
	//				ClientUtils.bindTexture(texture);
	//				model.render(null, 0, 0, 0, 0, 0, 0.0625F);
	//				GlStateManager.popMatrix();
	//
	//				ItemStack stack = te.slots.getStackInSlot(0);
	//				if (!stack.isEmpty() && showIcon)
	//				{
	//					Minecraft.getInstance().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	//					GlStateManager.pushMatrix();
	//					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	//
	//
	//					Direction facing = state.getValue(BlockSurgeryChamber.FACING);
	//
	//					switch (facing)
	//					{
	//						case EAST:
	//							GlStateManager.rotate(90F, 0F, 1F, 0F);
	//							break;
	//						case NORTH:
	//							GlStateManager.rotate(180F, 0F, 1F, 0F);
	//							break;
	//						case SOUTH:
	//							break;
	//						case WEST:
	//							GlStateManager.rotate(270F, 0F, 1F, 0F);
	//							break;
	//						default:
	//							break;
	//					}
	//					GlStateManager.translate(0F, 0F, -1.8F / 16F);
	//
	//					GlStateManager.translate(0F, -7.6F / 16F, 0F);
	//					GlStateManager.scale(0.8F, 0.8F, 0.8F);
	//					GlStateManager.rotate(90F, 1F, 0F, 0F);
	//
	//
	//					Minecraft.getInstance().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
	//					GlStateManager.popMatrix();
	//				}
	//
	//
	//				GlStateManager.popMatrix();
	//			}
	//		}
	//	}
}
