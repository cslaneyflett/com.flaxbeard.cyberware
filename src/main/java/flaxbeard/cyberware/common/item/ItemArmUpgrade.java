package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemArmUpgrade extends ItemCyberware
{
	public static final int META_BOW = 0;

	public ItemArmUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, ICyberware.EnumSlot.BONE);
	}

	@Override
	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
	{
		NonNullList<NonNullList<ItemStack>> l1 = NonNullList.create();
		NonNullList<ItemStack> l2 = NonNullList.create();
		l2.add(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM));
		l2.add(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM));
		l1.add(l2);
		return l1;
	}

	@SubscribeEvent
	public void useBow(LivingEntityUseItemEvent.Tick event)
	{
		ItemStack itemStack = event.getItem();

		if (!itemStack.isEmpty()
			&& itemStack.is(Tags.Items.TOOLS_BOWS))
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
			if (cyberwareUserData == null) return;

			if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_BOW)))
			{
				event.setDuration(event.getDuration() - 1);
			}
		}
	}
}
