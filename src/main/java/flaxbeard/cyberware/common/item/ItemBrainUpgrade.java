package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.ArmorClass;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.DodgePacket;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.EnumHand;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.*;

public class ItemBrainUpgrade extends ItemCyberware implements IMenuItem
{
	public static final int META_CORTICAL_STACK = 0;
	public static final int META_ENDER_JAMMER = 1;
	public static final int META_CONSCIOUSNESS_TRANSMITTER = 2;
	public static final int META_NEURAL_CONTEXTUALIZER = 3;
	public static final int META_THREAT_MATRIX = 4;
	public static final int META_RADIO = 5;

	public ItemBrainUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return other.getItem() == this
			&& CyberwareItemMetadata.matches(stack, META_CORTICAL_STACK)
			&& CyberwareItemMetadata.matches(other, META_CONSCIOUSNESS_TRANSMITTER);
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

		ItemStack itemStackJammer = CyberwareContent.brainUpgrades.getCachedStack(ItemBrainUpgrade.META_ENDER_JAMMER);

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

			if (entityPlayerOriginal.level.getGameRules().getBoolean("keepInventory"))
			{
				return;
			}

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayerOriginal);
			if (cyberwareUserData == null) return;

			if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_CORTICAL_STACK)))
			{
				if (!entityPlayerOriginal.level.isClientSide())
				{
					ItemStack stack = new ItemStack(CyberwareContent.expCapsule);
					CompoundTag tagCompound = new CompoundTag();
					tagCompound.putInt("xp", entityPlayerOriginal.totalExperience);
					stack.setTag(tagCompound);
					var pos = entityPlayerOriginal.position();
					// TODO: uhhhh
					//					BlockEntity item = BlockEntity.loadStatic(new BlockPos(pos.x, pos.y, pos.z),
					//					null);
					//					entityPlayerOriginal.level.setBlockEntity(item);
				}
			} else if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_CONSCIOUSNESS_TRANSMITTER)))
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

		ItemStack itemStackNeuralContextualizer =
			cyberwareUserData.getCyberware(getCachedStack(META_NEURAL_CONTEXTUALIZER));
		if (!itemStackNeuralContextualizer.isEmpty()
			&& EnableDisableHelper.isEnabled(itemStackNeuralContextualizer)
			&& isContextWorking(entityPlayer)
			&& !entityPlayer.isShiftKeyDown())
		{
			BlockState state = event.getState();
			ItemStack tool = entityPlayer.getHeldItem(EnumHand.MAIN_HAND);

			if (!tool.isEmpty()
				&& (tool.getItem() instanceof ItemSword
				|| tool.getItem().getTranslationKey().contains("sword")))
			{
				return;
			}

			if (isToolEffective(tool, state)) return;

			for (int indexSlot = 0; indexSlot < 10; indexSlot++)
			{
				if (indexSlot != entityPlayer.inventory.currentItem)
				{
					ItemStack potentialTool = entityPlayer.inventory.mainInventory.get(indexSlot);
					if (isToolEffective(potentialTool, state))
					{
						entityPlayer.inventory.currentItem = indexSlot;
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
			cyberwareUserData.getCyberware(getCachedStack(META_NEURAL_CONTEXTUALIZER));
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

		ItemStack itemStackThreatMatrix = cyberwareUserData.getCyberware(getCachedStack(META_THREAT_MATRIX));
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

		ItemStack itemStackRadio = cyberwareUserData.getCyberware(getCachedStack(META_RADIO));
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

	public boolean isToolEffective(ItemStack tool, BlockState state)
	{
		if (!tool.isEmpty())
		{
			for (String toolType : tool.getItem().getToolClasses(tool))
			{
				if (state.getBlock().isToolEffective(toolType, state))
				{
					return true;
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	public void handleXPDrop(LivingExperienceDropEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;

		if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_CORTICAL_STACK))
			|| cyberwareUserData.isCyberwareInstalled(getCachedStack(META_CONSCIOUSNESS_TRANSMITTER)))
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

		if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_THREAT_MATRIX))
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

					//					CyberwarePacketHandler.INSTANCE.sendToAllAround(new DodgePacket
					//					(entityLivingBase.getId()),
					//							new TargetPoint(entityLivingBase.level.provider.getDimension(),
					//							entityLivingBase.posX, entityLivingBase.posY, entityLivingBase.posZ,
					//							50));
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
	public int getPowerConsumption(ItemStack stack)
	{
		return switch (CyberwareItemMetadata.get(stack))
		{
			case META_NEURAL_CONTEXTUALIZER -> LibConstants.CONTEXTUALIZER_CONSUMPTION;
			case META_THREAT_MATRIX -> LibConstants.MATRIX_CONSUMPTION;
			case META_RADIO -> LibConstants.RADIO_CONSUMPTION;
			default -> 0;
		};
	}

	private final static int[] menuFlags = {META_ENDER_JAMMER, META_NEURAL_CONTEXTUALIZER, META_RADIO};

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return CyberwareItemMetadata.predicate(stack, (int t) -> ArrayUtils.contains(menuFlags, t));
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
