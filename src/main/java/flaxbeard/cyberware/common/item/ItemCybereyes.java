package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.items.Eyes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

public class ItemCybereyes extends ItemCyberware
{
	private static boolean isBlind;

	public ItemCybereyes(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.EYES);
	}

	@Override
	public boolean isEssential(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return CyberwareAPI.getCyberware(other).isEssential(other);
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return LibConstants.CYBEREYES_CONSUMPTION;
	}

	public static class EventHandler
	{
		@SubscribeEvent
		public void handleBlindnessImmunity(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			if (!entityLivingBase.hasEffect(MobEffects.BLINDNESS)) return;

			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

			if (cyberwareUserData.isCyberwareInstalled(Eyes.CYBEREYE_BASE.get().getDefaultInstance()))
			{
				entityLivingBase.removeEffect(MobEffects.BLINDNESS);
			}
		}

		@SubscribeEvent(priority = EventPriority.HIGH)
		public void handleMissingEssentials(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			if (entityLivingBase.tickCount % 20 != 0) return;

			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

			var eyesItem = (ItemCybereyes) Eyes.CYBEREYE_BASE.get();
			ItemStack itemStackCybereye = cyberwareUserData.getCyberware(eyesItem.getDefaultInstance());
			if (!itemStackCybereye.isEmpty())
			{
				boolean isPowered = cyberwareUserData.usePower(itemStackCybereye, eyesItem.getPowerConsumption(itemStackCybereye));
				if (entityLivingBase.level.isClientSide()
					&& entityLivingBase == Minecraft.getInstance().player)
				{
					isBlind = !isPowered;
				}
			} else if (entityLivingBase.level.isClientSide()
				&& entityLivingBase == Minecraft.getInstance().player)
			{
				isBlind = false;
			}

			if (isBlind)
			{
				entityLivingBase.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40));
			}
		}

		// TODO

		// @SubscribeEvent
		// @OnlyIn(Dist.CLIENT)
		// public void overlayPre(RenderGameOverlayEvent.Pre event)
		// {
		// 	if (event.getType() == ElementType.ALL)
		// 	{
		// 		Player entityPlayer = Minecraft.getInstance().player;
		//
		// 		if (isBlind && !entityPlayer.isCreative())
		// 		{
		// 			GlStateManager.pushMatrix();
		// 			GlStateManager.enableBlend();
		// 			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
		// 			Minecraft.getInstance().getTextureManager().bindTexture(EssentialsMissingHandler.BLACK_PX);
		// 			ClientUtils.drawTexturedModalRect(0, 0, 0, 0, Minecraft.getInstance().displayWidth,
		// 				Minecraft.getInstance().displayHeight
		// 			);
		// 			GlStateManager.popMatrix();
		// 		}
		// 	}
		// }
	}
}
