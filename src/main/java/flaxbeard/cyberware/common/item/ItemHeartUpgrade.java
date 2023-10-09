package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.ParticlePacket;
import flaxbeard.cyberware.common.registry.items.Heart;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ItemHeartUpgrade extends ItemCyberware
{
	private static final Map<UUID, Integer> timesPlatelets = new HashMap<>();
	private static final Map<UUID, Boolean> isPlateletWorking = new HashMap<>();
	private static final Map<UUID, Boolean> isStemWorking = new HashMap<>();
	private static final Map<UUID, Integer> timesMedkit = new HashMap<>();
	private static final Map<UUID, Float> damageMedkit = new HashMap<>();

	public ItemHeartUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.HEART);
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return other.getItem() == Heart.CYBERHEART_BASE.get()
			&& (stack.is(Heart.DEFIBRILLATOR.get()) || stack.is(Heart.CARDIOVASCULAR_COUPLER.get()));
	}

	@SubscribeEvent
	public void handleDeath(LivingDeathEvent event)
	{
		if (event.isCanceled()) return;
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;

		ItemStack itemStackInternalDefibrillator =
			cyberwareUserData.getCyberware(Heart.DEFIBRILLATOR.get().getDefaultInstance());
		if (!itemStackInternalDefibrillator.isEmpty())
		{
			if ((!CyberwareAPI.getCyberwareNBT(itemStackInternalDefibrillator).contains("used"))
				&& cyberwareUserData.usePower(itemStackInternalDefibrillator,
				getPowerConsumption(itemStackInternalDefibrillator), false
			))
			{
				if (entityLivingBase instanceof Player)
				{
					NonNullList<ItemStack> items = cyberwareUserData.getInstalledCyberware(BodyRegionEnum.HEART);
					NonNullList<ItemStack> itemsNew = NonNullList.create();
					itemsNew.addAll(items);
					for (int index = 0; index < items.size(); index++)
					{
						ItemStack item = items.get(index);
						if (!item.isEmpty()
							&& item.getItem() == this
							&& item.is(Heart.DEFIBRILLATOR.get()))
						{
							itemsNew.set(index, ItemStack.EMPTY);
							break;
						}
					}
					cyberwareUserData.setInstalledCyberware(entityLivingBase, BodyRegionEnum.HEART, itemsNew);
					cyberwareUserData.updateCapacity();
					if (!entityLivingBase.level.isClientSide())
					{
						CyberwareAPI.updateData(entityLivingBase);
					}
				} else
				{
					itemStackInternalDefibrillator = cyberwareUserData.getCyberware(itemStackInternalDefibrillator);
					CompoundTag tagCompoundCyberware = CyberwareAPI.getCyberwareNBT(itemStackInternalDefibrillator);
					tagCompoundCyberware.putBoolean("used", true);

					CyberwareAPI.updateData(entityLivingBase);
				}

				var pos = entityLivingBase.position();
				CyberwarePacketHandler.INSTANCE.send(
					PacketDistributor.NEAR.with(() ->
						new PacketDistributor.TargetPoint(
							pos.x, pos.y, pos.z,
							20, entityLivingBase.level.dimension()
						)
					),
					new ParticlePacket(
						1,
						pos.add(0, entityLivingBase.getBbHeight() / 2F, 0)
					)
				);

				entityLivingBase.setHealth(entityLivingBase.getMaxHealth() / 3F);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		if (entityLivingBase.tickCount % 20 == 0)
		{
			ItemStack itemStackCardiovascularCoupler =
				cyberwareUserData.getCyberware(Heart.CARDIOVASCULAR_COUPLER.get().getDefaultInstance());
			if (!itemStackCardiovascularCoupler.isEmpty())
			{
				cyberwareUserData.addPower(
					getPowerProduction(itemStackCardiovascularCoupler),
					itemStackCardiovascularCoupler
				);
			}
		}

		ItemStack itemStackStemCellSynthesizer =
			cyberwareUserData.getCyberware(Heart.MEDKIT.get().getDefaultInstance());
		if (entityLivingBase.tickCount % 20 == 0
			&& !itemStackStemCellSynthesizer.isEmpty())
		{
			isStemWorking.put(entityLivingBase.getUUID(), cyberwareUserData.usePower(
				itemStackStemCellSynthesizer,
				getPowerConsumption(itemStackStemCellSynthesizer)
			));
		}

		ItemStack itemStackPlateletDispatcher =
			cyberwareUserData.getCyberware(Heart.PLATELETS.get().getDefaultInstance());
		if (entityLivingBase.tickCount % 20 == 0
			&& !itemStackPlateletDispatcher.isEmpty())
		{
			isPlateletWorking.put(entityLivingBase.getUUID(), cyberwareUserData.usePower(
				itemStackPlateletDispatcher,
				getPowerConsumption(itemStackPlateletDispatcher)
			));
		}

		if (isPlateletWorking(entityLivingBase)
			&& !itemStackPlateletDispatcher.isEmpty())
		{
			if (entityLivingBase.getHealth() >= entityLivingBase.getMaxHealth() * .8F
				&& entityLivingBase.getHealth() != entityLivingBase.getMaxHealth())
			{
				int t = getPlateletTime(entityLivingBase);
				if (t >= 40)
				{
					timesPlatelets.put(entityLivingBase.getUUID(), entityLivingBase.tickCount);
					entityLivingBase.heal(1);
				}
			} else
			{
				timesPlatelets.put(entityLivingBase.getUUID(), entityLivingBase.tickCount);
			}
		} else
		{
			timesPlatelets.remove(entityLivingBase.getUUID());
		}

		if (!itemStackStemCellSynthesizer.isEmpty())
		{
			if (isStemWorking(entityLivingBase))
			{
				int t = getMedkitTime(entityLivingBase);
				if (t >= 100
					&& damageMedkit.get(entityLivingBase.getUUID()) > 0F)
				{
					var pos = entityLivingBase.position();
					CyberwarePacketHandler.INSTANCE.send(
						PacketDistributor.NEAR.with(() ->
							new PacketDistributor.TargetPoint(
								pos.x, pos.y, pos.z,
								20, entityLivingBase.level.dimension()
							)
						),
						new ParticlePacket(
							0,
							pos.add(0, entityLivingBase.getBbHeight() / 2F, 0)
						)
					);

					entityLivingBase.heal(damageMedkit.get(entityLivingBase.getUUID()));
					timesMedkit.put(entityLivingBase.getUUID(), 0);
					damageMedkit.put(entityLivingBase.getUUID(), 0F);
				}
			}
		}
		/*
		else
		{
			if (timesMedkit.containsKey(entityLivingBase.getId()))
			{
				timesMedkit.remove(entityLivingBase);
				damageMedkit.remove(entityLivingBase);
			}
		}
		*/
	}

	private boolean isPlateletWorking(LivingEntity entityLivingBase)
	{
		if (!isPlateletWorking.containsKey(entityLivingBase.getUUID()))
		{
			isPlateletWorking.put(entityLivingBase.getUUID(), false);
			return false;
		}

		return isPlateletWorking.get(entityLivingBase.getUUID());
	}

	private boolean isStemWorking(LivingEntity entityLivingBase)
	{
		if (!isStemWorking.containsKey(entityLivingBase.getUUID()))
		{
			isStemWorking.put(entityLivingBase.getUUID(), false);
			return false;
		}

		return isStemWorking.get(entityLivingBase.getUUID());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handleHurt(LivingHurtEvent event)
	{
		if (event.isCanceled()) return;
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;

		ItemStack itemStackStemCellSynthesizer =
			cyberwareUserData.getCyberware(Heart.MEDKIT.get().getDefaultInstance());
		if (!itemStackStemCellSynthesizer.isEmpty())
		{
			float damageAmount = event.getAmount();
			DamageSource damageSrc = event.getSource();

			damageAmount = applyArmorCalculations(entityLivingBase, damageSrc, damageAmount);
			damageAmount = applyPotionDamageCalculations(entityLivingBase, damageSrc, damageAmount);
			damageAmount = Math.max(damageAmount - entityLivingBase.getAbsorptionAmount(), 0.0F);

			damageMedkit.put(entityLivingBase.getUUID(), damageAmount);
			timesMedkit.put(entityLivingBase.getUUID(), entityLivingBase.tickCount);
		}
	}

	// Stolen from EntityLivingBase
	protected float applyArmorCalculations(LivingEntity entityLivingBase, DamageSource source, float damage)
	{
		if (!source.isBypassMagic())
		{
			damage = CombatRules.getDamageAfterAbsorb(
				damage,
				(float) entityLivingBase.getArmorValue(),
				(float) Objects.requireNonNull(entityLivingBase.getAttribute(Attributes.ARMOR_TOUGHNESS)).getValue()
			);
		}

		return damage;
	}

	// Stolen from EntityLivingBase
	protected float applyPotionDamageCalculations(LivingEntity entityLivingBase, DamageSource source, float damage)
	{
		if (source.isBypassMagic())
		{
			return damage;
		} else
		{
			if (entityLivingBase.hasEffect(MobEffects.DAMAGE_RESISTANCE) && source != DamageSource.OUT_OF_WORLD)
			{
				int i =
					(entityLivingBase.getActiveEffectsMap().get(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
				int j = 25 - i;
				float f = damage * (float) j;
				damage = f / 25.0F;
			}

			if (damage <= 0.0F)
			{
				return 0.0F;
			} else
			{
				int k = EnchantmentHelper.getDamageProtection(
					entityLivingBase.getArmorSlots(),
					source
				);

				if (k > 0)
				{
					damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) k);
				}

				return damage;
			}
		}
	}

	private int getPlateletTime(LivingEntity entityLivingBase)
	{
		if (entityLivingBase != null)
		{
			if (!timesPlatelets.containsKey(entityLivingBase.getUUID()))
			{
				timesPlatelets.put(entityLivingBase.getUUID(), entityLivingBase.tickCount);
				return 0;
			}
			return entityLivingBase.tickCount - timesPlatelets.get(entityLivingBase.getUUID());
		}
		return 0;
	}

	private int getMedkitTime(LivingEntity entityLivingBase)
	{
		if (entityLivingBase != null)
		{
			if (!timesMedkit.containsKey(entityLivingBase.getUUID()))
			{
				timesMedkit.put(entityLivingBase.getUUID(), entityLivingBase.tickCount);
				damageMedkit.put(entityLivingBase.getUUID(), 0F);
				return 0;
			}
			return entityLivingBase.tickCount - timesMedkit.get(entityLivingBase.getUUID());
		}
		return 0;
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		if (stack.is(Heart.DEFIBRILLATOR.get())) return LibConstants.DEFIBRILLATOR_CONSUMPTION;
		if (stack.is(Heart.PLATELETS.get())) return LibConstants.PLATELET_CONSUMPTION;
		if (stack.is(Heart.MEDKIT.get())) return LibConstants.STEMCELL_CONSUMPTION;
		return 0;
	}

	@Override
	public int getPowerCapacity(@Nonnull ItemStack stack)
	{
		if (stack.is(Heart.DEFIBRILLATOR.get())) return LibConstants.DEFIBRILLATOR_CONSUMPTION;
		return 0;
	}

	@Override
	public boolean hasCustomPowerMessage(ItemStack stack)
	{
		return stack.is(Heart.DEFIBRILLATOR.get());
	}

	@Override
	public int getPowerProduction(@Nonnull ItemStack stack)
	{
		if (stack.is(Heart.CARDIOVASCULAR_COUPLER.get())) return LibConstants.COUPLER_PRODUCTION + 1;
		return 0;
	}
}
