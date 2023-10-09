package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.registry.items.ArmUpgrades;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

public class ItemArmUpgrade extends ItemCyberware
{
	public ItemArmUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.BONE);
	}

	@Nonnull
	@Override
	public NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack)
	{
		NonNullList<NonNullList<ItemStack>> l1 = NonNullList.create();
		NonNullList<ItemStack> l2 = NonNullList.create();
		l2.add(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM));
		l2.add(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM));
		l1.add(l2);

		return l1;
	}

	public static class EventHandler
	{
		@SubscribeEvent
		public void useBow(LivingEntityUseItemEvent.Tick event)
		{
			ItemStack itemStack = event.getItem();

			if (!itemStack.isEmpty() && (itemStack.is(Tags.Items.TOOLS_BOWS) || itemStack.is(Tags.Items.TOOLS_CROSSBOWS)))
			{
				LivingEntity entityLivingBase = event.getEntity();
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
				if (cyberwareUserData == null) return;

				if (cyberwareUserData.isCyberwareInstalled(ArmUpgrades.BOW.get().getDefaultInstance()))
				{
					event.setDuration(event.getDuration() - 1);
				}
			}
		}
	}
}
