package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.common.registry.CWBlockEntities;
import flaxbeard.cyberware.common.registry.CWTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityComponentBox extends BlockEntity
{
	public TileEntityComponentBox(BlockPos pPos, BlockState pBlockState)
	{
		super(CWBlockEntities.COMPONENT_BOX.get(), pPos, pBlockState);
	}

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
			// TODO: component tag?
			if (!stack.isEmpty() && stack.is(CWTags.COMPONENTS)) return true;

			return stack.isEmpty();
		}
	}

	public final ItemStackHandler slots = new ItemStackHandlerComponent(18);
	public LazyOptional<ItemStackHandler> lazySlots = LazyOptional.of(() -> slots);
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
			return lazySlots.cast();
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public void load(@Nonnull CompoundTag tagCompound)
	{
		super.load(tagCompound);

		slots.deserializeNBT(tagCompound.getCompound("inv"));

		if (tagCompound.contains("CustomName"))
		{
			customName = tagCompound.getString("CustomName");
		}
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

	//	@Override
	public Component getDisplayName()
	{
		return this.hasCustomName() ? Component.literal(this.getName()) : Component.translatable(this.getName());
	}
}
