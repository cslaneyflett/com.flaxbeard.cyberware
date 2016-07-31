package flaxbeard.cyberware.client.gui;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.EngineeringDestroyPacket;

@SideOnly(Side.CLIENT)
public class GuiEngineeringTable extends GuiContainer
{
	
	private static class SmashButton extends GuiButton
	{
		public SmashButton(int p_i46316_1_, int p_i46316_2_, int p_i46316_3)
		{
			super(p_i46316_1_, p_i46316_2_, p_i46316_3, 21, 21, "");
		}
	

		public void drawButton(Minecraft mc, int mouseX, int mouseY)
		{
			if (this.visible)
			{
				float trans = 0.4F;
				boolean down = Mouse.isButtonDown(0);
				boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			
				
				mc.getTextureManager().bindTexture(ENGINEERING_GUI_TEXTURES);

	
				int i = 39;
				int j = 34;
				if (down && flag)
				{
					i = 176;
					j = 0;
				}
				this.drawTexturedModalRect(this.xPosition, this.yPosition, i, j, 21, 21);
			}
		}
	}
	
	private static final ResourceLocation ENGINEERING_GUI_TEXTURES = new ResourceLocation(Cyberware.MODID + ":textures/gui/engineering.png");

	private InventoryPlayer playerInventory;

	private TileEntityEngineeringTable engineering;
	
	private SmashButton smash;

	public GuiEngineeringTable(InventoryPlayer playerInv, TileEntityEngineeringTable engineering)
	{
		super(new ContainerEngineeringTable(playerInv, engineering));
		this.playerInventory = playerInv;
		this.engineering = engineering;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		String s = this.engineering.getDisplayName().getUnformattedText();
		this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
		
		if (this.isPointInRegion(39, 34, 21, 21, mouseX, mouseY))
		{
			this.drawHoveringText(Arrays.asList(new String[] { I18n.format("cyberware.gui.destroy") } ), mouseX - i, mouseY - j, fontRendererObj);
		}
		
		if (this.isPointInRegion(15, 20, 16, 16, mouseX, mouseY) && engineering.slots.getStackInSlot(0) == null)
		{
			this.drawHoveringText(Arrays.asList(new String[] { I18n.format("cyberware.gui.toDestroy") } ), mouseX - i, mouseY - j, fontRendererObj);
		}
		if (this.isPointInRegion(15, 53, 16, 16, mouseX, mouseY) && engineering.slots.getStackInSlot(1) == null)
		{
			this.drawHoveringText(Arrays.asList(new String[] { I18n.format("cyberware.gui.paper") } ), mouseX - i, mouseY - j, fontRendererObj);
		}
		if (this.isPointInRegion(115, 53, 16, 16, mouseX, mouseY) && engineering.slots.getStackInSlot(8) == null)
		{
			this.drawHoveringText(Arrays.asList(new String[] { I18n.format("cyberware.gui.blueprint") } ), mouseX - i, mouseY - j, fontRendererObj);
		}
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(ENGINEERING_GUI_TEXTURES);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.buttonList.add(smash = new SmashButton(0, i + 39, j + 34));
	}
	
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if (button.id == 0)
		{
			CyberwarePacketHandler.INSTANCE.sendToServer(new EngineeringDestroyPacket(engineering.getPos(), engineering.getWorld().provider.getDimension()));
		}
	}
}
