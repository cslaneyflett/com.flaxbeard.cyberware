package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.registry.items.CyberLimbs;
import flaxbeard.cyberware.common.registry.items.LegUpgrades;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemFootUpgrade extends ItemCyberware implements IMenuItem
{
	public ItemFootUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.FOOT);
	}

	@Nonnull
	@Override
	public NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack)
	{
		if (stack.is(LegUpgrades.AQUA.get())) return NonNullList.create();

		return NNLUtil.fromArray(new ItemStack[][]{
			new ItemStack[]{
				CyberLimbs.CYBERLEG_LEFT.get().getDefaultInstance(),
				CyberLimbs.CYBERLEG_RIGHT.get().getDefaultInstance()
			}});
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return stack.is(LegUpgrades.AQUA.get()) ? LibConstants.AQUA_CONSUMPTION :
			stack.is(LegUpgrades.WHEELS.get()) ? LibConstants.WHEEL_CONSUMPTION : 0;
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return stack.is(LegUpgrades.WHEELS.get());
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

	public static class EventHandler
	{
		public static final EventHandler INSTANCE = new EventHandler();

		@SubscribeEvent
		public void handleHorseMove(LivingEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			if (entityLivingBase instanceof Horse entityHorse)
			{
				ItemStack itemStackSpurs = LegUpgrades.SPURS.get().getDefaultInstance();
				for (Entity entityPassenger : entityHorse.getPassengers())
				{
					if (entityPassenger instanceof LivingEntity)
					{
						ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPassenger);
						if (cyberwareUserData != null
							&& cyberwareUserData.isCyberwareInstalled(itemStackSpurs))
						{
							entityHorse.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 5, true, false));
							break;
						}
					}
				}
			}
		}

		private final Set<UUID> setIsAquaPowered = new HashSet<>();
		private final Map<UUID, Integer> mapCountdownWheelsPowered = new HashMap<>();
		private final Map<UUID, Float> mapStepHeight = new HashMap<>();

		// TODO: below is certified shitcode, gotta redo my warcrime
		// private final AttributeModifier stepModifier = new AttributeModifier("cyberware_foot_step", 1F, AttributeModifier.Operation.ADDITION);

		@SubscribeEvent(priority = EventPriority.NORMAL)
		public void handleLivingUpdate(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

			if (!entityLivingBase.isOnGround()
				&& entityLivingBase.isInWater())
			{
				var aquaItem = (ItemFootUpgrade) LegUpgrades.AQUA.get();
				ItemStack itemStackAqua = cyberwareUserData.getCyberware(aquaItem.getDefaultInstance());
				if (!itemStackAqua.isEmpty())
				{
					boolean wasPowered = setIsAquaPowered.contains(entityLivingBase.getUUID());
					boolean isPowered = entityLivingBase.tickCount % 20 == 0
						? cyberwareUserData.usePower(itemStackAqua, aquaItem.getPowerConsumption(itemStackAqua))
						: wasPowered;
					if (isPowered)
					{
						if (entityLivingBase.getDeltaMovement().horizontalDistanceSqr() > 0.0F)
						{
							int numLegs = 0;
							if (cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERLEG_LEFT.get().getDefaultInstance()))
							{
								numLegs++;
							}
							if (cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERLEG_RIGHT.get().getDefaultInstance()))
							{
								numLegs++;
							}

							// increase maximum horizontal motion
							float boost = numLegs * 0.4F;

							// TODO: this correct?
							entityLivingBase.moveRelative(
								(float) (entityLivingBase.getDeltaMovement().horizontalDistance() * boost),
								entityLivingBase.getForward()
							);
						}
						if (!wasPowered)
						{
							setIsAquaPowered.add(entityLivingBase.getUUID());
						}
					} else if (entityLivingBase.tickCount % 20 == 0)
					{
						setIsAquaPowered.remove(entityLivingBase.getUUID());
					}
				}
			} else if (entityLivingBase.tickCount % 20 == 0)
			{
				setIsAquaPowered.remove(entityLivingBase.getUUID());
			}

			var wheelsItem = (ItemFootUpgrade) LegUpgrades.WHEELS.get();
			ItemStack itemStackWheels = cyberwareUserData.getCyberware(wheelsItem.getDefaultInstance());
			if (!itemStackWheels.isEmpty())
			{
				boolean wasPowered = getCountdownWheelsPowered(entityLivingBase) > 0;

				boolean isPowered = EnableDisableHelper.isEnabled(itemStackWheels)
					&& (entityLivingBase.tickCount % 20 == 0
					? cyberwareUserData.usePower(itemStackWheels, wheelsItem.getPowerConsumption(itemStackWheels))
					: wasPowered);
				if (isPowered)
				{
					if (!mapStepHeight.containsKey(entityLivingBase.getUUID()))
					{
						// mapStepHeight.put(entityLivingBase.getUUID(), Math.max(entityLivingBase.stepHeight, .6F));
						Objects.requireNonNull(entityLivingBase.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()))
							.setBaseValue(.6F);
					}
					Objects.requireNonNull(entityLivingBase.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()))
						.setBaseValue(1F);

					mapCountdownWheelsPowered.put(entityLivingBase.getUUID(), 10);
				} else if (mapStepHeight.containsKey(entityLivingBase.getUUID()) && wasPowered)
				{
					Objects.requireNonNull(entityLivingBase.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()))
						.setBaseValue(mapStepHeight.get(entityLivingBase.getUUID()));

					mapCountdownWheelsPowered.put(
						entityLivingBase.getUUID(),
						getCountdownWheelsPowered(entityLivingBase) - 1
					);
				} else
				{
					mapCountdownWheelsPowered.put(entityLivingBase.getUUID(), 0);
				}
			} else if (mapStepHeight.containsKey(entityLivingBase.getUUID()))
			{
				Objects.requireNonNull(entityLivingBase.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()))
					.setBaseValue(mapStepHeight.get(entityLivingBase.getUUID()));

				int countdownWheelsPowered = getCountdownWheelsPowered(entityLivingBase) - 1;
				if (countdownWheelsPowered == 0)
				{
					mapStepHeight.remove(entityLivingBase.getUUID());
				}

				mapCountdownWheelsPowered.put(entityLivingBase.getUUID(), countdownWheelsPowered);
			}
		}

		private int getCountdownWheelsPowered(LivingEntity entityLivingBase)
		{
			return mapCountdownWheelsPowered.computeIfAbsent(entityLivingBase.getUUID(), k -> 10);
		}
	}
}