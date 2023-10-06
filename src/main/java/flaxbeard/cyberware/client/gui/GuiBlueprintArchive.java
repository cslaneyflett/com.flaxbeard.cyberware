package flaxbeard.cyberware.client.gui;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.item.IBlueprint;
import flaxbeard.cyberware.common.block.tile.TileEntityBlueprintArchive;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiBlueprintArchive extends GuiContainer
{
	/**
	 * The ResourceLocation containing the chest GUI texture.
	 */
	private static final ResourceLocation ARCHIVE_GUI_TEXTURE = new ResourceLocation(Cyberware.MODID + ":textures/gui" +
		"/blueprint_archive.png");
	private Container playerInventory;
	private TileEntityBlueprintArchive archive;
	/**
	 * window height is calculated with these values; the more rows, the heigher
	 */
	private int inventoryRows;

	public GuiBlueprintArchive(Container playerInventory, TileEntityBlueprintArchive archive)
	{
		super(new ContainerBlueprintArchive(playerInventory, archive));
		this.playerInventory = playerInventory;
		this.archive = archive;
		this.allowUserInput = false;
		int i = 222;
		int j = i - 108;
		this.inventoryRows = archive.slots.getSlots() / 9;
		this.ySize = j + this.inventoryRows * 18;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		//this.fontRendererObj.drawString(this.archive.getDisplayName().getUnformattedText(), 8, 6, 4210752);
		//this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize -
		// 96 + 2, 4210752);
		this.fontRenderer.drawString(this.archive.getDisplayName().getUnformattedText(), 8, 6, 4210752);
		this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8,
			this.ySize - 96 + 2, 4210752
		);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 100F);

		for (int h = 0; h < archive.slots.getSlots(); h++)
		{
			ItemStack item = archive.slots.getStackInSlot(h);

			if (item != ItemStack.EMPTY && item.getItem() instanceof IBlueprint)
			{
				IBlueprint blueprint = (IBlueprint) item.getItem();
				ItemStack prod = blueprint.getIconForDisplay(item);
				this.itemRender.renderItemAndEffectIntoGUI(prod, 8 + 18 * (h % 9), 18 + 18 * (h / 9));
			}
		}

		GlStateManager.popMatrix();
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(ARCHIVE_GUI_TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
		this.drawTexturedModalRect(i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
	}
}