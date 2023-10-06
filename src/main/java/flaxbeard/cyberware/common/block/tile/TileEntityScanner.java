package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.item.ItemBlueprint;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityScanner extends BlockEntity implements ITickable
{
	public class ItemStackHandlerScanner extends ItemStackHandler
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

			switch (slot)
			{
				case 0:
					return CyberwareAPI.canDeconstruct(stack);

				case 1:
					int[] idsOreDictionary = OreDictionary.getOreIDs(stack);
					int idPaper = OreDictionary.getOreID("paper");
					for (int idOreDictionary : idsOreDictionary)
					{
						if (idOreDictionary == idPaper)
						{
							return true;
						}
					}
					return false;

				case 2:
					return false;
			}
			return true;
		}
	}

	public class GuiWrapper implements IItemHandlerModifiable
	{
		private ItemStackHandlerScanner slots;

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
	public void readFromNBT(CompoundTag tagCompound)
	{
		super.readFromNBT(tagCompound);

		slots.deserializeNBT(tagCompound.getCompound("inv"));

		if (tagCompound.contains("CustomName", 8))
		{
			customName = tagCompound.getString("CustomName");
		}

		ticks = tagCompound.getInt("ticks");
	}

	@Nonnull
	@Override
	public CompoundTag writeToNBT(CompoundTag tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);

		tagCompound.put("inv", slots.serializeNBT());

		if (hasCustomName())
		{
			tagCompound.putString("CustomName", customName);
		}

		tagCompound.putInt("ticks", ticks);

		return tagCompound;
	}

	@Override
	public void onDataPacket(Connection networkManager, @Nonnull SPacketUpdateTileEntity packetUpdateTileEntity)
	{
		CompoundTag tagCompound = packetUpdateTileEntity.getNbtCompound();
		readFromNBT(tagCompound);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		CompoundTag tagCompound = new CompoundTag();
		writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(pos, 0, tagCompound);
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag()
	{
		return writeToNBT(new CompoundTag());
	}

	public boolean isUsableByPlayer(Player entityPlayer)
	{
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

	@Override
	public Component getDisplayName()
	{
		return this.hasCustomName() ? Component.literal(this.getName()) : Component.translatable(this.getName());
	}

	@Override
	public void update()
	{
		ItemStack toDestroy = slots.getStackInSlot(0);
		if (CyberwareAPI.canDeconstruct(toDestroy)
			&& toDestroy.getCount() > 0
			&& slots.getStackInSlot(2).isEmpty())
		{
			ticks++;
			assert level != null;

			if (ticksMove > ticks
				|| (ticks - ticksMove > Math.max(Math.abs(lastX - x) * 3, Math.abs(lastZ - z) * 3) + 10))
			{
				ticksMove = ticks;
				lastX = x;
				lastZ = z;
				while (x == lastX)
				{
					x = level.getRandom().nextInt(11);
				}
				while (z == lastZ)
				{
					z = world.getRandom().nextInt(11);
				}
			}
			if (ticks > CyberwareConfig.INSTANCE.SCANNER_TIME.get())
			{
				ticks = 0;
				ticksMove = 0;

				if (!level.isClientSide()
					&& !slots.getStackInSlot(1).isEmpty())
				{
					float chance = CyberwareConfig.INSTANCE.SCANNER_CHANCE.get()
						+ CyberwareConfig.INSTANCE.SCANNER_CHANCE_ADDL.get() * (slots.getStackInSlot(0).getCount() - 1);
					if (slots.getStackInSlot(0).isItemStackDamageable())
					{
						chance =
							50F * (1F - (slots.getStackInSlot(0).LEVEL_getItemDamage() / (float) slots.getStackInSlot(0).getMaxDamage()));
					}
					chance = Math.min(chance, 50F);

					if (level.getRandom().nextFloat() < (chance / 100F))
					{
						ItemStack stackBlueprint = ItemBlueprint.getBlueprintForItem(toDestroy);
						slots.setStackInSlot(2, stackBlueprint);
						ItemStack current = slots.getStackInSlot(1);
						current.shrink(1);
						if (current.getCount() <= 0)
						{
							current = ItemStack.EMPTY;
						}
						slots.setStackInSlot(1, current);
						level.notifyBlockUpdate(worldPosition, level.getBlockState(worldPosition),
							level.getBlockState(worldPosition), 2
						);
					}
				}
			}
			markDirty();
		} else
		{
			x = lastX = z = lastZ = 0;
			if (ticks != 0)
			{
				ticks = 0;
				markDirty();
			}
		}
	}

	public float getProgress()
	{
		return ticks * 1F / CyberwareConfig.INSTANCE.SCANNER_TIME.get();
	}
}
