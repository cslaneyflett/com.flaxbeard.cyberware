package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.common.block.BlockBeaconPost;
import flaxbeard.cyberware.common.registry.CWBlockEntities;
import flaxbeard.cyberware.common.registry.CWBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TileEntityBeaconPost extends BlockEntity
{
	public TileEntityBeaconPost(BlockPos pPos, BlockState pBlockState)
	{
		super(CWBlockEntities.BEACON_POST.get(), pPos, pBlockState);
	}

	public TileEntityBeaconPost(BlockEntityType<? extends TileEntityBeaconPost> pType, BlockPos pPos, BlockState pBlockState)
	{
		super(pType, pPos, pBlockState);
	}

	public static class TileEntityBeaconPostMaster extends TileEntityBeaconPost
	{
		public TileEntityBeaconPostMaster(BlockPos pPos, BlockState pBlockState)
		{
			super(CWBlockEntities.BEACON_POST_MASTER.get(), pPos, pBlockState);
		}

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

	//	@OnlyIn(Dist.CLIENT)
	//	@Override
	//	public double getMaxRenderDistanceSquared()
	//	{
	//		return 16384.0D;
	//	}

	public void setMasterLoc(BlockPos start)
	{
		this.master = start;
		assert level != null;
		level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition),
			2
		);
		this.setChanged();
	}

	//	@Override
	//	public void invalidate()
	//	{
	//
	//		super.invalidate();
	//	}

	public void destruct()
	{
		assert level != null;

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
						if (block == CWBlocks.BEACON_POST.get() && state.getValue(BlockBeaconPost.TRANSFORMED) > 0)
						{
							level.getBlockEntity(newPos);
							level.getChunk(newPos).setBlockState(newPos, state.setValue(BlockBeaconPost.TRANSFORMED, 0), false);
						}
					}
				}
			}
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

	@Override
	public void saveAdditional(@Nonnull CompoundTag tagCompound)
	{
		super.saveAdditional(tagCompound);

		if (!(this instanceof TileEntityBeaconPostMaster))
		{
			tagCompound.putInt("xx", master.getX());
			tagCompound.putInt("yy", master.getY());
			tagCompound.putInt("zz", master.getZ());
		}
	}

	@Override
	public void load(@Nonnull CompoundTag tagCompound)
	{
		super.load(tagCompound);

		if (!(this instanceof TileEntityBeaconPostMaster))
		{
			int x = tagCompound.getInt("xx");
			int y = tagCompound.getInt("yy");
			int z = tagCompound.getInt("zz");
			this.master = new BlockPos(x, y, z);
		}
	}
}
