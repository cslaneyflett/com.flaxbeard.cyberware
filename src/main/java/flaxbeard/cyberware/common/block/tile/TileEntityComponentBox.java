package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.common.CyberwareContent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityComponentBox extends BlockEntity
{
	public static class ItemStackHandlerComponent extends ItemStackHandler
	{
		public ItemStackHandlerComponent(int size)
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

		public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack)
		{
			if (!stack.isEmpty() && stack.getItem() == CyberwareContent.component) return true;

			return stack.isEmpty();
		}
	}

	public LazyOptional<ItemStackHandler> slots = LazyOptional.of(() -> new ItemStackHandlerComponent(18));
	public String customName = null;
	public boolean doDrop = true;

	//	@Override
	//	public boolean hasCapability(@Nonnull Capability<?> capability, Direction facing)
	//	{
	//		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
	//			return true;
	//		}
	//		return super.hasCapability(capability, facing);
	//	}

	@Override
	public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
													  final @Nullable Direction facing)
	{
		if (capability == ForgeCapabilities.ITEM_HANDLER)
		{
			return slots.cast();
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public void readFromNBT(@Nonnull CompoundTag tagCompound)
	{
		super.readFromNBT(tagCompound);

		slots.deserializeNBT(tagCompound.getCompound("inv"));

		if (tagCompound.contains("CustomName"))
		{
			customName = tagCompound.getString("CustomName");
		}
	}

	@Nonnull
	@Override
	public CompoundTag writeToNBT(@Nonnull CompoundTag tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);

		tagCompound.put("inv", slots.serializeNBT());

		if (hasCustomName())
		{
			tagCompound.putString("CustomName", customName);
		}

		return tagCompound;
	}

	@Override
	public void onDataPacket(Connection networkManager, SPacketUpdateTileEntity packetUpdateTileEntity)
	{
		CompoundTag tagCompound = packetUpdateTileEntity.getNbtCompound();
		readFromNBT(tagCompound);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		CompoundTag tagCompound = new CompoundTag();
		writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(worldPosition, 0, tagCompound);
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag()
	{
		return writeToNBT(new CompoundTag());
	}

	public boolean isUsableByPlayer(Player entityPlayer)
	{
		assert level != null;
		return level.getBlockEntity(worldPosition) == this &&
			entityPlayer
				.position()
				.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	public String getName()
	{
		return hasCustomName() ? customName : "cyberware.container.component_box";
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
}
