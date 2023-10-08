package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VanillaWares
{
	public static class SpiderEyeWare implements ICyberware
	{
		private static final ItemStack itemStackSpiderEye = new ItemStack(Items.SPIDER_EYE);

		public SpiderEyeWare()
		{
			MinecraftForge.EVENT_BUS.register(this);
		}

		@Override
		public EnumSlot getSlot(ItemStack stack)
		{
			return EnumSlot.EYES;
		}

		@Override
		public int installedStackSize(ItemStack stack)
		{
			return 1;
		}

		@Override
		public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
		{
			return NonNullList.create();
		}

		@Override
		public boolean isIncompatible(ItemStack stack, ItemStack other)
		{
			return CyberwareAPI.getCyberware(other).isEssential(other);
		}

		@Override
		public boolean isEssential(ItemStack stack)
		{
			return true;
		}

		@SubscribeEvent
		public void handleSpiderNightVision(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			if (entityLivingBase.tickCount % 20 != 0) return;

			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
			if (cyberwareUserData.isCyberwareInstalled(itemStackSpiderEye))
			{
				entityLivingBase.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, -53, true,
					false
				));
			} else
			{
				MobEffectInstance effect = entityLivingBase.getEffect(MobEffects.NIGHT_VISION);
				if (effect != null && effect.getAmplifier() == -53)
				{
					entityLivingBase.removeEffect(MobEffects.NIGHT_VISION);
				}
			}
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public void onDrawScreenPost(RenderGameOverlayEvent.Pre event)
		{
			if (event.getType() == ElementType.CROSSHAIRS)
			{
				Player entityPlayer = Minecraft.getInstance().player;
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
				if (cyberwareUserData != null
					&& cyberwareUserData.isCyberwareInstalled(itemStackSpiderEye))
				{
					GlStateManager.translate(0, event.getResolution().getScaledHeight() / 5, 0);
				}
			}
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public void onDrawScreenPost(RenderGameOverlayEvent.Post event)
		{
			if (event.getType() == ElementType.CROSSHAIRS)
			{
				Player entityPlayer = Minecraft.getInstance().player;
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
				if (cyberwareUserData != null
					&& cyberwareUserData.isCyberwareInstalled(itemStackSpiderEye))
				{
					GlStateManager.translate(0, -event.getResolution().getScaledHeight() / 5, 0);
				}
			}
		}

		@SubscribeEvent
		@OnlyIn(Dist.CLIENT)
		public void handleSpiderVision(TickEvent.ClientTickEvent event)
		{
			if (event.phase != TickEvent.Phase.START) return;

			Player entityPlayer = Minecraft.getInstance().player;

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null
				&& cyberwareUserData.isCyberwareInstalled(itemStackSpiderEye))
			{
				if (Minecraft.getInstance().entityRenderer.getShaderGroup() == null)
				{
					Minecraft.getInstance().entityRenderer.loadShader(new ResourceLocation("shaders/post/spider" +
						".json"));
				}
			} else if (entityPlayer != null && !entityPlayer.isSpectator())
			{
				ShaderGroup shaderGroup = Minecraft.getInstance().entityRenderer.getShaderGroup();
				if (shaderGroup != null && shaderGroup.getShaderGroupName().equals("minecraft:shaders/post/spider" +
					".json"))
				{
					Minecraft.getInstance().entityRenderer.stopUseShader();
				}
			}
		}

		@Override
		public List<String> getInfo(ItemStack stack)
		{
			List<String> ret = new ArrayList<>();
			String[] desc = this.getDesciption(stack);
			if (desc.length > 0)
			{
				String format = desc[0];
				if (!format.isEmpty())
				{
					ret.addAll(Arrays.asList(desc));
				}
			}
			return ret;
		}

		private String[] getDesciption(ItemStack stack)
		{
			return I18n.get("cyberware.tooltip.spider_eye").split("\\\\n");
		}

		@Override
		public int getCapacity(ItemStack wareStack)
		{
			return 0;
		}

		@Override
		public void onAdded(LivingEntity entityLivingBase, ItemStack stack)
		{
			// no operation
		}

		@Override
		public void onRemoved(LivingEntity entityLivingBase, ItemStack stack)
		{
			// no operation
		}

		@Override
		public int getEssenceCost(ItemStack stack)
		{
			return 5;
		}

		@Override
		public Quality getQuality(ItemStack stack)
		{
			return null;
		}

		@Override
		public ItemStack setQuality(ItemStack stack, Quality quality)
		{
			return stack;
		}

		@Override
		public boolean canHoldQuality(ItemStack stack, Quality quality)
		{
			return false;
		}
	}
}
