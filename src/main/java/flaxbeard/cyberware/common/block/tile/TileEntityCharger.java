package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ITickable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

// TODO: EnergyStorage
public class TileEntityCharger extends BlockEntity implements ITickable, IEnergyStorage
{
	private final PowerContainer container = new PowerContainer();
	private final LazyOptional<PowerContainer> lazyContainer = LazyOptional.of(() -> this.container);
	private final LazyOptional<TileEntityCharger> lazySelf = LazyOptional.of(() -> this);
	private boolean last = false;

	public TileEntityCharger(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
	{
		super(pType, pPos, pBlockState);
	}

	//	@Override
	//	public void readFromNBT(CompoundTag tagCompound)
	//	{
	//		super.readFromNBT(tagCompound);
	//
	//		container.deserializeNBT(tagCompound.getCompound("power"));
	//	}
	//
	//	@Nonnull
	//	@Override
	//	public CompoundTag writeToNBT(CompoundTag tagCompound)
	//	{
	//		tagCompound = super.writeToNBT(tagCompound);
	//		tagCompound.put("power", container.serializeNBT());
	//		return tagCompound;
	//	}
	//
	//	@Override
	//	public void onDataPacket(Connection net, SPacketUpdateTileEntity pkt)
	//	{
	//		CompoundTag data = pkt.getNbtCompound();
	//		this.readFromNBT(data);
	//	}
	//
	//	@Override
	//	public SPacketUpdateTileEntity getUpdatePacket()
	//	{
	//		CompoundTag data = new CompoundTag();
	//		this.writeToNBT(data);
	//		return new SPacketUpdateTileEntity(pos, 0, data);
	//	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag()
	{
		return writeToNBT(new CompoundTag());
	}

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

	@Override
	public void update()
	{
		List<LivingEntity> entitiesInRange = level.getEntitiesOfClass(
			LivingEntity.class,
			new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
				worldPosition.getX() + 1F, worldPosition.getY() + 2.5F, worldPosition.getZ() + 1F
			)
		);
		for (LivingEntity entityInRange : entitiesInRange)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityInRange);
			if (cyberwareUserData != null
				&& !cyberwareUserData.isAtCapacity(ItemStack.EMPTY, 20)
				&& (container.getStoredPower() >= CyberwareConfig.INSTANCE.TESLA_PER_POWER.get()))
			{
				if (!level.isClientSide())
				{
					container.takePower(CyberwareConfig.INSTANCE.TESLA_PER_POWER.get(), false);
				}
				cyberwareUserData.addPower(20, ItemStack.EMPTY);

				if (entityInRange.tickCount % 5 == 0)
				{
					// TODO: particles
					//					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
					//					.5F,worldPosition.getY() + 1F, worldPosition.getZ() + .5F, 0F, .05F, 0F, 255,
					//					150, 255);
					//					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
					//					.5F,worldPosition.getY() + 1F, worldPosition.getZ() + .5F, .04F, .05F, .04F,
					//					255, 150, 255);
					//					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
					//					.5F,worldPosition.getY() + 1F, worldPosition.getZ() + .5F, -.04F, .05F, .04F,
					//					255, 150, 255);
					//					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
					//					.5F,worldPosition.getY() + 1F, worldPosition.getZ() + .5F, .04F, .05F, -.04F,
					//					255, 150, 255);
					//					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, worldPosition.getX() +
					//					.5F,worldPosition.getY() + 1F, worldPosition.getZ() + .5F, -.04F, .05F, -.04F,
					//					255, 150, 255);
				}
			}
		}

		boolean hasPower = (container.getStoredPower() >= CyberwareConfig.INSTANCE.TESLA_PER_POWER.get());
		if (hasPower != last && !level.isClientSide())
		{
			BlockState state = level.getBlockState(worldPosition);
			level.markAndNotifyBlock(worldPosition, level.getChunkAt(worldPosition), state, state, 2, 0); // TODO: not
			// sure if right
			last = hasPower;
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
