package flaxbeard.cyberware.common.block.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

// TODO: completely refactor this to target forge energy, low priority
public class PowerContainer implements INBTSerializable<CompoundTag>
{
	private long stored;
	private long capacity;
	private long inputRate;
	private long outputRate;

	public PowerContainer()
	{
		this.stored = 0;
		this.capacity = 5000;
		this.inputRate = 50;
		this.outputRate = 50;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		final CompoundTag tag = new CompoundTag();
		tag.putLong("power", stored);
		tag.putLong("capacity", capacity);
		tag.putLong("input", inputRate);
		tag.putLong("output", outputRate);

		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tagCompound)
	{
		this.stored = tagCompound.getLong("power");
		this.capacity = tagCompound.getLong("capacity");
		this.inputRate = tagCompound.getLong("input");
		this.outputRate = tagCompound.getLong("output");

		if (this.stored > this.getCapacity())
		{
			this.stored = this.getCapacity();
		}
	}

	// @Override
	public long getCapacity()
	{
		return capacity;
	}

	// @Override
	public long getStoredPower()
	{
		return stored;
	}

	// @Override
	public long givePower(long Tesla, boolean simulated)
	{
		final long acceptedTesla = Math.min(this.getCapacity() - this.stored, Math.min(inputRate, Tesla));

		if (!simulated)
		{
			this.stored += acceptedTesla;
		}

		return acceptedTesla;
	}

	// @Override
	public long takePower(long Tesla, boolean simulated)
	{
		final long removedPower = Math.min(this.stored, Math.min(outputRate, Tesla));

		if (!simulated)
		{
			this.stored -= removedPower;
		}

		return removedPower;
	}
}
