package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.item.ItemBrainUpgrade;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

public class TileEntityBeacon extends BlockEntity
{
	private static final List<Integer> tiers = new CopyOnWriteArrayList<>();
	private static final Map<Integer, Map<ResourceKey<Level>, Map<BlockPos, Integer>>> mapBeaconPositionByTierDimension = new HashMap<>();
	private boolean wasWorking = false;
	private int count = 0;
	private static int TIER = 2;

	public TileEntityBeacon(BlockPos pPos, BlockState pBlockState)
	{
		super(BlockEntities.BEACON.get(), pPos, pBlockState);
	}

	public TileEntityBeacon(BlockEntityType<? extends TileEntityBeacon> pType, BlockPos pPos, BlockState pBlockState)
	{
		super(pType, pPos, pBlockState);
	}

	private static Map<ResourceKey<Level>, Map<BlockPos, Integer>> getBeaconPositionsForTier(int tier)
	{
		Map<ResourceKey<Level>, Map<BlockPos, Integer>> mapBeaconPositionByDimension = mapBeaconPositionByTierDimension.get(tier);
		if (mapBeaconPositionByDimension == null)
		{
			mapBeaconPositionByDimension = new HashMap<>();
			mapBeaconPositionByTierDimension.put(tier, mapBeaconPositionByDimension);
			if (!tiers.contains(tier))
			{
				tiers.add(tier);
				Collections.sort(tiers);
				Collections.reverse(tiers);
			}
		}

		return mapBeaconPositionByDimension;
	}

	public static Map<BlockPos, Integer> getBeaconPositionsForTierAndDimension(int tier, @Nonnull Level level)
	{
		Map<ResourceKey<Level>, Map<BlockPos, Integer>> mapBeaconPositionByDimension = getBeaconPositionsForTier(tier);
		ResourceKey<Level> idDimension = level.dimension();

		return mapBeaconPositionByDimension.computeIfAbsent(idDimension, k -> new HashMap<>());
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be)
	{
		assert level != null;
		var blockEntity = (TileEntityBeacon) be;

		boolean working = level.hasNeighborSignal(pos);

		if (!blockEntity.wasWorking && working)
		{
			blockEntity.enable();
		}

		if (blockEntity.wasWorking && !working)
		{
			blockEntity.disable();
		}

		blockEntity.wasWorking = working;

		// TODO particle

		//		if (level.isClientSide() && working)
		//		{
		//			ClientLevel clientLevel = (ClientLevel) level;
		//			count = (count + 1) % 20;
		//			if (count == 0)
		//			{
		//				BlockState state = clientLevel.getBlockState(worldPosition);
		//				if (state.getBlock() == CyberwareContent.radio)
		//				{
		//					boolean ns = state.getValue(BlockBeaconLarge.FACING) == Direction.NORTH
		//						|| state.getValue(BlockBeaconLarge.FACING) == Direction.SOUTH;
		//					boolean backwards = state.getValue(BlockBeaconLarge.FACING) == Direction.SOUTH
		//						|| state.getValue(BlockBeaconLarge.FACING) == Direction.EAST;
		//
		//					float dist = .2F;
		//					float speedMod = .08F;
		//					int degrees = 45;
		//
		//					for (int index = 0; index < 5; index++)
		//					{
		//						float sin = (float) Math.sin(Math.toRadians(degrees));
		//						float cos = (float) Math.cos(Math.toRadians(degrees));
		//						float xOffset = dist * sin;
		//						float yOffset = .2F + dist * cos;
		//						float xSpeed = speedMod * sin;
		//						float ySpeed = speedMod * cos;
		//						float backOffsetX = (backwards ^ ns ? -.3F : .3F);
		//						float backOffsetZ = (backwards ? .4F : -.4F);
		//
		//						 clientLevel.spawnParticle(ParticleTypes.SMOKE,
		//							 worldPosition.getX() + .5F + (ns ? xOffset + backOffsetX :
		//								 backOffsetZ),
		//							 worldPosition.getY() + .5F + yOffset,
		//							 worldPosition.getZ() + .5F + (ns ? backOffsetZ : xOffset +
		//								 backOffsetX),
		//							 ns ? xSpeed : 0,
		//							 ySpeed,
		//							 ns ? 0 : xSpeed,
		//							 255, 255, 255
		//						 );
		//
		//						clientLevel.spawnParticle(ParticleTypes.SMOKE,
		//							worldPosition.getX() + .5F + (ns ? -xOffset + backOffsetX :
		//								backOffsetZ),
		//							worldPosition.getY() + .5F + yOffset,
		//							worldPosition.getZ() + .5F + (ns ? backOffsetZ : -xOffset +
		//								backOffsetX),
		//							ns ? -xSpeed : 0,
		//							ySpeed,
		//							ns ? 0 : -xSpeed,
		//							255, 255, 255
		//						);
		//
		//						degrees += 18;
		//					}
		//				}
		//			}
		//		}
	}

	private void disable()
	{
		assert level != null;
		Map<BlockPos, Integer> mapBeaconPosition = getBeaconPositionsForTierAndDimension(TIER, level);
		mapBeaconPosition.remove(getBlockPos());
	}

	private void enable()
	{
		assert level != null;
		Map<BlockPos, Integer> mapBeaconPosition = getBeaconPositionsForTierAndDimension(TIER, level);
		if (!mapBeaconPosition.containsKey(getBlockPos()))
		{
			mapBeaconPosition.put(getBlockPos(), LibConstants.BEACON_RANGE);
		}
	}

	//	@Override
	//	public void invalidate() {
	//		disable();
	//		super.invalidate();
	//	}

	public static int isInRange(Level level, double posX, double posY, double posZ)
	{
		for (int tier : tiers)
		{
			Map<BlockPos, Integer> mapBeaconPosition = getBeaconPositionsForTierAndDimension(tier, level);
			for (Entry<BlockPos, Integer> entry : mapBeaconPosition.entrySet())
			{
				double squareDistance = (posX - entry.getKey().getX()) * (posX - entry.getKey().getX())
					+ (posZ - entry.getKey().getZ()) * (posZ - entry.getKey().getZ());
				if (squareDistance < entry.getValue() * entry.getValue())
				{
					return tier;
				}
			}
		}

		List<Player> entitiesInRange = level.getEntitiesOfClass(
			Player.class,
			new AABB(posX - LibConstants.BEACON_RANGE_INTERNAL, 0, posZ - LibConstants.BEACON_RANGE_INTERNAL,
				posX + LibConstants.BEACON_RANGE_INTERNAL, 255, posZ + LibConstants.BEACON_RANGE_INTERNAL
			)
		);

		ItemStack itemStackRadioRaw = CyberwareContent.brainUpgrades.getCachedStack(ItemBrainUpgrade.META_RADIO);
		for (LivingEntity entityInRange : entitiesInRange)
		{
			if (ItemBrainUpgrade.isRadioWorking(entityInRange))
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityInRange);
				if (cyberwareUserData != null)
				{
					ItemStack itemStackRadio = cyberwareUserData.getCyberware(itemStackRadioRaw);
					if (!itemStackRadio.isEmpty()
						&& EnableDisableHelper.isEnabled(itemStackRadio))
					{
						return 1;
					}
				}
			}
		}

		return -1;
	}
}
