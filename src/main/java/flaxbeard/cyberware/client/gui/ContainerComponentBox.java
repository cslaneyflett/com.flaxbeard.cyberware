package flaxbeard.cyberware.client.gui;

import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox;
import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox.ItemStackHandlerComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerComponentBox extends Container
{
	private ItemStackHandler slots;
	private int numRows;
	private final TileEntityComponentBox box;
	private final ItemStack item;

	public ContainerComponentBox(Container playerInventory, @Nonnull TileEntityComponentBox box)
	{
		super();

		this.box = box;
		item = ItemStack.EMPTY;
		slots = box.slots;
		numRows = slots.getSlots() / 9;

		addSlots(playerInventory);
	}

	public ContainerComponentBox(Container playerInventory, @Nonnull ItemStack itemStack)
	{
		super();

		box = null;
		item = itemStack;
		slots = new ItemStackHandlerComponent(18);

		CompoundTag tagCompound = itemStack.getTag();
		if (tagCompound != null
			&& itemStack.getTag().contains("contents"))
		{
			slots.deserializeNBT(tagCompound.getCompound("contents"));
		}

		numRows = slots.getSlots() / 9;

		addSlots(playerInventory);
	}

	private void addSlots(Container playerInventory)
	{
		int yOffset = (numRows - 4) * 18;

		// component box's inventory
		for (int indexRow = 0; indexRow < numRows; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new SlotItemHandler(slots, indexColumn + indexRow * 9, 8 + indexColumn * 18,
					18 + indexRow * 18
				));
			}
		}

		// player's inventory
		for (int indexRow = 0; indexRow < 3; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new Slot(playerInventory, indexColumn + indexRow * 9 + 9, 8 + indexColumn * 18,
					103 + indexRow * 18 + yOffset
				));
			}
		}

		// player's hotbar
		for (int indexColumn = 0; indexColumn < 9; indexColumn++)
		{
			addSlotToContainer(new Slot(playerInventory, indexColumn, 8 + indexColumn * 18, 161 + yOffset));
		}
	}

	@Override
	public boolean canInteractWith(@Nonnull Player entityPlayer)
	{
		return box == null ? entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem) == item
			: box.isUsableByPlayer(entityPlayer);
	}

	@Override
	public void onContainerClosed(@Nonnull Player entityPlayer)
	{
		super.onContainerClosed(entityPlayer);

		if (!item.isEmpty())
		{
			CompoundTag tagCompoundSlots = slots.serializeNBT();
			CompoundTag tagCompoundItem = item.getTag();
			if (tagCompoundItem == null)
			{
				tagCompoundItem = new CompoundTag();
				item.setTag(tagCompoundItem);
			}
			tagCompoundItem.put("contents", tagCompoundSlots);
		}
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(Player entityPlayer, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null
			&& slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < numRows * 9)
			{
				if (!mergeItemStack(itemstack1, numRows * 9, inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(itemstack1, 0, numRows * 9, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.getCount() == 0)
			{
				slot.putStack(ItemStack.EMPTY);
			} else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}
}