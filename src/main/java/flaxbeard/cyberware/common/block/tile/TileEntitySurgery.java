package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareSurgeryEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb.EnumSide;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.BlockSurgeryChamber;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.item.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.registry.BlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class TileEntitySurgery extends BlockEntity
{
	public ItemStackHandler slotsPlayer = new ItemStackHandler(120);
	public ItemStackHandler slots = new ItemStackHandler(120);
	public boolean[] discardSlots = new boolean[120];
	public boolean[] isEssentialMissing = new boolean[EnumSlot.values().length * 2];
	public int essence = 0;
	public int maxEssence = 0;
	public int wrongSlot = -1;
	public int ticksWrong = 0;
	public int lastEntity = -1;
	public int cooldownTicks = 0;
	public boolean missingPower = false;

	public TileEntitySurgery(BlockPos pPos, BlockState pBlockState)
	{
		super(BlockEntities.SURGERY.get(), pPos, pBlockState);
	}

	public boolean isUsableByPlayer(Player entityPlayer)
	{
		assert this.level != null;
		return this.level.getBlockEntity(worldPosition) == this
			&& entityPlayer.position().distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
			worldPosition.getZ() + 0.5D
		) <= 64.0D;
	}

	public void updatePlayerSlots(LivingEntity entityLivingBase, ICyberwareUserData cyberwareUserData)
	{
		setChanged();

		if (cyberwareUserData != null)
		{
			if (entityLivingBase.getId() != lastEntity)
			{
				Arrays.fill(discardSlots, false);
				lastEntity = entityLivingBase.getId();
			}
			maxEssence = cyberwareUserData.getMaxTolerance(entityLivingBase);

			// Update slotsPlayer with the items in the player's body
			for (EnumSlot slot : EnumSlot.values())
			{
				NonNullList<ItemStack> cyberwares = cyberwareUserData.getInstalledCyberware(slot);
				for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
				{
					ItemStack toPut = cyberwares.get(indexSlot).copy();

					// If there's a new item, don't set it to discard by default unless it conflicts
					if (!ItemStack.tagMatches(
						toPut,
						slotsPlayer.getStackInSlot(slot.ordinal() * LibConstants.WARE_PER_SLOT + indexSlot)
					))
					{
						discardSlots[slot.ordinal() * LibConstants.WARE_PER_SLOT + indexSlot] = doesItemConflict(toPut
							, slot, indexSlot);
					}
					slotsPlayer.setStackInSlot(slot.ordinal() * LibConstants.WARE_PER_SLOT + indexSlot, toPut);
				}
				updateEssential(slot);
			}

			// Check for items with requirements that are no longer fulfilled
			boolean needToCheck = true;
			while (needToCheck)
			{
				needToCheck = false;
				for (EnumSlot slot : EnumSlot.values())
				{
					for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
					{
						int index = slot.ordinal() * LibConstants.WARE_PER_SLOT + indexSlot;

						ItemStack stack = slots.getStackInSlot(index);
						if (!stack.isEmpty()
							&& !areRequirementsFulfilled(stack, slot, indexSlot))
						{
							addItemStack(entityLivingBase, stack);
							slots.setStackInSlot(index, ItemStack.EMPTY);
							needToCheck = true;
						}
					}
				}
			}

			this.updateEssence();
		} else
		{
			slotsPlayer = new ItemStackHandler(120);
			this.maxEssence = CyberwareConfig.INSTANCE.ESSENCE.get();
			for (EnumSlot slot : EnumSlot.values())
			{
				updateEssential(slot);
			}
		}
		wrongSlot = -1;
	}

	public boolean doesItemConflict(@Nonnull ItemStack stack, EnumSlot slot, int indexSlotToCheck)
	{
		int row = slot.ordinal();
		if (!stack.isEmpty())
		{
			for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
			{
				if (indexSlot != indexSlotToCheck)
				{
					int index = row * LibConstants.WARE_PER_SLOT + indexSlot;
					ItemStack slotStack = slots.getStackInSlot(index);
					ItemStack playerStack = slotsPlayer.getStackInSlot(index);

					ItemStack otherStack = !slotStack.isEmpty() ? slotStack : (discardSlots[index] ? ItemStack.EMPTY :
						playerStack);

					// Automatically incompatible with the same item/damage. Doesn't use areCyberwareStacksEqual
					// because items conflict even if different grades.
					if (!otherStack.isEmpty() &&
						otherStack.getItem() == stack.getItem() &&
						CyberwareItemMetadata.identical(stack, otherStack)
					)
					{
						setWrongSlot(index);
						return true;
					}

					// Incompatible if either stack doesn't like the other one
					if (!otherStack.isEmpty() && CyberwareAPI.getCyberware(otherStack).isIncompatible(
						otherStack,
						stack
					))
					{
						setWrongSlot(index);
						return true;
					}
					if (!otherStack.isEmpty() && CyberwareAPI.getCyberware(stack).isIncompatible(stack, otherStack))
					{
						setWrongSlot(index);
						return true;
					}
				}
			}
		}

		return false;
	}

	public void setWrongSlot(int index)
	{
		// TODO: wrong
		this.wrongSlot = index;
		//		Cyberware.proxy.wrong(this);
	}

	public void disableDependants(ItemStack stack, EnumSlot slot, int indexSlotToCheck)
	{
		int row = slot.ordinal();
		if (!stack.isEmpty())
		{
			for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
			{
				if (indexSlot != indexSlotToCheck)
				{
					int index = row * LibConstants.WARE_PER_SLOT + indexSlot;
					ItemStack playerStack = slotsPlayer.getStackInSlot(index);

					if (!areRequirementsFulfilled(playerStack, slot, indexSlotToCheck))
					{
						discardSlots[index] = true;
					}
				}
			}
		}
	}

	public void enableDependsOn(ItemStack stack, EnumSlot slot, int indexSlotToCheck)
	{
		if (!stack.isEmpty())
		{
			ICyberware ware = CyberwareAPI.getCyberware(stack);
			for (NonNullList<ItemStack> neededItem : ware.required(stack))
			{
				boolean found = false;

				outerLoop:
				for (ItemStack needed : neededItem)
				{
					for (int row = 0; row < EnumSlot.values().length; row++)
					{
						for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
						{
							if (indexSlot != indexSlotToCheck)
							{
								int index = row * LibConstants.WARE_PER_SLOT + indexSlot;
								ItemStack playerStack = slotsPlayer.getStackInSlot(index);

								if (!playerStack.isEmpty()
									&& playerStack.getItem() == needed.getItem()
									&& CyberwareItemMetadata.identical(playerStack, needed)
								)
								{
									found = true;
									discardSlots[index] = false;
									break outerLoop;
								}
							}
						}
					}
				}
				if (!found)
				{
					Cyberware.logger.error(String.format("Can't find required %s for %s in %s:%d",
						neededItem, stack, slot, indexSlotToCheck
					));
				}
			}
		}
	}

	public boolean canDisableItem(ItemStack stack, EnumSlot slot, int indexSlotToCheck)
	{
		if (!stack.isEmpty())
		{
			for (int row = 0; row < EnumSlot.values().length; row++)
			{
				for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
				{
					if (indexSlot != indexSlotToCheck)
					{
						int index = row * LibConstants.WARE_PER_SLOT + indexSlot;
						ItemStack slotStack = slots.getStackInSlot(index);
						ItemStack playerStack = ItemStack.EMPTY;

						ItemStack otherStack = !slotStack.isEmpty()
							? slotStack
							: (
								discardSlots[index]
									? ItemStack.EMPTY
									: playerStack
							);

						if (!areRequirementsFulfilled(otherStack, slot, indexSlotToCheck))
						{
							setWrongSlot(index);
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public boolean areRequirementsFulfilled(ItemStack stack, EnumSlot ignoredSlot, int indexSlotToCheck)
	{
		if (!stack.isEmpty())
		{
			ICyberware ware = CyberwareAPI.getCyberware(stack);
			for (NonNullList<ItemStack> neededItem : ware.required(stack))
			{
				boolean found = false;

				outerLoop:
				for (ItemStack needed : neededItem)
				{
					for (int row = 0; row < EnumSlot.values().length; row++)
					{
						for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
						{
							if (indexSlot != indexSlotToCheck)
							{
								int index = row * LibConstants.WARE_PER_SLOT + indexSlot;
								ItemStack slotStack = slots.getStackInSlot(index);
								ItemStack playerStack = slotsPlayer.getStackInSlot(index);

								ItemStack otherStack = !slotStack.isEmpty() ? slotStack : (discardSlots[index] ?
									ItemStack.EMPTY :
									playerStack);

								if (!otherStack.isEmpty() && otherStack.getItem() == needed.getItem() && CyberwareItemMetadata.identical(otherStack, needed))
								{
									found = true;
									break outerLoop;
								}
							}
						}
					}
				}
				if (!found) return false;
			}
		}

		return true;
	}

	@Override
	public void load(@Nonnull CompoundTag tagCompound)
	{
		super.load(tagCompound);

		slots.deserializeNBT(tagCompound.getCompound("inv"));
		slotsPlayer.deserializeNBT(tagCompound.getCompound("inv2"));

		ListTag list = tagCompound.getList("discard", ByteTag.TAG_BYTE);
		for (int indexEssential = 0; indexEssential < list.size(); indexEssential++)
		{
			this.discardSlots[indexEssential] = ((ByteTag) list.get(indexEssential)).getAsByte() > 0;
		}

		this.essence = tagCompound.getInt("essence");
		this.maxEssence = tagCompound.getInt("maxEssence");
		this.lastEntity = tagCompound.getInt("lastEntity");
		this.missingPower = tagCompound.getBoolean("missingPower");
	}

	@Override
	public void saveAdditional(@Nonnull CompoundTag tagCompound)
	{
		super.saveAdditional(tagCompound);

		tagCompound.putInt("essence", essence);
		tagCompound.putInt("maxEssence", maxEssence);
		tagCompound.putInt("lastEntity", lastEntity);
		tagCompound.putBoolean("missingPower", missingPower);

		tagCompound.put("inv", this.slots.serializeNBT());
		tagCompound.put("inv2", this.slotsPlayer.serializeNBT());

		ListTag list = new ListTag();
		for (boolean discardSlot : this.discardSlots)
		{
			list.add(ByteTag.valueOf((byte) (discardSlot ? 1 : 0)));
		}
		tagCompound.put("discard", list);
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag()
	{
		var tag = new CompoundTag();
		saveAdditional(tag);
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {load(tag);}

	public void updateEssential(EnumSlot slot)
	{
		if (slot.hasEssential())
		{
			byte answer = isEssential(slot);
			boolean foundFirst = (answer & 1) > 0;
			boolean foundSecond = (answer & 2) > 0;
			this.isEssentialMissing[slot.ordinal() * 2] = !foundFirst;
			this.isEssentialMissing[slot.ordinal() * 2 + 1] = !foundSecond;
		} else
		{
			this.isEssentialMissing[slot.ordinal() * 2] = false;
			this.isEssentialMissing[slot.ordinal() * 2 + 1] = false;
		}
	}

	private byte isEssential(EnumSlot slot)
	{
		byte r = 0;

		for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
		{
			int index = slot.ordinal() * LibConstants.WARE_PER_SLOT + indexSlot;
			ItemStack slotStack = slots.getStackInSlot(index);
			ItemStack playerStack = slotsPlayer.getStackInSlot(index);

			ItemStack stack = !slotStack.isEmpty() ? slotStack : (discardSlots[index] ? ItemStack.EMPTY : playerStack);

			if (!stack.isEmpty())
			{
				ICyberware ware = CyberwareAPI.getCyberware(stack);
				if (ware.isEssential(stack))
				{
					if (slot.isSided() && ware instanceof ISidedLimb)
					{
						if (((ISidedLimb) ware).getSide(stack) == EnumSide.LEFT && (r & 1) == 0)
						{
							r += 1;
						} else if ((r & 2) == 0)
						{
							r += 2;
						}
					} else
					{
						return 3;
					}
				}
			}
		}
		return r;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TileEntitySurgery blockEntity)
	{
		assert level != null;

		if (blockEntity.inProgress && blockEntity.progressTicks < 80)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(blockEntity.targetEntity);
			if (blockEntity.targetEntity != null
				&& !blockEntity.targetEntity.isDeadOrDying()
				&& cyberwareUserData != null)
			{
				if (blockEntity.progressTicks > 20 && blockEntity.progressTicks < 60)
				{
					blockEntity.targetEntity.setPos(
						blockEntity.targetEntity
							.position()
							.add(new Vec3(pos.getX() + .5F, 0, pos.getZ() + .5F))
					);
				}

				if (blockEntity.progressTicks >= 20 && blockEntity.progressTicks <= 60 && blockEntity.progressTicks % 5 == 0)
				{
					blockEntity.targetEntity.hurt(EssentialsMissingHandler.surgery, 2F);
				}

				if (blockEntity.progressTicks == 60)
				{
					blockEntity.processUpdate(cyberwareUserData);
				}

				blockEntity.progressTicks++;

				if (Minecraft.getInstance().player == blockEntity.targetEntity)
				{
					workingOnPlayer = true;
					playerProgressTicks = blockEntity.progressTicks;
				}
			} else
			{
				blockEntity.inProgress = false;
				blockEntity.progressTicks = 0;

				if (Minecraft.getInstance().player == blockEntity.targetEntity)
				{
					workingOnPlayer = false;
				}
				blockEntity.targetEntity = null;

				BlockState state2 = level.getBlockState(pos.relative(Direction.DOWN));
				if (state2.getBlock() instanceof BlockSurgeryChamber)
				{
					((BlockSurgeryChamber) state2.getBlock()).toggleDoor(true, state2,
						pos.relative(Direction.DOWN), level
					);
				}
			}
		} else if (blockEntity.inProgress)
		{
			if (Minecraft.getInstance().player == blockEntity.targetEntity)
			{
				workingOnPlayer = false;
			}
			blockEntity.inProgress = false;
			blockEntity.progressTicks = 0;
			blockEntity.targetEntity = null;
			blockEntity.cooldownTicks = 60;

			BlockState state2 = level.getBlockState(pos.relative(Direction.DOWN));
			if (state2.getBlock() instanceof BlockSurgeryChamber)
			{
				((BlockSurgeryChamber) state2.getBlock()).toggleDoor(true, state2,
					pos.relative(Direction.DOWN), level
				);
			}
		}

		if (blockEntity.cooldownTicks > 0)
		{
			blockEntity.cooldownTicks--;
		}
	}

	public void processUpdate(ICyberwareUserData cyberwareUserData)
	{
		assert level != null;
		updatePlayerSlots(targetEntity, cyberwareUserData);

		for (int indexCyberSlot = 0; indexCyberSlot < EnumSlot.values().length; indexCyberSlot++)
		{
			EnumSlot slot = EnumSlot.values()[indexCyberSlot];
			NonNullList<ItemStack> nnlToInstall = NonNullList.create();
			for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
			{
				nnlToInstall.add(ItemStack.EMPTY);
			}

			int indexToInstall = 0;
			for (int indexCyberware = indexCyberSlot * LibConstants.WARE_PER_SLOT; indexCyberware < (indexCyberSlot + 1) * LibConstants.WARE_PER_SLOT; indexCyberware++)
			{
				ItemStack itemStackSurgery = slots.getStackInSlot(indexCyberware);

				ItemStack itemStackPlayer = slotsPlayer.getStackInSlot(indexCyberware).copy();
				if (!itemStackSurgery.isEmpty())
				{
					ItemStack itemStackToSet = itemStackSurgery.copy();
					if (CyberwareAPI.areCyberwareStacksEqual(itemStackToSet, itemStackPlayer))
					{
						int maxSize = CyberwareAPI.getCyberware(itemStackToSet).installedStackSize(itemStackToSet);

						if (itemStackToSet.getCount() < maxSize)
						{
							int numToShift = Math.min(maxSize - itemStackToSet.getCount(), itemStackPlayer.getCount());
							itemStackPlayer.shrink(numToShift);
							itemStackToSet.grow(numToShift);
						}
					}

					if (!itemStackPlayer.isEmpty())
					{
						CyberwareAPI.sanitize(itemStackPlayer);
						addItemStack(targetEntity, itemStackPlayer);
					}

					nnlToInstall.set(indexToInstall, itemStackToSet);
					indexToInstall++;
				} else if (!itemStackPlayer.isEmpty())
				{
					if (discardSlots[indexCyberware])
					{
						CyberwareAPI.sanitize(itemStackPlayer);
						addItemStack(targetEntity, itemStackPlayer);
					} else
					{
						nnlToInstall.set(indexToInstall, slotsPlayer.getStackInSlot(indexCyberware).copy());
						indexToInstall++;
					}
				}
			}
			if (!level.isClientSide())
			{
				cyberwareUserData.setInstalledCyberware(targetEntity, slot, nnlToInstall);
			}
			cyberwareUserData.setHasEssential(slot, !isEssentialMissing[indexCyberSlot * 2],
				!isEssentialMissing[indexCyberSlot * 2 + 1]
			);
		}

		cyberwareUserData.setTolerance(targetEntity, essence);
		cyberwareUserData.updateCapacity();
		cyberwareUserData.setImmune();

		if (!level.isClientSide())
		{
			CyberwareAPI.updateData(targetEntity);
		}
		slots = new ItemStackHandler(120);

		CyberwareSurgeryEvent.Post postSurgeryEvent = new CyberwareSurgeryEvent.Post(targetEntity);
		MinecraftForge.EVENT_BUS.post(postSurgeryEvent);
	}

	private void addItemStack(LivingEntity entityLivingBase, ItemStack stack)
	{
		boolean flag = true;
		assert level != null;

		if (entityLivingBase instanceof Player entityPlayer)
		{
			flag = !entityPlayer.getInventory().add(stack);
		}

		if (flag && !level.isClientSide())
		{
			ItemEntity item = new ItemEntity(level, worldPosition.getX() + .5F, worldPosition.getY() - 2F,
				worldPosition.getZ() + .5F, stack
			);
			level.addFreshEntity(item);
		}
	}

	public boolean canOpen()
	{
		return !inProgress && cooldownTicks <= 0;
	}

	public void notifyChange()
	{
		assert level != null;
		boolean opened =
			level.getBlockState(worldPosition.relative(Direction.DOWN)).getValue(BlockSurgeryChamber.OPEN);

		if (!opened)
		{
			BlockPos p = worldPosition;
			List<LivingEntity> entityLivingBases = level.getEntitiesOfClass(
				LivingEntity.class,
				new AABB(p.getX(), p.getY() - 2F, p.getZ(),
					p.getX() + 1F, p.getY(), p.getZ() + 1F
				)
			);
			if (entityLivingBases.size() == 1)
			{
				LivingEntity entityLivingBase = entityLivingBases.get(0);
				CyberwareSurgeryEvent.Pre preSurgeryEvent = new CyberwareSurgeryEvent.Pre(entityLivingBase,
					slotsPlayer, slots
				);

				if (!MinecraftForge.EVENT_BUS.post(preSurgeryEvent))
				{
					this.inProgress = true;
					this.progressTicks = 0;
					this.targetEntity = entityLivingBase;
				} else
				{
					BlockState state = level.getBlockState(worldPosition.relative(Direction.DOWN));
					if (state.getBlock() instanceof BlockSurgeryChamber)
					{
						((BlockSurgeryChamber) state.getBlock()).toggleDoor(true, state,
							worldPosition.relative(Direction.DOWN),
							level
						);
					}
				}
			}
		}
	}

	public boolean inProgress = false;
	public LivingEntity targetEntity = null;
	public int progressTicks = 0;
	public static boolean workingOnPlayer = false;
	public static int playerProgressTicks = 0;

	public void updateEssence()
	{
		this.essence = this.maxEssence;
		boolean hasConsume = false;
		boolean hasProduce = false;

		for (EnumSlot slot : EnumSlot.values())
		{
			for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
			{
				int index = slot.ordinal() * LibConstants.WARE_PER_SLOT + indexSlot;
				ItemStack slotStack = slots.getStackInSlot(index);
				ItemStack playerStack = slotsPlayer.getStackInSlot(index);

				ItemStack stack = !slotStack.isEmpty() ? slotStack : (discardSlots[index] ? ItemStack.EMPTY :
					playerStack);

				if (!stack.isEmpty())
				{
					ItemStack ret = stack.copy();
					if (!slotStack.isEmpty()
						&& !ret.isEmpty()
						&& !playerStack.isEmpty()
						&& CyberwareAPI.areCyberwareStacksEqual(playerStack, ret))
					{
						int maxSize = CyberwareAPI.getCyberware(ret).installedStackSize(ret);

						if (ret.getCount() < maxSize)
						{
							int numToShift = Math.min(maxSize - ret.getCount(), playerStack.getCount());
							ret.grow(numToShift);
						}
					}
					ICyberware ware = CyberwareAPI.getCyberware(ret);

					this.essence -= ware.getEssenceCost(ret);

					if (ware instanceof ItemCyberware && ((ItemCyberware) ware).getPowerConsumption(ret) > 0)
					{
						hasConsume = true;
					}
					if (ware instanceof ItemCyberware && (((ItemCyberware) ware).getPowerProduction(ret) > 0 || ware == CyberwareContent.creativeBattery))
					{
						hasProduce = true;
					}
				}
			}
		}

		this.missingPower = hasConsume && !hasProduce;
	}
}