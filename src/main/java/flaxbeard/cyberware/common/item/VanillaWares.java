package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
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

		@Nonnull
		@Override
		public BodyRegionEnum getSlot(@Nonnull ItemStack stack)
		{
			return BodyRegionEnum.EYES;
		}

		@Override
		public int maximumStackSize(@Nonnull ItemStack stack)
		{
			return 1;
		}

		@Nonnull
		@Override
		public NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack)
		{
			return NonNullList.create();
		}

		@Override
		public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
		{
			return CyberwareAPI.getCyberware(other).isEssential(other);
		}

		@Override
		public boolean isEssential(@Nonnull ItemStack stack)
		{
			return true;
		}

		@Nonnull
		@Override
		public List<String> getInfo(@Nonnull ItemStack stack)
		{
			List<String> ret = new ArrayList<>();
			String[] desc = this.getDescription(stack);
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

		private String[] getDescription(ItemStack stack)
		{
			return I18n.get("cyberware.tooltip.spider_eye").split("\\\\n");
		}

		@Override
		public int getPowerConsumption(@Nonnull ItemStack stack)
		{
			return 0;
		}

		@Override
		public int getPowerProduction(@Nonnull ItemStack stack)
		{
			return 0;
		}

		@Override
		public int getPowerCapacity(@Nonnull ItemStack wareStack)
		{
			return 0;
		}

		@Override
		public void onAdded(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
		{
			// no operation
		}

		@Override
		public void onRemoved(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
		{
			// no operation
		}

		@Override
		public int getEssenceCost(@Nonnull ItemStack stack)
		{
			return 5;
		}

		@Nonnull
		@Override
		public Quality getQuality(@Nonnull ItemStack stack)
		{
			return null;
		}

		@Nonnull
		@Override
		public ItemStack setQuality(@Nonnull ItemStack stack, @Nonnull Quality quality)
		{
			return stack;
		}

		@Override
		public boolean canHoldQuality(@Nonnull ItemStack stack, @Nonnull Quality quality)
		{
			return false;
		}

		public static class EventHandler
		{
			public static final EventHandler INSTANCE = new EventHandler();

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
			public void onDrawScreenPre(RenderGuiOverlayEvent.Pre event)
			{
				if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type())
				{
					Player entityPlayer = Minecraft.getInstance().player;
					ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
					if (cyberwareUserData != null
						&& cyberwareUserData.isCyberwareInstalled(itemStackSpiderEye))
					{
						event.getPoseStack().translate(0, event.getWindow().getGuiScaledHeight() / 5.0F, 0);
					}
				}
			}

			@OnlyIn(Dist.CLIENT)
			@SubscribeEvent
			public void onDrawScreenPost(RenderGuiOverlayEvent.Post event)
			{
				if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type())
				{
					Player entityPlayer = Minecraft.getInstance().player;
					ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
					if (cyberwareUserData != null
						&& cyberwareUserData.isCyberwareInstalled(itemStackSpiderEye))
					{
						event.getPoseStack().translate(0, -event.getWindow().getGuiScaledHeight() / 5.0F, 0);
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
					// TODO
					//					if (Minecraft.getInstance().entityRenderer.getShaderGroup() == null)
					//					{
					//						Minecraft.getInstance().entityRenderer.loadShader(new ResourceLocation("shaders/post/spider" +
					//							".json"));
					//					}
				} else if (entityPlayer != null && !entityPlayer.isSpectator())
				{
					// TODO
					//					ShaderGroup shaderGroup = Minecraft.getInstance().entityRenderer.getShaderGroup();
					//					if (shaderGroup != null && shaderGroup.getShaderGroupName().equals("minecraft:shaders/post/spider" +
					//						".json"))
					//					{
					//						Minecraft.getInstance().entityRenderer.stopUseShader();
					//					}
				}
			}
		}
	}
}
