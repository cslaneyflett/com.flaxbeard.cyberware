package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.registry.items.LegUpgrades;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

public class ItemLegUpgrade extends ItemCyberware
{
	public ItemLegUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.LEG);
	}

	@Nonnull
	@Override
	public NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack)
	{
		return NNLUtil.fromArray(new ItemStack[][]{
			new ItemStack[]{CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG),
				CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG)}});
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return stack.is(LegUpgrades.JUMP_BOOST.get()) ? LibConstants.JUMPBOOST_CONSUMPTION : 0;
	}

	public static class EventHandler
	{
		@SubscribeEvent
		public void playerJumps(LivingEvent.LivingJumpEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
			if (cyberwareUserData == null) return;

			var jumpItem = (ItemLegUpgrade) LegUpgrades.JUMP_BOOST.get();
			ItemStack itemStackJumpBoost = cyberwareUserData.getCyberware(jumpItem.getDefaultInstance());
			if (!itemStackJumpBoost.isEmpty())
			{
				int numLegs = 0;
				if (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG)))
				{
					numLegs++;
				}
				if (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG)))
				{
					numLegs++;
				}
				if (cyberwareUserData.usePower(itemStackJumpBoost, jumpItem.getPowerConsumption(itemStackJumpBoost)))
				{
					if (entityLivingBase.isShiftKeyDown())
					{
						Vec3 vector = entityLivingBase.getViewVector(0.5F);
						double total = Math.abs(vector.z + vector.x);

						// TODO: did i fuck this up? this looks broken
						double jump = 0;
						if (jump >= 1)
						{
							jump = (jump + 2D) / 4D;
						}

						double y = Math.max(vector.y, total);

						entityLivingBase.push((jump + 1) * vector.x * numLegs, (numLegs * ((jump + 1) * y)) / 3F, (jump + 1) * vector.z * numLegs);
					} else
					{
						entityLivingBase.push(0, numLegs * (0.2750000059604645D / 2D), 0);
					}
				}
			}
		}

		@SubscribeEvent
		public void onFallDamage(LivingAttackEvent event)
		{
			if (event.getSource() != DamageSource.FALL)
			{
				return;
			}

			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
			if (cyberwareUserData == null) return;

			if (cyberwareUserData.isCyberwareInstalled(LegUpgrades.FALL_DAMAGE.get().getDefaultInstance())
				&& cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG))
				&& cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG)))
			{
				event.setCanceled(true);
			}
		}
	}
}
