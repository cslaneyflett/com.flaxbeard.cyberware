package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.api.item.IBlueprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityBlueprintArchive extends BlockEntity
{
	public TileEntityBlueprintArchive(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
	{
		super(pType, pPos, pBlockState);
	}

	public class ItemStackHandlerBlueprint extends ItemStackHandler
	{
		public ItemStackHandlerBlueprint(int i)
		{
			super(i);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if (!isItemValidForSlot(slot, stack)) return stack;

			return super.insertItem(slot, stack, simulate);
		}

		public boolean isItemValidForSlot(int slot, ItemStack stack)
		{
			if (!stack.isEmpty() && stack.getItem() instanceof IBlueprint) return true;
			return stack.is(Items.PAPER);
		}
	}

	public LazyOptional<ItemStackHandler> slots = LazyOptional.of(() -> new ItemStackHandlerBlueprint(18));
	public String customName = null;

	//	@Override
	//	public boolean hasCapability(Capability<?> capability, Direction facing)
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
	public void readFromNBT(CompoundTag tagCompound)
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
	public CompoundTag writeToNBT(CompoundTag tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);

		tagCompound.put("inv", this.slots.serializeNBT());

		if (this.hasCustomName())
		{
			tagCompound.putString("CustomName", customName);
		}

		return tagCompound;
	}

	@Override
	public void onDataPacket(Connection net, SPacketUpdateTileEntity pkt)
	{
		CompoundTag data = pkt.getNbtCompound();
		this.readFromNBT(data);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		CompoundTag data = new CompoundTag();
		this.writeToNBT(data);
		return new SPacketUpdateTileEntity(pos, 0, data);
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag()
	{
		return writeToNBT(new CompoundTag());
	}

	public boolean isUsableByPlayer(Player entityPlayer)
	{
		return this.level.getBlockEntity(worldPosition) == this
			&& entityPlayer.position().distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
			worldPosition.getZ() + 0.5D
		) <= 64.0D;
	}

	public String getName()
	{
		return this.hasCustomName() ? customName : "cyberware.container.blueprint_archive";
	}

	public boolean hasCustomName()
	{
		return this.customName != null && !this.customName.isEmpty();
	}

	public void setCustomInventoryName(String name)
	{
		this.customName = name;
	}

	@Override
	public Component getDisplayName()
	{
		return this.hasCustomName() ? Component.literal(this.getName()) : Component.translatable(this.getName());
	}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newState)
	{
		return (oldState.getBlock() != newState.getBlock());
	}
}
