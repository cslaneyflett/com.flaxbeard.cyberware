package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.item.ItemBlueprint;
import flaxbeard.cyberware.common.registry.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityScanner extends BlockEntity
{
	public TileEntityScanner(BlockPos pPos, BlockState pBlockState)
	{
		super(BlockEntities.SCANNER.get(), pPos, pBlockState);
	}

	public static class ItemStackHandlerScanner extends ItemStackHandler
	{
		public ItemStackHandlerScanner(int size)
		{
			super(size);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
		{
			if (!isItemValidForSlot(slot, stack)) return stack;

			return super.insertItem(slot, stack, simulate);
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return super.extractItem(slot, amount, simulate);
		}

		public boolean isItemValidForSlot(int slot, ItemStack stack)
		{
			validateSlotIndex(slot);

			return switch (slot)
			{
				case 0 -> CyberwareAPI.canDeconstruct(stack);
				case 1 -> stack.is(Items.PAPER);
				case 2 -> false;
				default -> true;
			};
		}
	}

	public static class GuiWrapper implements IItemHandlerModifiable
	{
		private final ItemStackHandlerScanner slots;

		public GuiWrapper(ItemStackHandlerScanner slots)
		{
			this.slots = slots;
		}

		@Override
		public int getSlots()
		{
			return slots.getSlots();
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return slots.getStackInSlot(slot);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
		{
			return slots.insertItem(slot, stack, simulate);
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return slots.extractItem(slot, amount, simulate);
		}

		@Override
		public void setStackInSlot(int slot, @Nonnull ItemStack stack)
		{
			slots.setStackInSlot(slot, stack);
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return true;
		}
	}

	public ItemStackHandlerScanner slots = new ItemStackHandlerScanner(3);
	private final LazyOptional<RangedWrapper> slotsTopSides = LazyOptional.of(() -> new RangedWrapper(slots, 0, 2));
	private final LazyOptional<RangedWrapper> slotsBottom = LazyOptional.of(() -> new RangedWrapper(slots, 2, 3));
	private final LazyOptional<RangedWrapper> slotsBottom2 = LazyOptional.of(() -> new RangedWrapper(slots, 0, 1));
	public final GuiWrapper guiSlots = new GuiWrapper(slots);
	public String customName = null;
	public int ticks = 0;
	public int ticksMove = 0;
	public int lastX = 0;
	public int x = 0;
	public int lastZ = 0;
	public int z = 0;

	//	@Override
	//	public boolean hasCapability(@Nonnull Capability<?> capability, Direction facing)
	//	{
	//		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
	//			return true;
	//		}
	//		return super.hasCapability(capability, facing);
	//	}

	public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
													  final @Nullable Direction facing)
	{
		if (capability == ForgeCapabilities.ITEM_HANDLER)
		{
			if (facing == Direction.DOWN)
			{
				if (!slots.getStackInSlot(2).isEmpty() &&
					!slots.getStackInSlot(0).isEmpty()
				)
				{
					return slotsBottom2.cast();
				} else
				{
					return slotsBottom.cast();
				}
			} else
			{
				return slotsTopSides.cast();
			}
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public void load(@Nonnull CompoundTag tagCompound)
	{
		super.load(tagCompound);

		slots.deserializeNBT(tagCompound.getCompound("inv"));

		if (tagCompound.contains("CustomName", 8))
		{
			customName = tagCompound.getString("CustomName");
		}

		ticks = tagCompound.getInt("ticks");
	}

	@Override
	public void saveAdditional(@Nonnull CompoundTag tagCompound)
	{
		super.saveAdditional(tagCompound);

		tagCompound.put("inv", slots.serializeNBT());

		if (hasCustomName())
		{
			tagCompound.putString("CustomName", customName);
		}

		tagCompound.putInt("ticks", ticks);
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

	public boolean isUsableByPlayer(Player entityPlayer)
	{
		assert level != null;
		return level.getBlockEntity(worldPosition) == this
			&& entityPlayer.position().distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
			worldPosition.getZ() + 0.5D
		) <= 64.0D;
	}

	public String getName()
	{
		return hasCustomName() ? customName : "cyberware.container.scanner";
	}

	public boolean hasCustomName()
	{
		return customName != null && !customName.isEmpty();
	}

	public void setCustomInventoryName(String name)
	{
		customName = name;
	}

	//	@Override
	public Component getDisplayName()
	{
		return this.hasCustomName() ? Component.literal(this.getName()) : Component.translatable(this.getName());
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be)
	{
		assert level != null;
		var blockEntity = (TileEntityScanner) be;

		ItemStack toDestroy = blockEntity.slots.getStackInSlot(0);

		if (CyberwareAPI.canDeconstruct(toDestroy)
			&& toDestroy.getCount() > 0
			&& blockEntity.slots.getStackInSlot(2).isEmpty())
		{
			blockEntity.ticks++;
			assert level != null;

			if (blockEntity.ticksMove > blockEntity.ticks
				|| (blockEntity.ticks - blockEntity.ticksMove > Math.max(Math.abs(blockEntity.lastX - blockEntity.x) * 3, Math.abs(blockEntity.lastZ - blockEntity.z) * 3) + 10))
			{
				blockEntity.ticksMove = blockEntity.ticks;
				blockEntity.lastX = blockEntity.x;
				blockEntity.lastZ = blockEntity.z;
				while (blockEntity.x == blockEntity.lastX)
				{
					blockEntity.x = level.getRandom().nextInt(11);
				}
				while (blockEntity.z == blockEntity.lastZ)
				{
					blockEntity.z = level.getRandom().nextInt(11);
				}
			}
			if (blockEntity.ticks > CyberwareConfig.INSTANCE.SCANNER_TIME.get())
			{
				blockEntity.ticks = 0;
				blockEntity.ticksMove = 0;

				if (!level.isClientSide()
					&& !blockEntity.slots.getStackInSlot(1).isEmpty())
				{
					double chance = CyberwareConfig.INSTANCE.SCANNER_CHANCE.get()
						+ CyberwareConfig.INSTANCE.SCANNER_CHANCE_ADDL.get() * (blockEntity.slots.getStackInSlot(0).getCount() - 1);
					if (blockEntity.slots.getStackInSlot(0).isDamageableItem())
					{
						chance =
							50F * (1F - (blockEntity.slots.getStackInSlot(0).getDamageValue() / (float) blockEntity.slots.getStackInSlot(0).getMaxDamage()));
					}
					chance = Math.min(chance, 50F);

					if (level.getRandom().nextFloat() < (chance / 100F))
					{
						ItemStack stackBlueprint = ItemBlueprint.getBlueprintForItem(toDestroy);
						blockEntity.slots.setStackInSlot(2, stackBlueprint);
						ItemStack current = blockEntity.slots.getStackInSlot(1);
						current.shrink(1);
						if (current.getCount() <= 0)
						{
							current = ItemStack.EMPTY;
						}
						blockEntity.slots.setStackInSlot(1, current);
						level.sendBlockUpdated(pos, level.getBlockState(pos),
							state, 2
						);
					}
				}
			}
			blockEntity.setChanged();
		} else
		{
			blockEntity.x = blockEntity.lastX = blockEntity.z = blockEntity.lastZ = 0;
			if (blockEntity.ticks != 0)
			{
				blockEntity.ticks = 0;
				blockEntity.setChanged();
			}
		}
	}

	public float getProgress()
	{
		return ticks * 1F / CyberwareConfig.INSTANCE.SCANNER_TIME.get();
	}
}
