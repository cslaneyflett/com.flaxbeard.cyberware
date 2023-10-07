package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.misc.NNLUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class ItemFootUpgrade extends ItemCyberware implements IMenuItem
{
	public static final int META_SPURS = 0;
	public static final int META_AQUA = 1;
	public static final int META_WHEELS = 2;

	public ItemFootUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) != META_AQUA) return NonNullList.create();

		return NNLUtil.fromArray(new ItemStack[][]{
			new ItemStack[]{CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG),
				CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG)}});
	}

	@SubscribeEvent
	public void handleHorseMove(LivingEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase instanceof Horse entityHorse)
		{
			ItemStack itemStackSpurs = getCachedStack(META_SPURS);
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

	private Set<UUID> setIsAquaPowered = new HashSet<>();
	private Map<UUID, Integer> mapCountdownWheelsPowered = new HashMap<>();
	private Map<UUID, Float> mapStepHeight = new HashMap<>();

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		if (!entityLivingBase.isOnGround()
			&& entityLivingBase.isInWater())
		{
			ItemStack itemStackAqua = cyberwareUserData.getCyberware(getCachedStack(META_AQUA));
			if (!itemStackAqua.isEmpty())
			{
				boolean wasPowered = setIsAquaPowered.contains(entityLivingBase.getUUID());
				boolean isPowered = entityLivingBase.tickCount % 20 == 0
					? cyberwareUserData.usePower(itemStackAqua, getPowerConsumption(itemStackAqua))
					: wasPowered;
				if (isPowered)
				{
					if (Math.abs(entityLivingBase.moveStrafing) + Math.abs(entityLivingBase.moveForward) > 0.0F
						&& Math.abs(entityLivingBase.motionX) + Math.abs(entityLivingBase.motionZ) > 0.0F)
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

						// increase maximum horizontal motion
						float boost = numLegs * 0.4F;
						entityLivingBase.moveRelative(entityLivingBase.moveStrafing * boost, 0.26F,
							entityLivingBase.moveForward * boost, 0.075F
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

		ItemStack itemStackWheels = cyberwareUserData.getCyberware(getCachedStack(META_WHEELS));
		if (!itemStackWheels.isEmpty())
		{
			boolean wasPowered = getCountdownWheelsPowered(entityLivingBase) > 0;

			boolean isPowered = EnableDisableHelper.isEnabled(itemStackWheels)
				&& (entityLivingBase.tickCount % 20 == 0
				? cyberwareUserData.usePower(itemStackWheels, getPowerConsumption(itemStackWheels))
				: wasPowered);
			if (isPowered)
			{
				if (!mapStepHeight.containsKey(entityLivingBase.getUUID()))
				{
					// TODO attribute STEP_HEIGHT_ADDITION
					mapStepHeight.put(entityLivingBase.getUUID(), Math.max(entityLivingBase.stepHeight, .6F));
				}
				entityLivingBase.getStepHeight() = 1F;

				mapCountdownWheelsPowered.put(entityLivingBase.getUUID(), 10);
			} else if (mapStepHeight.containsKey(entityLivingBase.getUUID()) && wasPowered)
			{
				// TODO attribute STEP_HEIGHT_ADDITION
				entityLivingBase.stepHeight = mapStepHeight.get(entityLivingBase.getUUID());

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
			// TODO attribute STEP_HEIGHT_ADDITION
			entityLivingBase.stepHeight = mapStepHeight.get(entityLivingBase.getUUID());

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

	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_AQUA ? LibConstants.AQUA_CONSUMPTION :
			CyberwareItemMetadata.get(stack) == META_WHEELS ? LibConstants.WHEEL_CONSUMPTION : 0;
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_WHEELS;
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