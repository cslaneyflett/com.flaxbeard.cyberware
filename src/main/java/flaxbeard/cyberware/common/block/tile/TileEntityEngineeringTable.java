package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.IBlueprint;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.item.ItemBlueprint;
import flaxbeard.cyberware.common.misc.SpecificWrapper;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.ScannerSmashPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ITickable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileEntityEngineeringTable extends BlockEntity implements ITickable
{
	public static class TileEntityEngineeringDummy extends BlockEntity
	{

		//		@Override
		//		public boolean hasCapability(Capability<?> capability, Direction facing)
		//		{
		//			BlockEntity above = level.getBlockEntity(worldPosition.offset(0, 1, 0));
		//			if (above != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
		//				return above.hasCapability(capability, facing);
		//			}
		//			return super.hasCapability(capability, facing);
		//		}

		@Override
		public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
														  final @Nullable Direction facing)
		{
			assert level != null;
			BlockEntity above = level.getBlockEntity(worldPosition.relative(Direction.UP));

			if (above != null && capability == ForgeCapabilities.ITEM_HANDLER)
			{
				return above.getCapability(capability, facing);
			}

			return super.getCapability(capability, facing);
		}
	}

	public class ItemStackHandlerEngineering extends ItemStackHandler
	{
		public boolean overrideExtract = false;
		private final TileEntityEngineeringTable table;

		public ItemStackHandlerEngineering(TileEntityEngineeringTable table, int i)
		{
			super(i);
			this.table = table;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
			boolean check = false;
			if (slot == 0 && this.getStackInSlot(0).isEmpty() && !level.isClientSide())
			{
				check = true;
			}

			super.setStackInSlot(slot, stack);

			if (check)
			{
				table.level.setBlockState(worldPosition, table.level.getBlockState(worldPosition), 2);
				table.level.notifyBlockUpdate(worldPosition, table.level.getBlockState(worldPosition),
					table.level.getBlockState(worldPosition), 2
				);
			}

			if (slot >= 2 && slot <= 8)
			{
				table.updateRecipe();
			}
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if (!isItemValidForSlot(slot, stack)) return stack;

			boolean check = false;
			if (slot == 0 && this.getStackInSlot(0).isEmpty() && !simulate && !level.isClientSide())
			{
				check = true;
			}

			ItemStack result = super.insertItem(slot, stack, simulate);

			if (check)
			{
				table.level.setBlockState(worldPosition, table.level.getBlockState(worldPosition), 2);
				table.level.notifyBlockUpdate(worldPosition, table.level.getBlockState(worldPosition),
					table.level.getBlockState(worldPosition), 2
				);
			}

			if (slot >= 2 && slot <= 8 && !simulate)
			{
				table.updateRecipe();
			}
			return result;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if (!canRemoveItem(slot)) return ItemStack.EMPTY;


			ItemStack result = super.extractItem(slot, amount, simulate);
			if (slot == 9 && !result.isEmpty() && !simulate)
			{

				table.subtractResources();
			}
			if (slot >= 2 && slot <= 7 && !simulate)
			{
				table.updateRecipe();
			}

			return result;
		}

		public boolean canRemoveItem(int slot)
		{
			if (overrideExtract) return true;
			if (!getStackInSlot(8).isEmpty() && (slot >= 2 && slot <= 7)) return false;
			if (slot == 1 || slot == 8) return false;
			return true;
		}

		public boolean isItemValidForSlot(int slot, ItemStack stack)
		{
			switch (slot)
			{
				case 0:
					return CyberwareAPI.canDeconstruct(stack);
				case 1:
					return stack.is(Items.PAPER);
				case 8:
					return !stack.isEmpty() && stack.getItem() instanceof IBlueprint;
				case 9:
					return false;
				default:
					return overrideExtract || !CyberwareAPI.canDeconstruct(stack);
			}
		}
	}

	public class GuiWrapper implements IItemHandlerModifiable
	{
		private ItemStackHandlerEngineering slots;

		public GuiWrapper(ItemStackHandlerEngineering slots)
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
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			slots.overrideExtract = true;
			ItemStack res = slots.insertItem(slot, stack, simulate);
			slots.overrideExtract = false;
			return res;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			slots.overrideExtract = true;
			ItemStack ret = slots.extractItem(slot, amount, simulate);
			slots.overrideExtract = false;
			return ret;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
			slots.overrideExtract = true;
			slots.setStackInSlot(slot, stack);
			slots.overrideExtract = false;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack)
		{
			return true;
		}
	}

	public ItemStackHandlerEngineering slots = new ItemStackHandlerEngineering(this, 10);
	private final LazyOptional<RangedWrapper> slotsTopSides = LazyOptional.of(() -> new RangedWrapper(slots, 0, 7));
	private final LazyOptional<SpecificWrapper> slotsBottom = LazyOptional.of(() -> new SpecificWrapper(slots, 2, 3, 4, 5, 6, 7, 9));
	public final GuiWrapper guiSlots = new GuiWrapper(slots);
	public String customName = null;
	public float clickedTime = -100F;
	private int time;
	public HashMap<String, BlockPos> lastPlayerArchive = new HashMap<>();

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
			if (facing == Direction.DOWN)
			{
				return slotsBottom.cast();
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

		this.time = tagCompound.getInt("time");

		lastPlayerArchive = new HashMap<>();
		ListTag list = tagCompound.getList("playerArchive", Tag.TAG_COMPOUND);
		for (int indexArchive = 0; indexArchive < list.size(); indexArchive++)
		{
			CompoundTag tagCompoundAt = list.getCompound(indexArchive);
			String name = tagCompoundAt.getString("name");
			int x = tagCompoundAt.getInt("x");
			int y = tagCompoundAt.getInt("y");
			int z = tagCompoundAt.getInt("z");
			BlockPos pos = new BlockPos(x, y, z);
			lastPlayerArchive.put(name, pos);
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

		tagCompound.putInt("time", time);
		ListTag list = new ListTag();
		for (String name : this.lastPlayerArchive.keySet())
		{
			CompoundTag entry = new CompoundTag();
			entry.putString("name", name);
			BlockPos pos = lastPlayerArchive.get(name);
			entry.putInt("x", pos.getX());
			entry.putInt("y", pos.getY());
			entry.putInt("z", pos.getZ());
			list.add(entry);
		}
		tagCompound.put("playerArchive", list);
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
		return new SPacketUpdateTileEntity(worldPosition, 0, data);
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
		return this.hasCustomName() ? customName : "cyberware.container.engineering";
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

	public void updateRecipe()
	{
		ItemStack blueprintStack = slots.getStackInSlot(8);
		if (!blueprintStack.isEmpty() && blueprintStack.getItem() instanceof IBlueprint)
		{
			IBlueprint blueprint = (IBlueprint) blueprintStack.getItem();
			NonNullList<ItemStack> toCheck = NonNullList.create();
			for (int indexSlot = 0; indexSlot < 6; indexSlot++)
			{
				toCheck.add(slots.getStackInSlot(indexSlot + 2).copy());
			}
			ItemStack result = blueprint.getResult(blueprintStack, toCheck).copy();
			if (!result.isEmpty())
			{
				result.setCount(1);
			}
			this.slots.setStackInSlot(9, result);
		} else
		{
			this.slots.setStackInSlot(9, ItemStack.EMPTY);
		}
	}

	public void subtractResources()
	{
		ItemStack blueprintStack = slots.getStackInSlot(8);
		if (!blueprintStack.isEmpty() && blueprintStack.getItem() instanceof IBlueprint)
		{
			IBlueprint blueprint = (IBlueprint) blueprintStack.getItem();
			NonNullList<ItemStack> toCheck = NonNullList.create();
			for (int indexSlot = 0; indexSlot < 6; indexSlot++)
			{
				toCheck.add(slots.getStackInSlot(indexSlot + 2).copy());
			}
			NonNullList<ItemStack> result = blueprint.consumeItems(blueprintStack, toCheck);
			for (int indexSlot = 0; indexSlot < 6; indexSlot++)
			{
				slots.setStackInSlot(indexSlot + 2, result.get(indexSlot));
			}
			this.updateRecipe();
		} else
		{
			throw new IllegalStateException("Tried to subtract resources when no blueprint was available!");
		}
	}

	// Runs on the server 
	public void smash(boolean pkt)
	{
		ItemStack toDestroy = slots.getStackInSlot(0);

		if (CyberwareAPI.canDeconstruct(toDestroy) && toDestroy.getCount() > 0)
		{
			ItemStack paperSlot = slots.getStackInSlot(1);
			boolean doBlueprint =
				!paperSlot.isEmpty() && paperSlot.getCount() > 0 && paperSlot.getItem() == Items.PAPER;

			NonNullList<ItemStack> components = CyberwareAPI.getComponents(toDestroy);

			List<ItemStack> random = new ArrayList<>();
			for (ItemStack component : components)
			{
				if (!component.isEmpty())
				{
					for (int indexComponent = 0; indexComponent < component.getCount(); indexComponent++)
					{
						ItemStack copy = component.copy();
						copy.setCount(1);
						random.add(copy);
					}
				}
			}

			int numToRemove = 1;
			switch (level.getDifficulty())
			{
				case EASY:
					numToRemove = 1;
					break;
				case HARD:
					numToRemove = 2;
					break;
				case NORMAL:
					numToRemove = 2;
					break;
				case PEACEFUL:
					numToRemove = 1;
					break;
				default:
					break;
			}

			if (slots.getStackInSlot(0).isItemStackDamageable()) // Damaged items yield less
			{
				float percent =
					(slots.getStackInSlot(0).getItemDamage() * 1F / slots.getStackInSlot(0).getMaxDamage());
				int addl = (int) (random.size() * percent);
				addl = Math.max(0, addl - 1);
				numToRemove += addl;
			}

			numToRemove = Math.min(numToRemove, random.size() - 1);
			for (int index = 0; index < numToRemove; index++)
			{
				random.remove(level.getRandom().nextInt(random.size()));
			}

			ItemStackHandler handler = new ItemStackHandler(6);
			for (int indexSlot = 0; indexSlot < 6; indexSlot++)
			{
				handler.setStackInSlot(indexSlot, slots.getStackInSlot(indexSlot + 2).copy());
			}
			boolean canInsert = true;

			// Check if drops will fit
			for (ItemStack drop : components)
			{
				ItemStack left = drop.copy();
				boolean wasAble = false;
				for (int slot = 0; slot < 6; slot++)
				{
					left = handler.insertItem(slot, left, false);
					if (left.isEmpty())
					{
						wasAble = true;
						break;
					}
				}

				if (!wasAble)
				{
					canInsert = false;
					break;
				}
			}

			// Check if blueprint will fit
			if (doBlueprint)
			{
				ItemStack left = ItemBlueprint.getBlueprintForItem(toDestroy);
				boolean wasAble = false;
				for (int slot = 0; slot < 6; slot++)
				{
					left = handler.insertItem(slot, left, false);
					if (left.isEmpty())
					{
						wasAble = true;
						break;
					}
				}

				if (!wasAble)
				{
					canInsert = false;
				}
			}


			if (canInsert)
			{
				if (pkt)
				{
					CyberwarePacketHandler.INSTANCE.sendToAllAround(
						new ScannerSmashPacket(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()),
						new TargetPoint(level.provider.getDimension(), worldPosition.getX(), worldPosition.getY(),
							worldPosition.getZ(), 25
						)
					);
				}

				if (!level.isClientSide())
				{
					if (doBlueprint && getLevel().getRandom().nextFloat() < (CyberwareConfig.INSTANCE.ENGINEERING_CHANCE.get() / 100F))
					{
						ItemStack blue = ItemBlueprint.getBlueprintForItem(toDestroy);
						random.add(blue);

						ItemStack current = slots.getStackInSlot(1);
						current.shrink(1);
						if (current.getCount() <= 0)
						{
							current = ItemStack.EMPTY;
						}
						slots.setStackInSlot(1, current);
					}


					for (ItemStack drop : random)
					{
						ItemStack dropLeft = drop.copy();
						for (int slot = 2; slot < 8; slot++)
						{
							if (!slots.getStackInSlot(slot).isEmpty())
							{
								dropLeft = slots.insertItem(slot, dropLeft, false);
								if (dropLeft.isEmpty())
								{
									break;
								}
							}
						}

						for (int slot = 2; slot < 8; slot++)
						{
							dropLeft = slots.insertItem(slot, dropLeft, false);
							if (dropLeft.isEmpty())
							{
								break;
							}
						}
					}

					ItemStack current = slots.getStackInSlot(0);
					current.shrink(1);
					if (current.getCount() <= 0 || current.isEmpty())
					{
						world.notifyBlockUpdate(pos, world.getBlockState(worldPosition),
							world.getBlockState(worldPosition), 2
						);

						current = ItemStack.EMPTY;
					}
					slots.setStackInSlot(0, current);
					updateRecipe();
				} else
				{
					smashSounds();
				}
			}
		}
	}

	@Override
	public void update()
	{
		if (level.isBlockPowered(worldPosition) || level.isBlockPowered(worldPosition.offset(0, -1, 0)))
		{
			if (time == 0)
			{
				this.smash(false);
			}
			time = (time + 1) % 25;
		} else
		{
			time = 0;
		}
	}

	public void smashSounds()
	{
		var player = Minecraft.getInstance().player;
		assert player != null;
		clickedTime = player.tickCount + Minecraft.getInstance().getPartialTick();
		assert level != null;
		level.playSound(player, worldPosition, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1F, 1F);
		level.playSound(player, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1F, .5F);
		for (int index = 0; index < 10; index++)
		{
			// TODO: particles
			//			world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
			//			                    x + .5F, y, z + .5F,
			//			                    .25F * (world.rand.nextFloat() - .5F), .1F, .25F * (world.rand.nextFloat()
			//			                    - .5F),
			//			                    Item.getIdFromItem(slots.getStackInSlot(0).getItem()));
		}
	}
}
