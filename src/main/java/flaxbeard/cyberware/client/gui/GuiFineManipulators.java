package flaxbeard.cyberware.client.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiFineManipulators // extends InventoryEffectRenderer
{
	//	private static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation(Cyberware.MODID + ":textures/gui" +
	//		"/inventory_crafting" +
	//		".png");
	//
	//	public GuiFineManipulators(Player entityPlayer, ContainerFineManipulators fineManipulators)
	//	{
	//		super(fineManipulators);
	//		this.inventorySlots = fineManipulators;
	//	}
	//
	//	@Override
	//	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	//	{
	//		this.fontRenderer.drawString(I18n.get("cyberware.gui.fine_manipulators"), 28, 6, 4210752);
	//		this.fontRenderer.drawString(I18n.get("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	//	}
	//
	//	@Override
	//	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	//	{
	//		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	//		this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
	//		int i = this.guiLeft;
	//		int j = this.guiTop;
	//		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	//		//drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - this.oldMouseX, (float)(j + 75 - 50) - this
	//		// .oldMouseY, this.mc.player, partialTicks);
	//	}
	//
	//	@Override
	//	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	//	{
	//		this.drawDefaultBackground();
	//		super.drawScreen(mouseX, mouseY, partialTicks);
	//
	//		this.renderHoveredToolTip(mouseX, mouseY);
	//	}
	//
	//	@Override
	//	public void initGui()
	//	{
	//		super.initGui();
	//
	//		this.guiLeft = (this.width - this.xSize) / 2;
	//		this.guiLeft += TabRegistry.getPotionOffset();
	//
	//		int cornerX = this.guiLeft;
	//		int cornerY = this.guiTop;
	//
	//		TabRegistry.updateTabValues(cornerX, cornerY, InventoryTabFineManipulators.class);
	//		TabRegistry.addTabsToList(this.buttonList);
	//	}
}