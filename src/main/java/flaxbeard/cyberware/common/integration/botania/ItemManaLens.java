package flaxbeard.cyberware.common.integration.botania;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.item.ItemCyberware;
import flaxbeard.cyberware.common.misc.NNLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.subtile.ISubTileContainer;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.SubTileEntity;
import vazkii.botania.client.core.handler.BlockHighlightRenderHandler;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.helper.PlayerHelper;
import vazkii.botania.common.item.ItemTwigWand;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.bauble.ItemMonocle;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemManaLens extends ItemCyberware
{
	public static final int META_LENS = 0;
	public static final int META_LINK = 1;
	private static Method BlockHighlightRenderHandler_renderCircle;
	private static Method BlockHighlightRenderHandler_renderRectangle;

	public ItemManaLens(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_LENS)
		{
			return NonNullList.create();
		}

		return NNLUtil.fromArray(new ItemStack[][]{
			new ItemStack[]{CyberwareContent.cybereyes.getCachedStack(0)}});
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return CyberwareItemMetadata.get(stack) == META_LENS && other.getItem() == CyberwareContent.cybereyes;
	}

	private boolean hasLensNotMonocle(Player entityPlayer)
	{
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null)
		{
			return false;
		}

		return (!Botania.proxy.isClientPlayerWearingMonocle()
			&& cyberwareUserData.isCyberwareInstalled(getCachedStack(META_LENS)))
			|| cyberwareUserData.isCyberwareInstalled(getCachedStack(META_LINK));
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onDrawScreenPost(RenderGameOverlayEvent.Post event)
	{
		Player entityPlayer = Minecraft.getInstance().player;
		if (event.getType() == ElementType.ALL
			&& hasLensNotMonocle(entityPlayer))
		{
			ItemMonocle.renderHUD(event.getResolution(), entityPlayer);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onWorldRenderLast(RenderLevelLastEvent event)
	{
		if (BlockHighlightRenderHandler_renderCircle == null)
		{
			BlockHighlightRenderHandler_renderCircle = ReflectionHelper.findMethod(BlockHighlightRenderHandler.class,
				"renderCircle", null,
				BlockPos.class, double.class
			);
			BlockHighlightRenderHandler_renderRectangle =
				ReflectionHelper.findMethod(BlockHighlightRenderHandler.class, "renderRectangle", null, AABB.class
					, boolean.class, Color.class, byte.class);
		}

		// skip if wearing a monocle, or not targeting a block, or targeting an entity
		Minecraft mc = Minecraft.getInstance();
		HitResult rayTraceResult = mc.objectMouseOver;

		if (!hasLensNotMonocle(mc.player)
			|| rayTraceResult == null
			|| rayTraceResult.entityHit != null)
		{
			return;
		}
		BlockPos blockPos = rayTraceResult.getBlockPos();

		// use the Botania's wand bounded tile if it's defined
		ItemStack stackHeld = PlayerHelper.getFirstHeldItem(mc.player, ModItems.twigWand);
		if (!stackHeld.isEmpty()
			&& ItemTwigWand.getBindMode(stackHeld))
		{
			BlockPos blockPosBound = ItemTwigWand.getBoundTile(stackHeld);
			if (blockPosBound.getY() != -1)
			{
				blockPos = blockPosBound;
			}
		}

		// get the radius for that flower
		assert mc.level != null;
		BlockEntity tile = mc.level.getBlockEntity(blockPos);
		if (!(tile instanceof ISubTileContainer))
		{
			return;
		}
		ISubTileContainer container = (ISubTileContainer) tile;
		SubTileEntity subtile = container.getSubTile();
		if (subtile == null)
		{
			return;
		}
		RadiusDescriptor descriptor = subtile.getRadius();
		if (descriptor == null)
		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		// GlStateManager.pushAttrib(GL11.GL_LIGHTING_BIT);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		try
		{
			if (descriptor.isCircle())
			{
				BlockHighlightRenderHandler_renderCircle.invoke(null, descriptor.getSubtileCoords(),
					descriptor.getCircleRadius()
				);
			} else
			{
				BlockHighlightRenderHandler_renderRectangle.invoke(null, descriptor.getAABB(), false, null, (byte) 32);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception)
		{
			exception.printStackTrace();
		}

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		// GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}
}
