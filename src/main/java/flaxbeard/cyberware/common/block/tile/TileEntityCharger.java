package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.registry.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileEntityCharger extends BlockEntity implements IEnergyStorage
{
	private final PowerContainer container = new PowerContainer();
	//	private final LazyOptional<PowerContainer> lazyContainer = LazyOptional.of(() -> this.container);
	private final LazyOptional<TileEntityCharger> lazySelf = LazyOptional.of(() -> this);
	private boolean last = false;

	public TileEntityCharger(BlockPos pPos, BlockState pBlockState)
	{
		super(BlockEntities.CHARGER.get(), pPos, pBlockState);
	}

	@Override
	public void load(@Nonnull CompoundTag tagCompound)
	{
		super.load(tagCompound);

		container.deserializeNBT(tagCompound.getCompound("power"));
	}

	@Override
	public void saveAdditional(@Nonnull CompoundTag tagCompound)
	{
		super.saveAdditional(tagCompound);
		tagCompound.put("power", container.serializeNBT());
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

	@Override
	public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
													  final @Nullable Direction facing)
	{
		if (capability == ForgeCapabilities.ENERGY)
			return lazySelf.cast();

		return super.getCapability(capability, facing);
	}

	//	@Override
	//	public boolean hasCapability(Capability<?> capability, Direction facing)
	//	{
	//		if (capability == TESLA_CONSUMER || capability == TESLA_PRODUCER || capability == TESLA_HOLDER || capability == CapabilityEnergy.ENERGY) {
	//			return true;
	//		}
	//
	//		return super.hasCapability(capability, facing);
	//	}

	public static void tick(Level level, BlockPos pos, BlockState state, TileEntityCharger blockEntity)
	{
		assert level != null;
		List<LivingEntity> entitiesInRange = level.getEntitiesOfClass(
			LivingEntity.class,
			new AABB(pos.getX(), pos.getY(), pos.getZ(),
				pos.getX() + 1F, pos.getY() + 2.5F, pos.getZ() + 1F
			)
		);
		for (LivingEntity entityInRange : entitiesInRange)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityInRange);
			if (cyberwareUserData != null
				&& !cyberwareUserData.isAtCapacity(ItemStack.EMPTY, 20)
				&& (blockEntity.container.getStoredPower() >= CyberwareConfig.INSTANCE.TESLA_PER_POWER.get()))
			{
				if (!level.isClientSide())
				{
					blockEntity.container.takePower(CyberwareConfig.INSTANCE.TESLA_PER_POWER.get(), false);
				}
				cyberwareUserData.addPower(20, ItemStack.EMPTY);

				// TODO: particles

				//				if (entityInRange.tickCount % 5 == 0)
				//				{
				//					level.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
				//							.5F, worldPosition.getY() + 1F, worldPosition.getZ() + .5F, 0F, .05F, 0F, 255,
				//						150, 255
				//					);
				//					level.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
				//							.5F, worldPosition.getY() + 1F, worldPosition.getZ() + .5F, .04F, .05F, .04F,
				//						255, 150, 255
				//					);
				//					level.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
				//							.5F, worldPosition.getY() + 1F, worldPosition.getZ() + .5F, -.04F, .05F, .04F,
				//						255, 150, 255
				//					);
				//					level.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
				//							.5F, worldPosition.getY() + 1F, worldPosition.getZ() + .5F, .04F, .05F, -.04F,
				//						255, 150, 255
				//					);
				//					level.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
				//							.5F, worldPosition.getY() + 1F, worldPosition.getZ() + .5F, -.04F, .05F, -.04F,
				//						255, 150, 255
				//					);
				//				}
			}
		}

		boolean hasPower = (blockEntity.container.getStoredPower() >= CyberwareConfig.INSTANCE.TESLA_PER_POWER.get());
		if (hasPower != blockEntity.last && !level.isClientSide())
		{
			level.markAndNotifyBlock(pos, level.getChunkAt(pos), state, state, 2, 0);
			// TODO: not sure if right
			blockEntity.last = hasPower;
		}
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		return (int) container.givePower(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		return (int) container.takePower(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored()
	{
		return (int) container.getStoredPower();
	}

	@Override
	public int getMaxEnergyStored()
	{
		return (int) container.getCapacity();
	}

	@Override
	public boolean canExtract()
	{
		return false;
	}

	@Override
	public boolean canReceive()
	{
		return true;
	}
}
