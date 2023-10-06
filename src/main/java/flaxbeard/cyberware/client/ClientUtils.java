package flaxbeard.cyberware.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.client.render.ModelTrenchCoat;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.TriggerActiveAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientUtils
{
	@OnlyIn(Dist.CLIENT)
	public static final ModelTrenchCoat modelTrenchCoat = new ModelTrenchCoat(0.51F);
	private static final float TEXTURE_SCALE = 1.0F / 256;

	public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
	{
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y + height, 0.0F).uv((textureX) * TEXTURE_SCALE, (textureY + height) * TEXTURE_SCALE).endVertex();
		bufferBuilder.vertex(x + width, y + height, 0.0F).uv(
			(textureX + width) * TEXTURE_SCALE,
			(textureY + height) * TEXTURE_SCALE
		).endVertex();
		bufferBuilder.vertex(x + width, y, 0.0F).uv((textureX + width) * TEXTURE_SCALE, (textureY) * TEXTURE_SCALE).endVertex();
		bufferBuilder.vertex(x, y, 0.0F).uv((textureX) * TEXTURE_SCALE, (textureY) * TEXTURE_SCALE).endVertex();
		tesselator.end();
	}

	private static final HashMap<String, ResourceLocation> textures = new HashMap<>();

	public static void bindTexture(String string)
	{
		if (!textures.containsKey(string))
		{
			textures.put(string, new ResourceLocation(string));
			Cyberware.logger.info("Registering new ResourceLocation: " + string);
		}
		Minecraft.getInstance().getTextureManager().bindForSetup(textures.get(string));
	}

	public static void drawHoveringText(Screen gui, List<Component> textLines, int x, int y, Font font)
	{
//		net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(textLines, x, y, gui.width, gui.height, -1,
//			font
//		);
	}

	public static void useActiveItemClient(Entity entity, ItemStack stack)
	{
		CyberwarePacketHandler.INSTANCE.sendToServer(new TriggerActiveAbilityPacket(stack));
		CyberwareAPI.useActiveItem(entity, stack);
	}
}
