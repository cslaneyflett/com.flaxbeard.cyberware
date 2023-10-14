package flaxbeard.cyberware.client.render;

public class TileEntityBeaconLargeRenderer // implements BlockEntityRenderer<TileEntityBeaconPostMaster>
{
	//	private static final ModelBeaconLarge model = new ModelBeaconLarge();
	//	private static final String texture = "cyberware:textures/models/radio.png";
	//	private static final String texture2 = "cyberware:textures/models/radio_base.png";
	//
	//	@Override
	//	public void render(TileEntityBeaconPostMaster pBlockEntity, float pPartialTick, @Nonnull PoseStack pPoseStack,
	//					   @Nonnull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay)
	//	{
	//		var level = pBlockEntity.getLevel();
	//		var pos = pBlockEntity.getBlockPos();
	//		assert level != null;
	//
	//		BlockState state = level.getBlockState(pos);
	//		if (state.is(CWBlocks.BEACON_POST.get()))
	//		{
	//			pPoseStack.pushPose();
	//			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	//			pPoseStack.translate(pos.getX() + 0.5, pos.getY() + 10.5, pos.getZ() + 0.5);
	//
	//			ClientUtils.bindTexture(texture);
	//			model.render(null, 0, 0, 0, 0, 0, 0.0625F);
	//			ClientUtils.bindTexture(texture2);
	//			model.renderBase(null, 0, 0, 0, 0, 0, 0.0625F);
	//			pPoseStack.popPose();
	//		}
	//	}
}
