package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.items.LowerOrgans;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemLowerOrgansUpgrade extends ItemCyberware implements IMenuItem
{
	public ItemLowerOrgansUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.LOWER_ORGANS);
	}

	private static final Map<UUID, Collection<MobEffectInstance>> mapPotions = new HashMap<>();

	@SubscribeEvent
	public void handleEatFoodTick(LivingEntityUseItemEvent.Tick event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (!(entityLivingBase instanceof Player entityPlayer)) return;

		ItemStack stack = event.getItem();
		if (!stack.isEmpty() && (
			stack.getItem().getUseAnimation(stack) == UseAnim.EAT ||
				stack.getItem().getUseAnimation(stack) == UseAnim.DRINK))
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null
				&& cyberwareUserData.isCyberwareInstalled(LowerOrgans.LIVER_FILTER.get().getDefaultInstance()))
			{
				mapPotions.put(entityPlayer.getUUID(), new ArrayList<>(entityPlayer.getActiveEffects()));
			}
		}
	}

	@SubscribeEvent
	public void handleEatFoodEnd(LivingEntityUseItemEvent.Finish event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (!(entityLivingBase instanceof Player entityPlayer)) return;

		ItemStack stack = event.getItem();
		if (!stack.isEmpty()
			&& (stack.getItem().getUseAnimation(stack) == UseAnim.EAT
			|| stack.getItem().getUseAnimation(stack) == UseAnim.DRINK))
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);

			if (cyberwareUserData != null
				&& cyberwareUserData.isCyberwareInstalled(LowerOrgans.LIVER_FILTER.get().getDefaultInstance()))
			{
				Collection<MobEffectInstance> potionEffectsRemoved = new ArrayList<>(entityPlayer.getActiveEffects());
				for (MobEffectInstance potionEffect : potionEffectsRemoved)
				{
					if (!potionEffect.getEffect().isBeneficial())
					{
						entityPlayer.removeEffect(potionEffect.getEffect());
					}
				}

				Collection<MobEffectInstance> potionEffectsToAdd = mapPotions.get(entityPlayer.getUUID());
				if (potionEffectsToAdd != null)
				{
					for (MobEffectInstance potionEffectToAdd : potionEffectsToAdd)
					{
						for (MobEffectInstance potionEffectRemoved : potionEffectsRemoved)
						{
							if (potionEffectRemoved.getEffect() == potionEffectToAdd.getEffect())
							{
								entityPlayer.addEffect(potionEffectToAdd);
								break;
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase.tickCount % 20 != 0) return;

		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		ItemStack itemStackMetabolicGenerator =
			cyberwareUserData.getCyberware(LowerOrgans.METABOLIC.get().getDefaultInstance());
		if (!itemStackMetabolicGenerator.isEmpty()
			&& EnableDisableHelper.isEnabled(itemStackMetabolicGenerator)
			&& !cyberwareUserData.isAtCapacity(
			itemStackMetabolicGenerator,
			getPowerProduction(itemStackMetabolicGenerator)
		))
		{
			if (entityLivingBase instanceof Player entityPlayer)
			{
				if (entityPlayer.getFoodData().getFoodLevel() > 0
					|| entityPlayer.isCreative())
				{
					int toRemove = getTicksTilRemove(itemStackMetabolicGenerator);
					if (!entityPlayer.isCreative() && toRemove <= 0)
					{
						entityPlayer.getFoodData().addExhaustion(6.0F);
						toRemove = LibConstants.METABOLIC_USES;
					} else if (toRemove > 0)
					{
						toRemove--;
					}
					CyberwareAPI.getCyberwareNBT(itemStackMetabolicGenerator).putInt("toRemove", toRemove);

					cyberwareUserData.addPower(
						getPowerProduction(itemStackMetabolicGenerator),
						itemStackMetabolicGenerator
					);
				}
			} else
			{
				cyberwareUserData.addPower(
					getPowerProduction(itemStackMetabolicGenerator) / 10,
					itemStackMetabolicGenerator
				);
			}
		}

		ItemStack itemStackAdrenalinePump = cyberwareUserData.getCyberware(LowerOrgans.ADRENALINE.get().getDefaultInstance());
		if (!itemStackAdrenalinePump.isEmpty())
		{
			boolean wasBelow = wasBelow(itemStackAdrenalinePump);
			boolean isBelow = false;
			if (entityLivingBase.getMaxHealth() > 8 && entityLivingBase.getHealth() < 8)
			{
				isBelow = true;

				if (!wasBelow
					&& cyberwareUserData.usePower(itemStackAdrenalinePump,
					this.getPowerConsumption(itemStackAdrenalinePump), false
				))
				{
					entityLivingBase.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, true, false));
					entityLivingBase.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0, true, false));
				}
			}

			CyberwareAPI.getCyberwareNBT(itemStackAdrenalinePump).putBoolean("wasBelow", isBelow);
		}
	}

	private int getTicksTilRemove(ItemStack stack)
	{
		CompoundTag data = CyberwareAPI.getCyberwareNBT(stack);
		if (!data.contains("toRemove"))
		{
			data.putInt("toRemove", LibConstants.METABOLIC_USES);
		}
		return data.getInt("toRemove");
	}

	private boolean wasBelow(ItemStack stack)
	{
		CompoundTag data = CyberwareAPI.getCyberwareNBT(stack);
		if (!data.contains("wasBelow"))
		{
			data.putBoolean("wasBelow", false);
		}
		return data.getBoolean("wasBelow");
	}

	@Override
	public int getPowerCapacity(@Nonnull ItemStack wareStack)
	{
		if (wareStack.is(LowerOrgans.METABOLIC.get())) return LibConstants.METABOLIC_PRODUCTION;
		if (wareStack.is(LowerOrgans.BATTERY.get())) return LibConstants.BATTERY_CAPACITY * wareStack.getCount();
		return 0;
	}

	@Override
	public int maximumStackSize(@Nonnull ItemStack stack)
	{
		return stack.is(LowerOrgans.BATTERY.get()) ? 4 : 1;
	}

	@Override
	public int getPowerProduction(@Nonnull ItemStack stack)
	{
		return stack.is(LowerOrgans.METABOLIC.get()) ? LibConstants.METABOLIC_PRODUCTION : 0;
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return stack.is(LowerOrgans.ADRENALINE.get()) ? LibConstants.ADRENALINE_CONSUMPTION : 0;
	}

	@Override
	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		if (stack.is(LowerOrgans.BATTERY.get()))
		{
			switch (stack.getCount())
			{
				case 1:
					return 5;
				case 2:
					return 7;
				case 3:
					return 9;
				case 4:
					return 11;
			}
		}
		return super.getUnmodifiedEssenceCost(stack);
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return stack.is(LowerOrgans.METABOLIC.get());
	}

	@Override
	public void use(Entity entity, ItemStack stack)
	{
		EnableDisableHelper.toggle(stack);
	}

	@Override
	public String getUnlocalizedLabel(ItemStack stack)
	{
		return EnableDisableHelper.getUnlocalizedLabel(stack);
	}

	private static final float[] f = new float[]{1.0F, 0.0F, 0.0F};

	@Override
	public float[] getColor(ItemStack stack)
	{
		return EnableDisableHelper.isEnabled(stack) ? f : null;
	}
}
