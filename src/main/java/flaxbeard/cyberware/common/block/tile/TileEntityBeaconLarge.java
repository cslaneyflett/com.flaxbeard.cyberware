package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.BlockBeaconLarge;
import flaxbeard.cyberware.common.block.BlockBeaconPost;
import flaxbeard.cyberware.common.lib.LibConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class TileEntityBeaconLarge extends TileEntityBeacon implements ITickable
{
	private boolean wasWorking = false;
	private int count = 0;
	private static int TIER = 3;

	@Override
	public void update()
	{
		BlockState master = level.getBlockState(worldPosition.offset(0, -10, 0));

		boolean powered = level.hasNeighborSignal(worldPosition.offset(1, -10, 0))
			|| level.hasNeighborSignal(worldPosition.offset(-1, -10, 0))
			|| level.hasNeighborSignal(worldPosition.offset(0, -10, 1))
			|| level.hasNeighborSignal(worldPosition.offset(0, -10, -1));
		boolean working =
			!powered && master.getBlock() == CyberwareContent.radioPost && master.getValue(BlockBeaconPost.TRANSFORMED) == 2;

		if (!wasWorking && working)
		{
			this.enable();
		}

		if (wasWorking && !working)
		{
			disable();
		}

		wasWorking = working;

		if (level.isClientSide() && working)
		{
			count = (count + 1) % 20;
			if (count == 0)
			{
				BlockState state = level.getBlockState(worldPosition);
				if (state.getBlock() == CyberwareContent.radioLarge)
				{
					boolean ns =
						state.getValue(BlockBeaconLarge.FACING) == Direction.EAST || state.getValue(BlockBeaconLarge.FACING) == Direction.WEST;
					float dist = .5F;
					float speedMod = .2F;
					int degrees = 45;
					for (int index = 0; index < 18; index++)
					{
						float sin = (float) Math.sin(Math.toRadians(degrees));
						float cos = (float) Math.cos(Math.toRadians(degrees));
						float xOffset = dist * sin;
						float yOffset = .2F + dist * cos;
						float xSpeed = speedMod * sin;
						float ySpeed = speedMod * cos;
						// TODO: particles
						//						level.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
						//								worldPosition.getX() + .5F + (ns ? xOffset : 0),
						//								worldPosition.getY() + .5F + yOffset,
						//								worldPosition.getZ() + .5F + (ns ? 0 : xOffset),
						//								ns ? xSpeed : 0,
						//								ySpeed,
						//								ns ? 0 : xSpeed,
						//								255, 255, 255 );
						//
						//						level.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
						//								worldPosition.getX() + .5F - (ns ? xOffset : 0),
						//								worldPosition.getY() + .5F + yOffset,
						//								worldPosition.getZ() + .5F - (ns ? 0 : xOffset),
						//								ns ? -xSpeed : 0,
						//								ySpeed,
						//								ns ? 0 : -xSpeed,
						//								255, 255, 255 );

						degrees += 5;
					}
				}
			}
		}
	}

	private void disable()
	{
		Map<BlockPos, Integer> mapBeaconPosition = getBeaconPositionsForTierAndDimension(TIER, level);
		mapBeaconPosition.remove(worldPosition);
	}

	private void enable()
	{
		Map<BlockPos, Integer> mapBeaconPosition = getBeaconPositionsForTierAndDimension(TIER, level);
		if (!mapBeaconPosition.containsKey(worldPosition))
		{
			mapBeaconPosition.put(worldPosition, LibConstants.LARGE_BEACON_RANGE);
		}
	}

	//	@Override
	//	public void invalidate()
	//	{
	//		disable();
	//		super.invalidate();
	//	}
}
