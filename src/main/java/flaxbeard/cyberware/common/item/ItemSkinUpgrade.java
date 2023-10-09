package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.ArmorClass;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.items.Skin;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemSkinUpgrade extends ItemCyberware
{
	public ItemSkinUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.SKIN);
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return stack.is(Skin.IMMUNO.get()) ? LibConstants.IMMUNO_CONSUMPTION : 0;
	}

	@Override
	public int getPowerProduction(@Nonnull ItemStack stack)
	{
		return stack.is(Skin.SOLAR_SKIN.get()) ? LibConstants.SOLAR_PRODUCTION : 0;
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

	public static class EventHandler
	{
		@SubscribeEvent
		public void handleLivingUpdate(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			if (entityLivingBase.tickCount % 20 != 0) return;

			float lightFactor = getLightFactor(entityLivingBase);
			if (lightFactor <= 0.0F) return;

			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

			var solarItem = (ItemSkinUpgrade) Skin.SOLAR_SKIN.get();
			ItemStack itemStackSolarSkin = cyberwareUserData.getCyberware(solarItem.getDefaultInstance());
			if (!itemStackSolarSkin.isEmpty())
			{
				int power = Math.max(0, Math.round(solarItem.getPowerProduction(itemStackSolarSkin) * lightFactor));
				cyberwareUserData.addPower(power, itemStackSolarSkin);
			}
		}

		private float getLightFactor(LivingEntity entityLivingBase)
		{
			Level world = entityLivingBase.level;
			// world must have a sun
			if (!entityLivingBase.level.dimensionType().hasSkyLight()) return 0.0F;
			var pos2 = entityLivingBase.position();
			// current position can see the sun
			BlockPos pos = new BlockPos(pos2.x, pos2.y + entityLivingBase.getBbHeight(), pos2.z);
			if (!entityLivingBase.level.canSeeSky(pos)) return 0.0F;

			// sun isn't shaded
			int lightSky = world.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(pos);
			// note: world.getSkylightSubtracted() is server side only
			if (lightSky < 15) return 0.0F;

			// it's day time (see Vanilla daylight sensor)
			float celestialAngleRadians = world.getSunAngle(1.0F);
			float offsetRadians = celestialAngleRadians < (float) Math.PI ? 0.0F : ((float) Math.PI * 2.0F);
			float celestialAngleRadians2 = celestialAngleRadians + (offsetRadians - celestialAngleRadians) * 0.2F;
			return Mth.cos(celestialAngleRadians2);
		}

		private static final Map<UUID, Collection<LastPotionEffect>> mapEntityLastPotionEffects = new HashMap<>();

		@SubscribeEvent(priority = EventPriority.HIGH)
		public void handleMissingEssentials(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

			var immunoItem = (ItemSkinUpgrade) Skin.IMMUNO.get();
			ItemStack itemStackImmunosuppressant = cyberwareUserData.getCyberware(immunoItem.getDefaultInstance());
			if (!itemStackImmunosuppressant.isEmpty())
			{
				// consume power every 1 s, apply damage every 5 s unless power or Neuropozyne is active
				if (entityLivingBase instanceof Player
					&& entityLivingBase.tickCount % 20 == 0)
				{
					boolean isPowered = cyberwareUserData.usePower(
						itemStackImmunosuppressant,
						immunoItem.getPowerConsumption(itemStackImmunosuppressant)
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
						Collection<MobEffectInstance> currentEffects = entityLivingBase.getActiveEffects();
						for (MobEffectInstance potionEffectCurrent : currentEffects)
						{
							if (potionEffectCurrent.getEffect() == MobEffects.POISON
								|| potionEffectCurrent.getEffect() == MobEffects.HUNGER)
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
									final MobEffectInstance potionEffectAugmented = new MobEffectInstance(
										potionEffectCurrent.getEffect(),
										(int) (potionEffectCurrent.getDuration() * 1.8F),
										potionEffectCurrent.getAmplifier(),
										potionEffectCurrent.isAmbient(),
										potionEffectCurrent.isVisible()
									);

									entityLivingBase.addEffect(potionEffectAugmented);

									final LastPotionEffect lastPotionEffectAugmented = new LastPotionEffect(potionEffectAugmented);
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

		@SubscribeEvent
		public void handleHurt(LivingHurtEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
			if (cyberwareUserData == null) return;

			if (cyberwareUserData.isCyberwareInstalled(Skin.SUBDERMAL_SPIKES.get().getDefaultInstance()))
			{
				if (event.getSource() instanceof EntityDamageSource
					&& !(event.getSource() instanceof IndirectEntityDamageSource))
				{
					ArmorClass armorClass = ArmorClass.get(entityLivingBase);
					if (armorClass == ArmorClass.HEAVY) return;

					RandomSource random = entityLivingBase.getRandom();
					Entity attacker = event.getSource().getEntity();
					if (ThornsEnchantment.shouldHit(3, random)
						&& attacker != null)
					{
						attacker.hurt(
							DamageSource.thorns(entityLivingBase),
							(float) ThornsEnchantment.getDamage(2, random)
						);
					}
				}
			}
		}
	}
}
