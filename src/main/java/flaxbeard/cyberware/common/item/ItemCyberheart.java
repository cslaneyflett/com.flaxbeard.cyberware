package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.lib.LibConstants;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemCyberheart extends ItemCyberware
{
	public ItemCyberheart(String name, EnumSlot slot)
	{
		super(name, slot);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean isEssential(ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return CyberwareAPI.getCyberware(other).isEssential(other);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase.tickCount % 20 != 0) return;
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		ItemStack itemStackCyberheart = cyberwareUserData.getCyberware(getCachedStack(0));

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
	public int getPowerConsumption(ItemStack stack)
	{
		return LibConstants.HEART_CONSUMPTION;
	}
}
