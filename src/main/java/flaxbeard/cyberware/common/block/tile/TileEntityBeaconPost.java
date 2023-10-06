package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.BlockBeaconPost;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TileEntityBeaconPost extends BlockEntity
{
	public static class TileEntityBeaconPostMaster extends TileEntityBeaconPost
	{
		@OnlyIn(Dist.CLIENT)
		@Nonnull
		@Override
		public AABB getRenderBoundingBox()
		{
			return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1,
				worldPosition.getX() + 2, worldPosition.getY() + 10, worldPosition.getZ() + 2
			);
		}

		@Override
		public void setMasterLoc(BlockPos start)
		{
			throw new IllegalStateException("NO");
		}
	}

	public BlockPos master = null;
	public boolean destructing = false;

	@OnlyIn(Dist.CLIENT)
	@Override
	public double getMaxRenderDistanceSquared()
	{
		return 16384.0D;
	}

	public void setMasterLoc(BlockPos start)
	{
		this.master = start;
		level.notifyBlockUpdate(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition),
			2
		);
		this.markDirty();
	}

	//	@Override
	//	public void invalidate()
	//	{
	//
	//		super.invalidate();
	//	}

	public void destruct()
	{
		if (!destructing)
		{
			destructing = true;
			for (int y = 0; y <= 9; y++)
			{
				for (int x = -1; x <= 1; x++)
				{
					for (int z = -1; z <= 1; z++)
					{
						if (y > 3 && (x != 0 || z != 0))
						{
							continue;
						}

						BlockPos newPos = worldPosition.offset(x, y, z);

						BlockState state = level.getBlockState(newPos);
						Block block = state.getBlock();
						if (block == CyberwareContent.radioPost && state.getValue(BlockBeaconPost.TRANSFORMED) > 0)
						{
							level.getBlockEntity(newPos);
							level.setBlockState(newPos, state.withProperty(BlockBeaconPost.TRANSFORMED, 0), 2);
						}
					}
				}
			}
		}
	}

	@Override
	public void readFromNBT(CompoundTag tagCompound)
	{
		super.readFromNBT(tagCompound);

		if (!(this instanceof TileEntityBeaconPostMaster))
		{
			int x = tagCompound.getInt("xx");
			int y = tagCompound.getInt("yy");
			int z = tagCompound.getInt("zz");
			this.master = new BlockPos(x, y, z);
		}
	}

	@Override
	public void onDataPacket(Connection net, SPacketUpdateTileEntity pkt)
	{
		CompoundTag data = pkt.getNbtCompound();
		this.readFromNBT(data);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
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

	@Nonnull
	@Override
	public CompoundTag writeToNBT(CompoundTag tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);

		if (!(this instanceof TileEntityBeaconPostMaster))
		{
			tagCompound.putInt("xx", master.getX());
			tagCompound.putInt("yy", master.getY());
			tagCompound.putInt("zz", master.getZ());
		}

		return tagCompound;
	}
}
