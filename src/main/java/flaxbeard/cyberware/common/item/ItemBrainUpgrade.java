package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.ArmorClass;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.DodgePacket;
import flaxbeard.cyberware.common.registry.items.BrainUpgrades;
import flaxbeard.cyberware.common.registry.items.Misc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ItemBrainUpgrade extends ItemCyberware implements IMenuItem
{
	public ItemBrainUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.CRANIUM);
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return other.getItem() == this
			&& stack.is(BrainUpgrades.CORTICAL_STACK.get())
			&& other.is(BrainUpgrades.CONSCIOUSNESS_TRANSMITTER.get());
	}

	@SubscribeEvent
	public void handleTeleJam(EntityTeleportEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof LivingEntity entityLivingBase && !isTeleportationAllowed(entityLivingBase))
		{
			event.setCanceled(true);
		}
	}

	public static boolean isTeleportationAllowed(@Nullable LivingEntity entityLivingBase)
	{
		if (entityLivingBase == null) return true;

		ItemStack itemStackJammer = BrainUpgrades.ENDER_JAMMER.get().getDefaultInstance();

		ICyberwareUserData cyberwareUserDataSelf = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserDataSelf != null)
		{
			ItemStack itemStackJammerSelf = cyberwareUserDataSelf.getCyberware(itemStackJammer);
			if (!itemStackJammerSelf.isEmpty()
				&& EnableDisableHelper.isEnabled(itemStackJammerSelf))
			{
				return false;
			}
		}

		float range = 25F;
		var pos = entityLivingBase.position();
		List<LivingEntity> entitiesInRange = entityLivingBase.level.getEntitiesOfClass(
			LivingEntity.class,
			new AABB(pos.x - range, pos.y - range, pos.z - range,
				pos.x + entityLivingBase.getBbWidth() + range,
				pos.y + entityLivingBase.getBbHeight() + range,
				pos.z + entityLivingBase.getBbWidth() + range
			)
		);

		for (LivingEntity entityInRange : entitiesInRange)
		{
			if (entityLivingBase.distanceTo(entityInRange) <= range)
			{
				ICyberwareUserData cyberwareUserDataInRange = CyberwareAPI.getCapabilityOrNull(entityInRange);
				if (cyberwareUserDataInRange != null)
				{
					ItemStack itemStackJammerInRange = cyberwareUserDataInRange.getCyberware(itemStackJammer);
					if (!itemStackJammerInRange.isEmpty()
						&& EnableDisableHelper.isEnabled(itemStackJammerInRange))
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	@SubscribeEvent
	public void handleClone(PlayerEvent.Clone event)
	{
		if (event.isWasDeath())
		{
			Player entityPlayerOriginal = event.getOriginal();

			if (entityPlayerOriginal.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
			{
				return;
			}

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayerOriginal);
			if (cyberwareUserData == null) return;

			if (cyberwareUserData.isCyberwareInstalled(BrainUpgrades.CORTICAL_STACK.get().getDefaultInstance()))
			{
				if (!entityPlayerOriginal.level.isClientSide())
				{
					ItemStack stack = new ItemStack(Misc.EXP_CAPSULE.get());
					CompoundTag tagCompound = new CompoundTag();
					tagCompound.putInt("xp", entityPlayerOriginal.totalExperience);
					stack.setTag(tagCompound);
					var pos = entityPlayerOriginal.position();

					entityPlayerOriginal.level.addFreshEntity(new ItemEntity(
						entityPlayerOriginal.level, pos.x(), pos.y(), pos.z(),
						stack
					));
				}
			} else if (cyberwareUserData.isCyberwareInstalled(BrainUpgrades.CONSCIOUSNESS_TRANSMITTER.get().getDefaultInstance()))
			{
				event.getEntity().giveExperienceLevels((int) (entityPlayerOriginal.experienceLevel * .9F));
			}
		}
	}

	@SubscribeEvent
	public void handleMining(BreakSpeed event)
	{
		Player entityPlayer = event.getEntity();

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;

		ItemStack itemStackNeuralContextualizer = cyberwareUserData.getCyberware(
			BrainUpgrades.NEURAL_CONTEXTUALIZER.get().getDefaultInstance()
		);
		if (!itemStackNeuralContextualizer.isEmpty() &&
			EnableDisableHelper.isEnabled(itemStackNeuralContextualizer) &&
			isContextWorking(entityPlayer) &&
			!entityPlayer.isShiftKeyDown())
		{
			BlockState state = event.getState();
			ItemStack tool = entityPlayer.getItemInHand(InteractionHand.MAIN_HAND);

			if (!tool.isEmpty() &&
				tool.is(Tags.Items.TOOLS_SWORDS))
			{
				return;
			}

			if (isToolEffective(tool, state)) return;

			for (int indexSlot = 0; indexSlot < 10; indexSlot++)
			{
				if (indexSlot != entityPlayer.getInventory().selected)
				{
					ItemStack potentialTool = entityPlayer.getInventory().getItem(indexSlot);
					if (isToolEffective(potentialTool, state))
					{
						entityPlayer.getInventory().selected = indexSlot;
						return;
					}
				}
			}
		}
	}

	private static final Map<UUID, Boolean> isContextWorking = new HashMap<>();
	private static final Map<UUID, Boolean> isMatrixWorking = new HashMap<>();
	private static final Map<UUID, Boolean> isRadioWorking = new HashMap<>();

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase.tickCount % 20 != 0) return;

		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		ItemStack itemStackNeuralContextualizer =
			cyberwareUserData.getCyberware(BrainUpgrades.NEURAL_CONTEXTUALIZER.get().getDefaultInstance());
		if (!itemStackNeuralContextualizer.isEmpty()
			&& EnableDisableHelper.isEnabled(itemStackNeuralContextualizer))
		{
			isContextWorking.put(entityLivingBase.getUUID(), cyberwareUserData.usePower(
				itemStackNeuralContextualizer,
				getPowerConsumption(itemStackNeuralContextualizer)
			));
		} else
		{
			isContextWorking.put(entityLivingBase.getUUID(), Boolean.FALSE);
		}

		ItemStack itemStackThreatMatrix = cyberwareUserData.getCyberware(
			BrainUpgrades.THREAT_MATRIX.get().getDefaultInstance()
		);
		if (!itemStackThreatMatrix.isEmpty())
		{
			isMatrixWorking.put(entityLivingBase.getUUID(), cyberwareUserData.usePower(
				itemStackThreatMatrix,
				getPowerConsumption(itemStackThreatMatrix)
			));
		} else
		{
			isMatrixWorking.put(entityLivingBase.getUUID(), Boolean.FALSE);
		}

		ItemStack itemStackRadio = cyberwareUserData.getCyberware(BrainUpgrades.RADIO.get().getDefaultInstance());
		if (!itemStackRadio.isEmpty()
			&& EnableDisableHelper.isEnabled(itemStackRadio))
		{
			isRadioWorking.put(entityLivingBase.getUUID(), cyberwareUserData.usePower(
				itemStackRadio,
				getPowerConsumption(itemStackRadio)
			));
		} else
		{
			isRadioWorking.put(entityLivingBase.getUUID(), Boolean.FALSE);
		}
	}

	public static boolean isRadioWorking(LivingEntity entityLivingBase)
	{
		if (!isRadioWorking.containsKey(entityLivingBase.getUUID()))
		{
			isRadioWorking.put(entityLivingBase.getUUID(), Boolean.FALSE);
		}

		return isRadioWorking.get(entityLivingBase.getUUID());
	}

	private boolean isContextWorking(LivingEntity entityLivingBase)
	{
		if (!isContextWorking.containsKey(entityLivingBase.getUUID()))
		{
			isContextWorking.put(entityLivingBase.getUUID(), Boolean.FALSE);
		}

		return isContextWorking.get(entityLivingBase.getUUID());
	}

	private boolean isMatrixWorking(LivingEntity entityLivingBase)
	{
		if (!isMatrixWorking.containsKey(entityLivingBase.getUUID()))
		{
			isMatrixWorking.put(entityLivingBase.getUUID(), Boolean.FALSE);
		}

		return isMatrixWorking.get(entityLivingBase.getUUID());
	}

	public boolean isToolEffective(@Nonnull ItemStack tool, BlockState state)
	{
		return !tool.isEmpty() && tool.getItem().isCorrectToolForDrops(tool, state);
		//		if (!tool.isEmpty())
		//		{
		//			for (String toolType : tool.getItem().getToolClasses(tool))
		//			{
		//				if (state.getBlock().isToolEffective(toolType, state))
		//				{
		//					return true;
		//				}
		//			}
		//		}
		//		return false;
	}

	@SubscribeEvent
	public void handleXPDrop(LivingExperienceDropEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;

		if (cyberwareUserData.isCyberwareInstalled(BrainUpgrades.CORTICAL_STACK.get().getDefaultInstance())
			|| cyberwareUserData.isCyberwareInstalled(BrainUpgrades.CONSCIOUSNESS_TRANSMITTER.get().getDefaultInstance()))
		{
			event.setCanceled(true);
		}
	}

	private static final ArrayList<String> lastHits = new ArrayList<>();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleHurt(LivingAttackEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (!isMatrixWorking(entityLivingBase)) return;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;

		if (cyberwareUserData.isCyberwareInstalled(BrainUpgrades.THREAT_MATRIX.get().getDefaultInstance())
			&& !entityLivingBase.level.isClientSide()
			&& event.getSource() instanceof EntityDamageSource source)
		{
			Entity attacker = source.getEntity();

			if (entityLivingBase instanceof Player)
			{
				String str = entityLivingBase.getId() + " " + entityLivingBase.tickCount + " " + (attacker == null ?
					-1 :
					attacker.getId());
				if (lastHits.contains(str))
				{
					return;
				} else
				{
					lastHits.add(str);
				}
			}

			ArmorClass armorClass = ArmorClass.get(entityLivingBase);
			if (armorClass == ArmorClass.HEAVY) return;

			// TODO
			if ((float) entityLivingBase.hurtResistantTime <= (float) entityLivingBase.maxHurtResistantTime / 2.0F)
			{
				RandomSource random = entityLivingBase.getRandom();
				if (random.nextFloat() < (armorClass == ArmorClass.LIGHT ? LibConstants.DODGE_ARMOR :
					LibConstants.DODGE_NO_ARMOR))
				{
					event.setCanceled(true);
					// ???????????????
					entityLivingBase.hurtResistantTime = entityLivingBase.maxHurtResistantTime;
					entityLivingBase.hurtTime = entityLivingBase.maxHurtTime = 10;
					entityLivingBase.lastDamage = 9999F;

					var pos = entityLivingBase.position();
					CyberwarePacketHandler.INSTANCE.send(
						PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z,
							50,
							entityLivingBase.level.dimension()
						)),
						new DodgePacket(entityLivingBase.getId())
					);
				}
			}
		}
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		if (stack.is(BrainUpgrades.NEURAL_CONTEXTUALIZER.get())) return LibConstants.CONTEXTUALIZER_CONSUMPTION;
		if (stack.is(BrainUpgrades.THREAT_MATRIX.get())) return LibConstants.MATRIX_CONSUMPTION;
		if (stack.is(BrainUpgrades.RADIO.get())) return LibConstants.RADIO_CONSUMPTION;
		return 0;
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return stack.is(BrainUpgrades.ENDER_JAMMER.get())
			|| stack.is(BrainUpgrades.NEURAL_CONTEXTUALIZER.get())
			|| stack.is(BrainUpgrades.RADIO.get());
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

	private static final float[] f = {1.0F, 0.0F, 0.0F};

	@Override
	public float[] getColor(ItemStack stack)
	{
		return EnableDisableHelper.isEnabled(stack) ? f : null;
	}
}
