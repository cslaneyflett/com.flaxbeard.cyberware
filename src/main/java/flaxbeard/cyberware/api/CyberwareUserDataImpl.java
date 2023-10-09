package flaxbeard.cyberware.api;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.item.HotkeyHelper;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.BodyRegionEnum;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb.EnumSide;
import flaxbeard.cyberware.api.item.IHudjack;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.config.StartingStacksConfig;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.misc.NNLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyberwareUserDataImpl implements ICyberwareUserData
{
	//	public static final IStorage<ICyberwareUserData> STORAGE = new CyberwareUserDataStorage();
	private final NonNullList<NonNullList<ItemStack>> cyberwaresBySlot = NonNullList.create();
	private boolean[] missingEssentials = new boolean[BodyRegionEnum.values().length * 2];
	private int power_stored = 0;
	private int power_production = 0;
	private int power_lastProduction = 0;
	private int power_consumption = 0;
	private int power_lastConsumption = 0;
	private int power_capacity = 0;
	private Map<ItemStack, Integer> power_buffer = new HashMap<>();
	private Map<ItemStack, Integer> power_lastBuffer = new HashMap<>();
	private final NonNullList<ItemStack> nnlPowerOutages = NonNullList.create();
	private final List<Integer> ticksPowerOutages = new ArrayList<>();
	private int missingEssence = 0;
	private NonNullList<ItemStack> specialBatteries = NonNullList.create();
	private NonNullList<ItemStack> activeItems = NonNullList.create();
	private NonNullList<ItemStack> hudjackItems = NonNullList.create();
	private Map<Integer, ItemStack> hotkeys = new HashMap<>();
	private CompoundTag hudData;
	private boolean hasOpenedRadialMenu = false;
	private int hudColor = 0x00FFFF;
	private float[] hudColorFloat = new float[]{0.0F, 1.0F, 1.0F};

	public CyberwareUserDataImpl()
	{
		hudData = new CompoundTag();
		for (BodyRegionEnum slot : BodyRegionEnum.values())
		{
			NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
			for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
			{
				nnlCyberwaresInSlot.add(ItemStack.EMPTY);
			}
			cyberwaresBySlot.add(nnlCyberwaresInSlot);
		}
		resetWare(null);
	}

	@Override
	public void resetWare(LivingEntity LivingEntity)
	{
		for (NonNullList<ItemStack> nnlCyberwaresInSlot : cyberwaresBySlot)
		{
			for (ItemStack item : nnlCyberwaresInSlot)
			{
				if (CyberwareAPI.isCyberware(item))
				{
					CyberwareAPI.getCyberware(item).onRemoved(LivingEntity, item);
				}
			}
		}
		missingEssence = 0;
		for (BodyRegionEnum slot : BodyRegionEnum.values())
		{
			NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
			NonNullList<ItemStack> startItems = StartingStacksConfig.getStartingItems(slot);
			for (ItemStack startItem : startItems)
			{
				nnlCyberwaresInSlot.add(startItem.copy());
			}
			cyberwaresBySlot.set(slot.ordinal(), nnlCyberwaresInSlot);
		}
		missingEssentials = new boolean[BodyRegionEnum.values().length * 2];
		updateCapacity();
	}

	@Override
	public List<ItemStack> getPowerOutages()
	{
		return nnlPowerOutages;
	}

	@Override
	public List<Integer> getPowerOutageTimes()
	{
		return ticksPowerOutages;
	}

	@Override
	public int getCapacity()
	{
		int specialCap = 0;
		for (ItemStack item : specialBatteries)
		{
			ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(item);
			specialCap += battery.getCapacity(item);
		}
		return power_capacity + specialCap;
	}

	@Override
	public int getStoredPower()
	{
		int specialStored = 0;
		for (ItemStack item : specialBatteries)
		{
			ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(item);
			specialStored += battery.getStoredEnergy(item);
		}
		return power_stored + specialStored;
	}

	@Override
	public float getPercentFull()
	{
		if (getCapacity() == 0) return -1F;
		return getStoredPower() / (float) getCapacity();
	}

	@Override
	public boolean isAtCapacity(ItemStack stack)
	{
		return isAtCapacity(stack, 0);
	}

	@Override
	public boolean isAtCapacity(ItemStack stack, int buffer)
	{
		// buffer = Math.min(power_capacity - 1, buffer); TODO
		int leftOverSpaceNormal = power_capacity - power_stored;

		if (leftOverSpaceNormal > buffer) return false;

		int leftOverSpaceSpecial = 0;

		for (ItemStack batteryStack : specialBatteries)
		{
			ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(batteryStack);
			int spaceInThisSpecial = battery.add(batteryStack, stack, buffer + 1, true);
			leftOverSpaceSpecial += spaceInThisSpecial;

			if (leftOverSpaceNormal + leftOverSpaceSpecial > buffer) return false;
		}

		return true;
	}

	@Override
	public void addPower(int amount, ItemStack inputter)
	{
		if (amount < 0)
		{
			throw new IllegalArgumentException("Amount must be positive!");
		}

		ItemStack stack = ItemStack.EMPTY;
		if (!inputter.isEmpty())
		{
			if (inputter.hasTag()
				|| inputter.getCount() != 1)
			{
				stack = new ItemStack(inputter.getItem(), 1, CyberwareItemMetadata.copy(inputter));
			} else
			{
				stack = inputter;
			}
		}

		Integer amountExisting = power_buffer.get(stack);
		power_buffer.put(stack, amount + (amountExisting == null ? 0 : amountExisting));

		power_production += amount;
	}

	private boolean canGiveOut = true;

	@Override
	public boolean usePower(ItemStack stack, int amount)
	{
		return usePower(stack, amount, true);
	}

	private int ComputeSum(@Nonnull Map<ItemStack, Integer> map)
	{
		int total = 0;
		for (ItemStack key : map.keySet())
		{
			total += map.get(key);
		}
		return total;
	}

	private void subtractFromBufferLast(int amount)
	{
		for (ItemStack key : power_lastBuffer.keySet())
		{
			int get = power_lastBuffer.get(key);
			int amountToSubtract = Math.min(get, amount);
			amount -= amountToSubtract;
			power_lastBuffer.put(key, get - amountToSubtract);
			if (amount <= 0) break;
		}
	}

	@Override
	public boolean usePower(ItemStack stack, int amount, boolean isPassive)
	{
		if (isImmune) return true;

		if (!canGiveOut)
		{
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> setOutOfPower(stack));
			return false;
		}

		power_consumption += amount;

		int sumPowerBufferLast = ComputeSum(power_lastBuffer);
		int amountAvailable = power_stored + sumPowerBufferLast;

		int amountAvailableSpecial = 0;
		if (amountAvailable < amount)
		{
			int amountMissing = amount - amountAvailable;

			for (ItemStack batteryStack : specialBatteries)
			{
				ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(batteryStack);
				int extract = battery.extract(batteryStack, amountMissing, true);

				amountMissing -= extract;
				amountAvailableSpecial += extract;

				if (amountMissing <= 0) break;
			}

			if (amountAvailableSpecial + amountAvailable >= amount)
			{
				amountMissing = amount - amountAvailable;

				for (ItemStack batteryStack : specialBatteries)
				{
					ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(batteryStack);
					int extract = battery.extract(batteryStack, amountMissing, false);

					amountMissing -= extract;

					if (amountMissing <= 0) break;
				}

				amount -= amountAvailableSpecial;
			}
		}

		if (amountAvailable < amount)
		{
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> setOutOfPower(stack));
			if (isPassive)
			{
				canGiveOut = false;
			}
			return false;
		}

		int leftAfterBuffer = Math.max(0, amount - sumPowerBufferLast);
		subtractFromBufferLast(amount);
		power_stored -= leftAfterBuffer;

		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void setOutOfPower(ItemStack stack)
	{
		Player entityPlayer = Minecraft.getInstance().player;
		if (entityPlayer == null || stack.isEmpty()) return;

		int indexFound = NNLUtil.findIndex(stack, nnlPowerOutages);
		if (indexFound != -1)
		{
			nnlPowerOutages.remove(indexFound);
			ticksPowerOutages.remove(indexFound);
		}

		nnlPowerOutages.add(stack);
		ticksPowerOutages.add(entityPlayer.tickCount);

		if (nnlPowerOutages.size() >= 8)
		{
			nnlPowerOutages.remove(0);
			ticksPowerOutages.remove(0);
		}
	}

	@Override
	public NonNullList<ItemStack> getInstalledCyberware(BodyRegionEnum slot)
	{
		return cyberwaresBySlot.get(slot.ordinal());
	}

	@Override
	public boolean hasEssential(BodyRegionEnum slot)
	{
		return !missingEssentials[slot.ordinal() * 2];
	}

	@Override
	public boolean hasEssential(BodyRegionEnum slot, EnumSide side)
	{
		return !missingEssentials[slot.ordinal() * 2 + (side == EnumSide.LEFT ? 0 : 1)];
	}

	@Override
	public void setHasEssential(BodyRegionEnum slot, boolean hasLeft, boolean hasRight)
	{
		missingEssentials[slot.ordinal() * 2] = !hasLeft;
		missingEssentials[slot.ordinal() * 2 + 1] = !hasRight;
	}

	@Override
	public void setInstalledCyberware(LivingEntity LivingEntity, BodyRegionEnum slot,
									  @Nonnull List<ItemStack> cyberwaresToInstall)
	{
		while (cyberwaresToInstall.size() > LibConstants.WARE_PER_SLOT)
		{
			cyberwaresToInstall.remove(cyberwaresToInstall.size() - 1);
		}
		while (cyberwaresToInstall.size() < LibConstants.WARE_PER_SLOT)
		{
			cyberwaresToInstall.add(ItemStack.EMPTY);
		}
		setInstalledCyberware(LivingEntity, slot, NNLUtil.fromArray(cyberwaresToInstall.toArray(new ItemStack[0])));
	}

	@Override
	public void updateCapacity()
	{
		power_capacity = 0;
		specialBatteries = NonNullList.create();
		activeItems = NonNullList.create();
		hudjackItems = NonNullList.create();
		hotkeys = new HashMap<>();

		for (BodyRegionEnum slot : BodyRegionEnum.values())
		{
			for (ItemStack itemStackCyberware : getInstalledCyberware(slot))
			{
				if (CyberwareAPI.isCyberware(itemStackCyberware))
				{
					ICyberware cyberware = CyberwareAPI.getCyberware(itemStackCyberware);

					if (cyberware instanceof IMenuItem
						&& ((IMenuItem) cyberware).hasMenu(itemStackCyberware))
					{
						activeItems.add(itemStackCyberware);

						int hotkey = HotkeyHelper.getHotkey(itemStackCyberware);
						if (hotkey != -1)
						{
							hotkeys.put(hotkey, itemStackCyberware);
						}
					}

					if (cyberware instanceof IHudjack)
					{
						hudjackItems.add(itemStackCyberware);
					}

					if (cyberware instanceof ISpecialBattery)
					{
						specialBatteries.add(itemStackCyberware);
					} else
					{
						power_capacity += cyberware.getPowerCapacity(itemStackCyberware);
					}
				}
			}
		}

		power_stored = Math.min(power_stored, power_capacity);
	}

	@Override
	public void setInstalledCyberware(LivingEntity LivingEntity, BodyRegionEnum slot,
									  NonNullList<ItemStack> cyberwaresToInstall)
	{
		if (cyberwaresToInstall.size() != cyberwaresBySlot.get(slot.ordinal()).size())
		{
			Cyberware.logger.error(String.format(
				"Invalid number of cyberware to install: found %d, expecting %d",
				cyberwaresToInstall.size(),
				cyberwaresBySlot.get(slot.ordinal()).size()
			));
		}
		NonNullList<ItemStack> cyberwaresInstalled = cyberwaresBySlot.get(slot.ordinal());

		if (LivingEntity != null)
		{
			for (ItemStack itemStackInstalled : cyberwaresInstalled)
			{
				if (!CyberwareAPI.isCyberware(itemStackInstalled)) continue;

				boolean found = false;
				for (ItemStack itemStackToInstall : cyberwaresToInstall)
				{
					if (CyberwareAPI.areCyberwareStacksEqual(itemStackToInstall, itemStackInstalled)
						&& itemStackToInstall.getCount() == itemStackInstalled.getCount())
					{
						found = true;
						break;
					}
				}

				if (!found)
				{
					CyberwareAPI.getCyberware(itemStackInstalled).onRemoved(LivingEntity, itemStackInstalled);
				}
			}

			for (ItemStack itemStackToInstall : cyberwaresToInstall)
			{
				if (!CyberwareAPI.isCyberware(itemStackToInstall)) continue;

				boolean found = false;
				for (ItemStack oldWare : cyberwaresInstalled)
				{
					if (CyberwareAPI.areCyberwareStacksEqual(itemStackToInstall, oldWare)
						&& itemStackToInstall.getCount() == oldWare.getCount())
					{
						found = true;
						break;
					}
				}

				if (!found)
				{
					CyberwareAPI.getCyberware(itemStackToInstall).onAdded(LivingEntity, itemStackToInstall);
				}
			}
		}

		cyberwaresBySlot.set(slot.ordinal(), cyberwaresToInstall);
	}

	@Override
	public boolean isCyberwareInstalled(ItemStack cyberware)
	{
		return getCyberwareRank(cyberware) > 0;
	}

	@Override
	public int getCyberwareRank(ItemStack cyberwareTemplate)
	{
		ItemStack cyberwareFound = getCyberware(cyberwareTemplate);

		if (!cyberwareFound.isEmpty())
		{
			return cyberwareFound.getCount();
		}

		return 0;
	}

	@Override
	public ItemStack getCyberware(ItemStack cyberware)
	{
		for (ItemStack itemStack : getInstalledCyberware(CyberwareAPI.getCyberware(cyberware).getSlot(cyberware)))
		{
			if (!itemStack.isEmpty()
				&& itemStack.getItem() == cyberware.getItem()
				&& CyberwareItemMetadata.identical(itemStack, cyberware)
			)
			{
				return itemStack;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tagCompound = new CompoundTag();
		ListTag listSlots = new ListTag();

		for (BodyRegionEnum slot : BodyRegionEnum.values())
		{
			ListTag listCyberwares = new ListTag();
			for (ItemStack cyberware : getInstalledCyberware(slot))
			{
				CompoundTag tagCompoundCyberware = new CompoundTag();
				if (!cyberware.isEmpty())
				{
					cyberware.setTag(tagCompoundCyberware);
				}
				listCyberwares.add(tagCompoundCyberware);
			}
			listSlots.add(listCyberwares);
		}

		tagCompound.put("cyberware", listSlots);

		ListTag listEssentials = new ListTag();
		for (boolean missingEssential : missingEssentials)
		{
			listEssentials.add(ByteTag.valueOf((byte) (missingEssential ? 1 : 0)));
		}
		tagCompound.put("discard", listEssentials);
		tagCompound.put("powerBuffer", serializeMap(power_buffer));
		tagCompound.put("powerBufferLast", serializeMap(power_lastBuffer));
		tagCompound.putInt("powerCap", power_capacity);
		tagCompound.putInt("storedPower", power_stored);
		tagCompound.putInt("missingEssence", missingEssence);
		tagCompound.put("hud", hudData);
		tagCompound.putInt("color", hudColor);
		tagCompound.putBoolean("hasOpenedRadialMenu", hasOpenedRadialMenu);
		return tagCompound;
	}

	private ListTag serializeMap(@Nonnull Map<ItemStack, Integer> map)
	{
		ListTag listMap = new ListTag();

		for (ItemStack stack : map.keySet())
		{
			CompoundTag tagCompoundEntry = new CompoundTag();
			tagCompoundEntry.putBoolean("null", stack.isEmpty());
			if (!stack.isEmpty())
			{
				CompoundTag tagCompoundItem = new CompoundTag();
				stack.setTag(tagCompoundItem);
				tagCompoundEntry.put("item", tagCompoundItem);
			}
			tagCompoundEntry.putInt("value", map.get(stack));

			listMap.add(tagCompoundEntry);
		}

		return listMap;
	}

	private Map<ItemStack, Integer> deserializeMap(@Nonnull ListTag listMap)
	{
		Map<ItemStack, Integer> map = new HashMap<>();
		for (int index = 0; index < listMap.size(); index++)
		{
			CompoundTag tagCompoundEntry = listMap.getCompound(index);
			boolean isNull = tagCompoundEntry.getBoolean("null");

			ItemStack stack = ItemStack.EMPTY;
			if (!isNull)
			{
				stack = ItemStack.of(tagCompoundEntry.getCompound("item"));
			}

			map.put(stack, tagCompoundEntry.getInt("value"));
		}

		return map;
	}

	@Override
	public void deserializeNBT(CompoundTag tagCompound)
	{
		power_buffer = deserializeMap(tagCompound.getList("powerBuffer", Tag.TAG_COMPOUND));
		power_capacity = tagCompound.getInt("powerCap");
		power_lastBuffer = deserializeMap(tagCompound.getList("powerBufferLast", Tag.TAG_COMPOUND));

		power_stored = tagCompound.getInt("storedPower");
		if (tagCompound.contains("essence"))
		{
			missingEssence = getMaxEssence() - tagCompound.getInt("essence");
		} else
		{
			missingEssence = tagCompound.getInt("missingEssence");
		}

		hudData = tagCompound.getCompound("hud");
		hasOpenedRadialMenu = tagCompound.getBoolean("hasOpenedRadialMenu");

		ListTag listEssentials = tagCompound.getList("discard", Tag.TAG_BYTE);
		for (int indexEssential = 0; indexEssential < listEssentials.size(); indexEssential++)
		{
			missingEssentials[indexEssential] = ((ByteTag) listEssentials.get(indexEssential)).getAsByte() > 0;
		}

		ListTag listSlots = tagCompound.getList("cyberware", Tag.TAG_LIST);
		for (int indexBodySlot = 0; indexBodySlot < listSlots.size(); indexBodySlot++)
		{
			BodyRegionEnum slot = BodyRegionEnum.values()[indexBodySlot];

			ListTag listCyberwares = (ListTag) listSlots.get(indexBodySlot);
			NonNullList<ItemStack> nnlCyberwaresOfType = NonNullList.create();
			for (int indexInventorySlot = 0; indexInventorySlot < LibConstants.WARE_PER_SLOT; indexInventorySlot++)
			{
				nnlCyberwaresOfType.add(ItemStack.EMPTY);
			}

			int countInventorySlots = Math.min(listCyberwares.size(), nnlCyberwaresOfType.size());
			for (int indexInventorySlot = 0; indexInventorySlot < countInventorySlots; indexInventorySlot++)
			{
				nnlCyberwaresOfType.set(
					indexInventorySlot,
					ItemStack.of(listCyberwares.getCompound(indexInventorySlot))
				);
			}

			setInstalledCyberware(null, slot, nnlCyberwaresOfType);
		}

		int color = 0x00FFFF;

		if (tagCompound.contains("color"))
		{
			color = tagCompound.getInt("color");
		}
		setHudColor(color);

		updateCapacity();
	}

	// TODO: storage, dont know quite how to do this but see https://docs.minecraftforge.net/en/1.19.x/datastorage/capabilities/#persisting-across-player-deaths
	//	private static class CyberwareUserDataStorage implements IStorage<ICyberwareUserData> {
	//		@Override
	//		public Tag writeNBT(Capability<ICyberwareUserData> capability, ICyberwareUserData cyberwareUserData,
	//		Direction side) {
	//			return cyberwareUserData.serializeNBT();
	//		}
	//
	//		@Override
	//		public void readNBT(Capability<ICyberwareUserData> capability, ICyberwareUserData cyberwareUserData,
	//		Direction side, Tag nbt) {
	//			if (nbt instanceof CompoundTag) {
	//				cyberwareUserData.deserializeNBT((CompoundTag) nbt);
	//			} else {
	//				throw new IllegalStateException("Cyberware NBT should be a CompoundTag!");
	//			}
	//		}
	//	}

	public static class Provider implements ICapabilitySerializable<CompoundTag>
	{
		public static final ResourceLocation NAME = new ResourceLocation(Cyberware.MODID, "cyberware");
		private final LazyOptional<ICyberwareUserData> cyberwareUserData = LazyOptional.of(CyberwareUserDataImpl::new);

		// TODO?
		//		@Override
		//		public boolean hasCapability(@Nonnull Capability<?> capability, Direction facing)
		//		{
		//			return capability == CyberwareAPI.CYBERWARE_CAPABILITY;
		//		}

		@Override
		public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
														  final @Nullable Direction facing)
		{
			if (capability == CyberwareAPI.CYBERWARE_CAPABILITY)
			{
				return cyberwareUserData.cast();
			}

			return LazyOptional.empty();
		}

		@Override
		public CompoundTag serializeNBT()
		{
			return cyberwareUserData.resolve().orElseThrow().serializeNBT();
		}

		@Override
		public void deserializeNBT(CompoundTag tagCompound)
		{
			cyberwareUserData.resolve().orElseThrow().deserializeNBT(tagCompound);
		}
	}

	private void storePower(Map<ItemStack, Integer> map)
	{
		for (ItemStack itemStackSpecialBattery : specialBatteries)
		{
			ISpecialBattery specialBattery = (ISpecialBattery) CyberwareAPI.getCyberware(itemStackSpecialBattery);
			for (Map.Entry<ItemStack, Integer> entryBuffer : map.entrySet())
			{
				int amountBuffer = entryBuffer.getValue();
				int amountTaken = specialBattery.add(itemStackSpecialBattery, entryBuffer.getKey(), amountBuffer,
					false
				);
				entryBuffer.setValue(amountBuffer - amountTaken);
			}
		}
		power_stored = Math.min(power_capacity, power_stored + ComputeSum(map));
	}

	@Override
	public void resetBuffer()
	{
		canGiveOut = true;
		storePower(power_lastBuffer);
		power_lastBuffer = power_buffer;
		power_buffer = new HashMap<>(power_buffer.size());
		isImmune = false;

		power_lastConsumption = power_consumption;
		power_lastProduction = power_production;
		power_production = 0;
		power_consumption = 0;
	}

	@Override
	public void setImmune()
	{
		isImmune = true;
	}

	private boolean isImmune = false;

	@Override
	@Deprecated
	public int getEssence()
	{
		return getMaxEssence() - missingEssence;
	}

	@Override
	@Deprecated
	public int getMaxEssence()
	{
		return CyberwareConfig.INSTANCE.ESSENCE.get();
	}

	@Override
	@Deprecated
	public void setEssence(int essence)
	{
		missingEssence = getMaxEssence() - essence;
	}

	@Override
	public int getMaxTolerance(@Nonnull LivingEntity LivingEntity)
	{
		return (int) LivingEntity.getAttributes().getInstance(CyberwareAPI.TOLERANCE_ATTR).getValue();
	}

	@Override
	public int getTolerance(@Nonnull LivingEntity LivingEntity)
	{
		return getMaxTolerance(LivingEntity) - missingEssence;
	}

	@Override
	public void setTolerance(@Nonnull LivingEntity LivingEntity, int amount)
	{
		missingEssence = getMaxTolerance(LivingEntity) - amount;
	}

	@Override
	public int getNumActiveItems()
	{
		return activeItems.size();
	}

	@Override
	public List<ItemStack> getActiveItems()
	{
		return activeItems;
	}

	@Override
	public List<ItemStack> getHudjackItems()
	{
		return hudjackItems;
	}

	@Override
	public void removeHotkey(int i)
	{
		hotkeys.remove(i);
	}

	@Override
	public void addHotkey(int i, ItemStack stack)
	{
		hotkeys.put(i, stack);
	}

	@Override
	public ItemStack getHotkey(int i)
	{
		if (!hotkeys.containsKey(i))
		{
			return ItemStack.EMPTY;
		}
		return hotkeys.get(i);
	}

	@Override
	public Iterable<Integer> getHotkeys()
	{
		return hotkeys.keySet();
	}

	@Override
	public void setHudData(CompoundTag tagCompound)
	{
		hudData = tagCompound;
	}

	@Override
	public CompoundTag getHudData()
	{
		return hudData;
	}

	@Override
	public boolean hasOpenedRadialMenu()
	{
		return hasOpenedRadialMenu;
	}

	@Override
	public void setOpenedRadialMenu(boolean hasOpenedRadialMenu)
	{
		this.hasOpenedRadialMenu = hasOpenedRadialMenu;
	}

	@Override
	public void setHudColor(int hexVal)
	{
		float r = ((hexVal >> 16) & 0x0000FF) / 255F;
		float g = ((hexVal >> 8) & 0x0000FF) / 255F;
		float b = ((hexVal) & 0x0000FF) / 255F;
		setHudColor(new float[]{r, g, b});
	}

	@Override
	public int getHudColorHex()
	{
		return hudColor;
	}

	@Override
	public void setHudColor(float[] color)
	{
		hudColorFloat = color;
		int ri = Math.round(color[0] * 255);
		int gi = Math.round(color[1] * 255);
		int bi = Math.round(color[2] * 255);

		int rp = (ri << 16) & 0xFF0000;
		int gp = (gi << 8) & 0x00FF00;
		int bp = (bi) & 0x0000FF;
		hudColor = rp | gp | bp;
	}

	@Override
	public float[] getHudColor()
	{
		return hudColorFloat;
	}

	@Override
	public int getProduction()
	{
		return power_lastProduction;
	}

	@Override
	public int getConsumption()
	{
		return power_lastConsumption;
	}
}
