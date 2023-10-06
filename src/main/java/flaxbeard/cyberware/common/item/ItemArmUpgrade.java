package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareContent;
import net.minecraft.core.NonNullList;
import net.minecraft.item.ItemBow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemArmUpgrade extends ItemCyberware
{
	public static final int META_BOW = 0;

	public ItemArmUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
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
		// note: we can't use itemStack.getItemUseAction() == EnumAction.BOW because it's use for many other things
		// unrelated to bows
		if (!itemStack.isEmpty()
			&& itemStack.getItem() instanceof ItemBow)
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
