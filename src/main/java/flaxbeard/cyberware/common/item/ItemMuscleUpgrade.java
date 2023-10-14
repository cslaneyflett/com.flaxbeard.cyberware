package flaxbeard.cyberware.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.SwitchHeldItemAndRotationPacket;
import flaxbeard.cyberware.common.registry.items.MuscleUpgrades;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ItemMuscleUpgrade extends ItemCyberware implements IMenuItem
{
	private static final UUID idMuscleSpeedAttribute = UUID.fromString("f0ab4766-4be1-11e6-beb8-9e71128cae77");
	private static final UUID idMuscleDamageAttribute = UUID.fromString("f63d6916-4be1-11e6-beb8-9e71128cae77");
	private static final HashMultimap<Attribute, AttributeModifier> multimapMuscleSpeedAttribute;
	private static final HashMultimap<Attribute, AttributeModifier> multimapMuscleDamageAttribute;

	static
	{
		multimapMuscleSpeedAttribute = HashMultimap.create();
		multimapMuscleSpeedAttribute.put(Attributes.ATTACK_SPEED, new AttributeModifier(idMuscleSpeedAttribute,
			"Muscle speed upgrade", 1.5F,
			AttributeModifier.Operation.ADDITION
		));
		multimapMuscleDamageAttribute = HashMultimap.create();
		multimapMuscleDamageAttribute.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(idMuscleDamageAttribute,
			"Muscle damage upgrade", 3F,
			AttributeModifier.Operation.ADDITION
		));
	}

	public ItemMuscleUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.MUSCLE);
	}

	@Override
	public void onAdded(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
	{
		if (stack.is(MuscleUpgrades.WIRED_REFLEXES.get()))
		{
			entityLivingBase.getAttributes().addTransientAttributeModifiers(multimapMuscleSpeedAttribute);
		} else if (stack.is(MuscleUpgrades.MUSCLE_REPLACEMENTS.get()))
		{
			entityLivingBase.getAttributes().addTransientAttributeModifiers(multimapMuscleDamageAttribute);
		}
	}

	@Override
	public void onRemoved(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
	{
		if (stack.is(MuscleUpgrades.WIRED_REFLEXES.get()))
		{
			entityLivingBase.getAttributes().removeAttributeModifiers(multimapMuscleSpeedAttribute);
		} else if (stack.is(MuscleUpgrades.MUSCLE_REPLACEMENTS.get()))
		{
			entityLivingBase.getAttributes().removeAttributeModifiers(multimapMuscleDamageAttribute);
		}
	}

	@Override
	public int maximumStackSize(@Nonnull ItemStack stack)
	{
		return stack.is(MuscleUpgrades.WIRED_REFLEXES.get()) ? 3 : 1;
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return stack.is(MuscleUpgrades.WIRED_REFLEXES.get()) ? LibConstants.REFLEXES_CONSUMPTION :
			LibConstants.REPLACEMENTS_CONSUMPTION;
	}

	@Override
	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		if (stack.is(MuscleUpgrades.WIRED_REFLEXES.get()))
		{
			switch (stack.getCount())
			{
				case 1:
					return 9;
				case 2:
					return 10;
				case 3:
					return 11;
			}
		}
		return super.getUnmodifiedEssenceCost(stack);
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return stack.is(MuscleUpgrades.WIRED_REFLEXES.get());
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

	@Override
	public boolean isEssential(@Nonnull ItemStack stack)
	{
		return stack.is(MuscleUpgrades.MUSCLE_REPLACEMENTS.get());
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return stack.is(MuscleUpgrades.MUSCLE_REPLACEMENTS.get())
			&& CyberwareAPI.getCyberware(other).isEssential(other);
	}

	public static class EventHandler
	{
		public static final EventHandler INSTANCE = new EventHandler();

		@SubscribeEvent
		public void handleHurt(LivingHurtEvent event)
		{
			if (event.isCanceled()) return;
			LivingEntity entityLivingBase = event.getEntity();
			if (!(entityLivingBase instanceof Player)) return;
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
			if (cyberwareUserData == null) return;

			ItemStack itemStackWiredReflexes = cyberwareUserData.getCyberware(MuscleUpgrades.WIRED_REFLEXES.get().getDefaultInstance());
			int rank = itemStackWiredReflexes.getCount();
			if (rank > 1 &&
				EnableDisableHelper.isEnabled(itemStackWiredReflexes) &&
				setIsStrengthPowered.contains(entityLivingBase.getUUID()) &&
				entityLivingBase instanceof ServerPlayer entityPlayer)
			{
				if (event.getSource() instanceof EntityDamageSource source
					&& !(event.getSource() instanceof IndirectEntityDamageSource))
				{
					Entity attacker = source.getEntity();

					// Last attacked or right now if never attacked.
					CombatEntry lastAttack = entityPlayer.getCombatTracker().getLastEntry();
					int lastAttacked = lastAttack != null ? lastAttack.getTime() : entityPlayer.tickCount;

					if (entityPlayer.tickCount - lastAttacked > 120)
					{
						ItemStack indexWeapon = null;
						ItemStack mainHandItem = entityPlayer.getMainHandItem();
						if (!mainHandItem.isEmpty())
						{
							if (entityPlayer.isUsingItem() ||
								mainHandItem.is(Tags.Items.TOOLS_SWORDS) ||
								mainHandItem.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, mainHandItem).containsKey(Attributes.ATTACK_DAMAGE))
							{
								indexWeapon = entityPlayer.getUseItem();
							}
						}

						if (indexWeapon == null)
						{
							double mostDamage = 0F;

							for (int indexHotbar = 0; indexHotbar < 10; indexHotbar++)
							{
								// Skip the currently selected item, since if it's the best, no point in comparing
								if (indexHotbar != entityPlayer.getInventory().selected)
								{
									ItemStack potentialWeapon = entityPlayer.getInventory().getItem(indexHotbar);
									if (!potentialWeapon.isEmpty())
									{
										Multimap<Attribute, AttributeModifier> modifiers =
											potentialWeapon.getItem().getAttributeModifiers(
												EquipmentSlot.MAINHAND,
												potentialWeapon
											);
										if (modifiers.containsKey(Attributes.ATTACK_DAMAGE))
										{
											double damage =
												modifiers.get(Attributes.ATTACK_DAMAGE).iterator().next().getAmount();

											if (damage > mostDamage || indexWeapon == null)
											{
												mostDamage = damage;
												indexWeapon = potentialWeapon;
											}
										}
									}
								}
							}
						}

						if (indexWeapon != null)
						{
							var slot = entityPlayer.getInventory().findSlotMatchingItem(indexWeapon);
							entityPlayer.getInventory().pickSlot(slot);

							CyberwarePacketHandler.INSTANCE.send(
								PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entityPlayer),
								new SwitchHeldItemAndRotationPacket(
									slot, entityPlayer.getId(),
									rank > 2 && attacker != null ? attacker.getId() : -1
								)
							);
						}
					}
				}
			}
		}

		private final Set<UUID> setIsStrengthPowered = new HashSet<>();

		@SubscribeEvent(priority = EventPriority.NORMAL)
		public void handleLivingUpdate(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

			var muscleItem = (ItemMuscleUpgrade) MuscleUpgrades.MUSCLE_REPLACEMENTS.get();
			ItemStack itemStackMuscleReplacement =
				cyberwareUserData.getCyberware(muscleItem.getDefaultInstance());
			if (!itemStackMuscleReplacement.isEmpty())
			{
				boolean wasPowered = setIsStrengthPowered.contains(entityLivingBase.getUUID());
				boolean isPowered = entityLivingBase.tickCount % 20 == 0
					? cyberwareUserData.usePower(
					itemStackMuscleReplacement,
					muscleItem.getPowerConsumption(itemStackMuscleReplacement)
				)
					: wasPowered;
				if (isPowered)
				{
					if (!entityLivingBase.isInWater() &&
						entityLivingBase.isOnGround() &&
						entityLivingBase.getDeltaMovement().horizontalDistanceSqr() > 0.0F)
					{
						// increase maximum horizontal motion by 70% (0.118 -> 0.200)
						float boost = 0.51F;
						// TODO: this correct?
						entityLivingBase.moveRelative(
							(float) (entityLivingBase.getDeltaMovement().horizontalDistance() * boost),
							entityLivingBase.getForward()
						);
					}

					if (entityLivingBase.tickCount % 20 == 0)
					{
						muscleItem.onAdded(entityLivingBase, itemStackMuscleReplacement);
					}
					if (!wasPowered)
					{
						setIsStrengthPowered.add(entityLivingBase.getUUID());
					}
				} else if (entityLivingBase.tickCount % 20 == 0)
				{
					muscleItem.onRemoved(entityLivingBase, itemStackMuscleReplacement);
					setIsStrengthPowered.remove(entityLivingBase.getUUID());
				}
			} else if (entityLivingBase.tickCount % 20 == 0)
			{
				muscleItem.onRemoved(entityLivingBase, itemStackMuscleReplacement);
				setIsStrengthPowered.remove(entityLivingBase.getUUID());
			}

			if (entityLivingBase.tickCount % 20 == 0)
			{
				var reflexItem = (ItemMuscleUpgrade) MuscleUpgrades.WIRED_REFLEXES.get();
				ItemStack itemStackWiredReflexes = cyberwareUserData.getCyberware(reflexItem.getDefaultInstance());
				if (!itemStackWiredReflexes.isEmpty()
					&& EnableDisableHelper.isEnabled(itemStackWiredReflexes))
				{
					boolean isPowered = cyberwareUserData.usePower(
						itemStackWiredReflexes,
						muscleItem.getPowerConsumption(itemStackWiredReflexes)
					);
					if (isPowered)
					{
						reflexItem.onAdded(entityLivingBase, itemStackWiredReflexes);
					} else
					{
						reflexItem.onRemoved(entityLivingBase, itemStackWiredReflexes);
					}
				} else
				{
					reflexItem.onRemoved(entityLivingBase, itemStackWiredReflexes);
				}
			}
		}
	}
}
