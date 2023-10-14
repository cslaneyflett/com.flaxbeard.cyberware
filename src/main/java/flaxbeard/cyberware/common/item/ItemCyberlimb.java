package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.items.CyberLimbs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ItemCyberlimb extends ItemCyberware implements ISidedLimb
{
	@Nonnull
	public final BodyPartEnum bodyPartEnum;

	public ItemCyberlimb(@Nonnull Properties itemProperties, @Nonnull CyberwareProperties cyberwareProperties, @Nonnull BodyPartEnum bodyPartEnum)
	{
		super(itemProperties, cyberwareProperties, bodyPartEnum.slot);
		this.bodyPartEnum = bodyPartEnum;
	}

	@Override
	public boolean isEssential(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		ICyberware ware = CyberwareAPI.getCyberware(other);

		if (ware instanceof ISidedLimb)
		{
			return ware.isEssential(other) && ((ISidedLimb) ware).getSide(other) == this.getSide(stack);
		}
		return false;
	}

	@Nonnull
	@Override
	public EnumSide getSide(@Nonnull ItemStack stack)
	{
		// TODO: this is incorrect, nullibility issue
		assert bodyPartEnum.side != null;
		return bodyPartEnum.side;
	}

	public static boolean isPowered(ItemStack stack)
	{
		CompoundTag data = CyberwareAPI.getCyberwareNBT(stack);
		if (!data.contains("active"))
		{
			data.putBoolean("active", true);
		}
		return data.getBoolean("active");
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return LibConstants.LIMB_CONSUMPTION;
	}

	public static class EventHandler
	{
		public static final EventHandler INSTANCE = new EventHandler();
		private final Set<Integer> didFall = new HashSet<>();

		@SubscribeEvent
		public void handleFallDamage(LivingAttackEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();

			if (entityLivingBase.level.isClientSide()
				&& event.getSource() == DamageSource.FALL
			)
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
				if (cyberwareUserData == null) return;

				if (cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERLEG_LEFT.get().getDefaultInstance())
					&& cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERLEG_RIGHT.get().getDefaultInstance()))
				{
					didFall.add(entityLivingBase.getId());
				}
			}
		}

		@SubscribeEvent
		public void handleSound(PlaySoundEvent event)
		{
			// TODO issue
			//Entity entity = event.getEntity();
			//if (entity instanceof Player
			//	&& event.getSound() == SoundEvents.PLAYER_HURT
			//	&& entity.level.isClientSide()
			//	&& didFall.contains(entity.getId()))
			//{
			//	ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entity);
			//	if (cyberwareUserData == null) return;
			//
			//	int numLegs = 0;
			//	if (cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERLEG_LEFT.get().getDefaultInstance()))
			//	{
			//		numLegs++;
			//	}
			//	if (cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERLEG_RIGHT.get().getDefaultInstance()))
			//	{
			//		numLegs++;
			//	}
			//
			//	if (numLegs > 0)
			//	{
			//		// TODO
			//		//event.setSound(SoundEvents.IRON_GOLEM_HURT);
			//		//event.getSound().setPitch(event.getSound().getPitch() + 1F);
			//		didFall.remove(entity.getId());
			//	}
			//}
		}

		@SubscribeEvent(priority = EventPriority.HIGH)
		public void handleLivingUpdate(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			if (entityLivingBase.tickCount % 20 != 0) return;

			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
			var limbs = new ItemCyberlimb[]{
				CyberLimbs.CYBERARM_LEFT.get(),
				CyberLimbs.CYBERARM_RIGHT.get(),
				CyberLimbs.CYBERLEG_LEFT.get(),
				CyberLimbs.CYBERLEG_RIGHT.get(),
			};
			for (int damage = 0; damage < 4; damage++)
			{
				ItemStack itemStackInstalled = cyberwareUserData.getCyberware(limbs[damage].getDefaultInstance());
				if (!itemStackInstalled.isEmpty())
				{
					boolean isPowered = cyberwareUserData.usePower(
						itemStackInstalled,
						limbs[damage].getPowerConsumption(itemStackInstalled)
					);

					CyberwareAPI.getCyberwareNBT(itemStackInstalled).putBoolean("active", isPowered);
				}
			}
		}
	}
}
