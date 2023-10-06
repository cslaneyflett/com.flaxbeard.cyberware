package flaxbeard.cyberware.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.SwitchHeldItemAndRotationPacket;
import net.minecraft.item.ItemSword;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ItemMuscleUpgrade extends ItemCyberware implements IMenuItem
{
	private static final int META_WIRED_REFLEXES = 0;
	private static final int META_MUSCLE_REPLACEMENTS = 1;
	private static final UUID idMuscleSpeedAttribute = UUID.fromString("f0ab4766-4be1-11e6-beb8-9e71128cae77");
	private static final UUID idMuscleDamageAttribute = UUID.fromString("f63d6916-4be1-11e6-beb8-9e71128cae77");
	private static final HashMultimap<Attribute, AttributeModifier> multimapMuscleSpeedAttribute;
	private static final HashMultimap<Attribute, AttributeModifier> multimapMuscleDamageAttribute;

	static
	{
		multimapMuscleSpeedAttribute = HashMultimap.create();
		multimapMuscleSpeedAttribute.put(Attributes.ATTACK_SPEED, new AttributeModifier(idMuscleSpeedAttribute,
			"Muscle speed upgrade", 1.5F,
			0
		));
		multimapMuscleDamageAttribute = HashMultimap.create();
		multimapMuscleDamageAttribute.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(idMuscleDamageAttribute,
			"Muscle damage upgrade", 3F,
			0
		));
	}

	public ItemMuscleUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onAdded(LivingEntity entityLivingBase, ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_WIRED_REFLEXES)
		{
			entityLivingBase.getAttributeMap().applyAttributeModifiers(multimapMuscleSpeedAttribute);
		} else if (CyberwareItemMetadata.get(stack) == META_MUSCLE_REPLACEMENTS)
		{
			entityLivingBase.getAttributeMap().applyAttributeModifiers(multimapMuscleDamageAttribute);
		}
	}

	@Override
	public void onRemoved(LivingEntity entityLivingBase, ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_WIRED_REFLEXES)
		{
			entityLivingBase.getAttributes().removeAttributeModifiers(multimapMuscleSpeedAttribute);
		} else if (CyberwareItemMetadata.get(stack) == META_MUSCLE_REPLACEMENTS)
		{
			entityLivingBase.getAttributes().removeAttributeModifiers(multimapMuscleDamageAttribute);
		}
	}

	@Override
	public int installedStackSize(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_WIRED_REFLEXES ? 3 : 1;
	}

	@SubscribeEvent
	public void handleHurt(LivingHurtEvent event)
	{
		if (event.isCanceled()) return;
		LivingEntity entityLivingBase = event.getEntity();
		if (!(entityLivingBase instanceof Player)) return;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;

		ItemStack itemStackWiredReflexes = cyberwareUserData.getCyberware(getCachedStack(META_WIRED_REFLEXES));
		int rank = itemStackWiredReflexes.getCount();
		if (rank > 1
			&& EnableDisableHelper.isEnabled(itemStackWiredReflexes)
			&& setIsStrengthPowered.contains(entityLivingBase.getUUID()))
		{
			Player entityPlayer = (Player) entityLivingBase;
			if (event.getSource() instanceof EntityDamageSource
				&& !(event.getSource() instanceof IndirectEntityDamageSource))
			{
				EntityDamageSource source = (EntityDamageSource) event.getSource();
				Entity attacker = source.getEntity();
				int lastAttacked = entityPlayer.getCombatTracker().lastDamageTime;

				if (entityPlayer.tickCount - lastAttacked > 120)
				{
					int indexWeapon = -1;
					ItemStack itemMainhand = entityPlayer.getMainHandItem();
					if (!itemMainhand.isEmpty())
					{
						if (entityPlayer.getItemInUseCount() > 0
							|| itemMainhand.getItem() instanceof ItemSword
							|| itemMainhand.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, itemMainhand).containsKey(Attributes.ATTACK_DAMAGE))
						{
							indexWeapon = entityPlayer.inventory.currentItem;
						}
					}

					if (indexWeapon == -1)
					{
						double mostDamage = 0F;

						for (int indexHotbar = 0; indexHotbar < 10; indexHotbar++)
						{
							if (indexHotbar != entityPlayer.inventory.currentItem)
							{
								ItemStack potentialWeapon = entityPlayer.inventory.mainInventory.get(indexHotbar);
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

										if (damage > mostDamage || indexWeapon == -1)
										{
											mostDamage = damage;
											indexWeapon = indexHotbar;
										}
									}
								}
							}
						}
					}

					if (indexWeapon != -1)
					{
						entityPlayer.inventory.currentItem = indexWeapon;

						CyberwarePacketHandler.INSTANCE.sendTo(
							new SwitchHeldItemAndRotationPacket(indexWeapon, entityPlayer.getId(),
								rank > 2 && attacker != null ? attacker.getId()
									: -1
							),
							(ServerPlayer) entityPlayer
						);

						ServerLevel worldServer = (ServerLevel) entityPlayer.level;

						for (Player trackingPlayer : worldServer.getEntityTracker().getTrackingPlayers(entityPlayer))
						{
							CyberwarePacketHandler.INSTANCE.sendTo(
								new SwitchHeldItemAndRotationPacket(indexWeapon, entityPlayer.getId(),
									rank > 2 && attacker != null ?
										attacker.getId() : -1
								),
								(ServerPlayer) trackingPlayer
							);
						}
					}
				}
			}
		}
	}

	private Set<UUID> setIsStrengthPowered = new HashSet<>();

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		ItemStack itemStackMuscleReplacement =
			cyberwareUserData.getCyberware(getCachedStack(META_MUSCLE_REPLACEMENTS));
		if (!itemStackMuscleReplacement.isEmpty())
		{
			boolean wasPowered = setIsStrengthPowered.contains(entityLivingBase.getUUID());
			boolean isPowered = entityLivingBase.tickCount % 20 == 0
				? cyberwareUserData.usePower(
				itemStackMuscleReplacement,
				getPowerConsumption(itemStackMuscleReplacement)
			)
				: wasPowered;
			if (isPowered)
			{
				if (!entityLivingBase.isInWater()
					&& entityLivingBase.isOnGround()
					&& Math.abs(entityLivingBase.moveStrafing) + Math.abs(entityLivingBase.moveForward) > 0.0F
					&& Math.abs(entityLivingBase.motionX) + Math.abs(entityLivingBase.motionZ) > 0.0F)
				{
					// increase maximum horizontal motion by 70% (0.118 -> 0.200)
					float boost = 0.51F;
					entityLivingBase.moveRelative(entityLivingBase.moveStrafing * boost, 0.0F,
						entityLivingBase.moveForward * boost, 0.075F
					);
				}

				if (entityLivingBase.tickCount % 20 == 0)
				{
					onAdded(entityLivingBase, itemStackMuscleReplacement);
				}
				if (!wasPowered)
				{
					setIsStrengthPowered.add(entityLivingBase.getUUID());
				}
			} else if (entityLivingBase.tickCount % 20 == 0)
			{
				onRemoved(entityLivingBase, itemStackMuscleReplacement);
				setIsStrengthPowered.remove(entityLivingBase.getUUID());
			}
		} else if (entityLivingBase.tickCount % 20 == 0)
		{
			onRemoved(entityLivingBase, itemStackMuscleReplacement);
			setIsStrengthPowered.remove(entityLivingBase.getUUID());
		}

		if (entityLivingBase.tickCount % 20 == 0)
		{
			ItemStack itemStackWiredReflexes = cyberwareUserData.getCyberware(getCachedStack(META_WIRED_REFLEXES));
			if (!itemStackWiredReflexes.isEmpty()
				&& EnableDisableHelper.isEnabled(itemStackWiredReflexes))
			{
				boolean isPowered = cyberwareUserData.usePower(
					itemStackWiredReflexes,
					getPowerConsumption(itemStackWiredReflexes)
				);
				if (isPowered)
				{
					onAdded(entityLivingBase, itemStackWiredReflexes);
				} else
				{
					onRemoved(entityLivingBase, itemStackWiredReflexes);
				}
			} else
			{
				onRemoved(entityLivingBase, itemStackWiredReflexes);
			}
		}
	}

	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_WIRED_REFLEXES ? LibConstants.REFLEXES_CONSUMPTION :
			LibConstants.REPLACEMENTS_CONSUMPTION;
	}

	@Override
	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_WIRED_REFLEXES)
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
		return CyberwareItemMetadata.get(stack) == META_WIRED_REFLEXES;
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
	public boolean isEssential(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_MUSCLE_REPLACEMENTS;
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return CyberwareItemMetadata.get(stack) == META_MUSCLE_REPLACEMENTS
			&& CyberwareAPI.getCyberware(other).isEssential(other);
	}
}
