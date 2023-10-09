package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ItemCyberlimb extends ItemCyberware implements ISidedLimb
{
	public static final int META_LEFT_CYBER_ARM = 0;
	public static final int META_RIGHT_CYBER_ARM = 1;
	public static final int META_LEFT_CYBER_LEG = 2;
	public static final int META_RIGHT_CYBER_LEG = 3;

	public ItemCyberlimb(String name, BodyRegionEnum[] slots, String[] subnames)
	{
		super(name, slots, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public ItemCyberlimb(Properties itemProperties, CyberwareProperties cyberwareProperties)
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
		return CyberwareItemMetadata.predicate(stack, (int t) -> t % 2 == 0) ? EnumSide.LEFT : EnumSide.RIGHT;
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

	private Set<Integer> didFall = new HashSet<>();

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

			if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_LEFT_CYBER_LEG))
				&& cyberwareUserData.isCyberwareInstalled(getCachedStack(META_RIGHT_CYBER_LEG)))
			{
				didFall.add(entityLivingBase.getId());
			}
		}
	}

	@SubscribeEvent
	public void handleSound(PlaySoundEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Player
			&& event.getSound() == SoundEvents.PLAYER_HURT
			&& entity.level.isClientSide()
			&& didFall.contains(entity.getId()))
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entity);
			if (cyberwareUserData == null) return;

			int numLegs = 0;
			if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_LEFT_CYBER_LEG)))
			{
				numLegs++;
			}
			if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_RIGHT_CYBER_LEG)))
			{
				numLegs++;
			}

			if (numLegs > 0)
			{
				event.setSound(SoundEvents.IRON_GOLEM_HURT);
				event.setPitch(event.getPitch() + 1F);
				didFall.remove(entity.getId());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase.tickCount % 20 != 0) return;

		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		for (int damage = 0; damage < 4; damage++)
		{
			ItemStack itemStackInstalled = cyberwareUserData.getCyberware(getCachedStack(damage));
			if (!itemStackInstalled.isEmpty())
			{
				boolean isPowered = cyberwareUserData.usePower(
					itemStackInstalled,
					getPowerConsumption(itemStackInstalled)
				);

				CyberwareAPI.getCyberwareNBT(itemStackInstalled).putBoolean("active", isPowered);
			}
		}
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return LibConstants.LIMB_CONSUMPTION;
	}
}
