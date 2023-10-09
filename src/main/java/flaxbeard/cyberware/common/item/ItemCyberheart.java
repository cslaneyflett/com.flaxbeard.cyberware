package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.items.Heart;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

public class ItemCyberheart extends ItemCyberware
{
	public ItemCyberheart(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.HEART);
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

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase.tickCount % 20 != 0) return;
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		ItemStack itemStackCyberheart = cyberwareUserData.getCyberware(Heart.CYBERHEART_BASE.get().getDefaultInstance());

		if (!itemStackCyberheart.isEmpty())
		{
			if (!cyberwareUserData.usePower(itemStackCyberheart, getPowerConsumption(itemStackCyberheart)))
			{
				entityLivingBase.hurt(EssentialsMissingHandler.heartless, Integer.MAX_VALUE);
			} else if (entityLivingBase.hasEffect(MobEffects.WEAKNESS))
			{
				entityLivingBase.removeEffect(MobEffects.WEAKNESS);
			}
		}
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return LibConstants.HEART_CONSUMPTION;
	}
}
