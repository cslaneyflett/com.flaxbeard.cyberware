package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.ArmorClass;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.enchantment.EnchantmentThorns;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemSkinUpgrade extends ItemCyberware
{
	public static final int META_SOLARSKIN = 0;
	public static final int META_SUBDERMAL_SPIKES = 1;
	public static final int META_SYNTHETIC_SKIN = 2;
	public static final int META_IMMUNOSUPPRESSANT = 3;

	public ItemSkinUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase.tickCount % 20 != 0) return;

		float lightFactor = getLightFactor(entityLivingBase);
		if (lightFactor <= 0.0F) return;

		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		ItemStack itemStackSolarskin = cyberwareUserData.getCyberware(getCachedStack(META_SOLARSKIN));
		if (!itemStackSolarskin.isEmpty())
		{
			int power = Math.max(0, Math.round(getPowerProduction(itemStackSolarskin) * lightFactor));
			cyberwareUserData.addPower(power, itemStackSolarskin);
		}
	}

	private float getLightFactor(LivingEntity entityLivingBase)
	{
		Level world = entityLivingBase.level;
		// world must have a sun
		if (!entityLivingBase.level.provider.hasSkyLight()) return 0.0F;
		// current position can see the sun
		BlockPos pos = new BlockPos(entityLivingBase.posX, entityLivingBase.posY + entityLivingBase.height,
			entityLivingBase.posZ
		);
		if (!entityLivingBase.level.canBlockSeeSky(pos)) return 0.0F;

		// sun isn't shaded
		int lightSky = world.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(pos);
		// note: world.getSkylightSubtracted() is server side only
		if (lightSky < 15) return 0.0F;

		// it's day time (see Vanilla daylight sensor)
		float celestialAngleRadians = world.getCelestialAngleRadians(1.0F);
		float offsetRadians = celestialAngleRadians < (float) Math.PI ? 0.0F : ((float) Math.PI * 2.0F);
		float celestialAngleRadians2 = celestialAngleRadians + (offsetRadians - celestialAngleRadians) * 0.2F;
		return Mth.cos(celestialAngleRadians2);
	}

	private static Map<UUID, Collection<LastPotionEffect>> mapEntityLastPotionEffects = new HashMap<>();

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleMissingEssentials(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		ItemStack itemStackImmunosuppressant = cyberwareUserData.getCyberware(getCachedStack(META_IMMUNOSUPPRESSANT));
		if (!itemStackImmunosuppressant.isEmpty())
		{
			// consume power every 1 s, apply damage every 5 s unless power or Neuropozyne is active
			if (entityLivingBase instanceof Player
				&& entityLivingBase.tickCount % 20 == 0)
			{
				boolean isPowered = cyberwareUserData.usePower(
					itemStackImmunosuppressant,
					getPowerConsumption(itemStackImmunosuppressant)
				);

				if (!isPowered
					&& entityLivingBase.tickCount % 100 == 0
					&& !entityLivingBase.hasEffect(CyberwareContent.neuropozyneEffect))
				{
					entityLivingBase.hurt(EssentialsMissingHandler.lowessence, 2.0F);
				}
			}

			// increase poison and hunger duration
			if (!entityLivingBase.getLevel().isClientSide())
			{
				Collection<LastPotionEffect> lastPotionEffects =
					mapEntityLastPotionEffects.get(entityLivingBase.getUUID());
				if (lastPotionEffects == null)
				{// (this is our first time seeing this player)
					// save all current poison and hunger potions
					lastPotionEffects = new ArrayList<>(2);
					Collection<PotionEffect> currentEffects = entityLivingBase.curePotionEffects();
					for (PotionEffect potionEffectCurrent : currentEffects)
					{
						if (potionEffectCurrent.getPotion() == MobEffects.POISON
							|| potionEffectCurrent.getPotion() == MobEffects.HUNGER)
						{
							lastPotionEffects.add(new LastPotionEffect(potionEffectCurrent));
						}
					}
					mapEntityLastPotionEffects.put(entityLivingBase.getUUID(), lastPotionEffects);
				} else
				{
					// mark last potion effects for removal
					lastPotionEffects.forEach(lastPotionEffect -> lastPotionEffect.isFound = false);

					// check all current poison and hunger potions
					var currentEffects = entityLivingBase.getActiveEffects();
					for (MobEffectInstance potionEffectCurrent : currentEffects)
					{
						if (potionEffectCurrent.getEffect() == MobEffects.POISON
							|| potionEffectCurrent.getEffect() == MobEffects.HUNGER)
						{
							// check if it's a new one or changed
							boolean found = false;
							for (LastPotionEffect lastPotionEffect : lastPotionEffects)
							{
								if (lastPotionEffect.potion == potionEffectCurrent.getEffect()
									&& lastPotionEffect.amplifier == potionEffectCurrent.getAmplifier()
									&& lastPotionEffect.duration >= potionEffectCurrent.getDuration())
								{
									lastPotionEffect.isFound = true;
									found = true;
									break;
								}
							}

							if (!found)
							{
								final PotionEffect potionEffectAugmented = new PotionEffect(
									potionEffectCurrent.getPotion(),
									(int) (potionEffectCurrent.getDuration() * 1.8F),
									potionEffectCurrent.getAmplifier(),
									potionEffectCurrent.getIsAmbient(),
									potionEffectCurrent.doesShowParticles()
								);
								entityLivingBase.addEffect(potionEffectAugmented);
								final LastPotionEffect lastPotionEffectAugmented =
									new LastPotionEffect(potionEffectAugmented);
								lastPotionEffectAugmented.isFound = true;
								lastPotionEffects.add(lastPotionEffectAugmented);
							}
						}
					}

					// update and remove last potion effects
					lastPotionEffects.forEach(lastPotionEffect -> lastPotionEffect.duration--);
					lastPotionEffects.removeIf(lastPotionEffect -> lastPotionEffect.duration < 0 || !lastPotionEffect.isFound);
				}
			}
		} else if (entityLivingBase.tickCount % 20 == 0)
		{
			mapEntityLastPotionEffects.remove(entityLivingBase.getUUID());
		}
	}

	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_IMMUNOSUPPRESSANT ? LibConstants.IMMUNO_CONSUMPTION : 0;
	}

	@SubscribeEvent
	public void handleHurt(LivingHurtEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;

		if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_SUBDERMAL_SPIKES)))
		{
			if (event.getSource() instanceof EntityDamageSource
				&& !(event.getSource() instanceof IndirectEntityDamageSource))
			{
				ArmorClass armorClass = ArmorClass.get(entityLivingBase);
				if (armorClass == ArmorClass.HEAVY) return;

				RandomSource random = entityLivingBase.getRandom();
				Entity attacker = event.getSource().getTrueSource();
				if (EnchantmentThorns.shouldHit(3, random)
					&& attacker != null)
				{
					attacker.hurt(
						DamageSource.causeThornsDamage(entityLivingBase),
						(float) EnchantmentThorns.getDamage(2, random)
					);
				}
			}
		}
	}

	@Override
	public int getPowerProduction(ItemStack stack)
	{
		return CyberwareItemMetadata.matches(stack, META_SOLARSKIN) ? LibConstants.SOLAR_PRODUCTION : 0;
	}

	private static class LastPotionEffect
	{
		public final MobEffect potion;
		public final int amplifier;
		public int duration;
		public boolean isFound;

		LastPotionEffect(@Nonnull MobEffectInstance potionEffect)
		{
			potion = potionEffect.getEffect();
			amplifier = potionEffect.getAmplifier();
			duration = potionEffect.getDuration();
			isFound = false;
		}

		@Override
		public String toString()
		{
			return String.format("%s x %d, Duration:  %d",
				potion.getDisplayName(), amplifier + 1, duration
			);
		}
	}
}
