package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import net.minecraft.block.BlockBed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.init.Biomes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

public class BlockSurgeryTable extends Block
{
	public BlockSurgeryTable()
	{
		String name = "surgeryTable";

		setRegistryName(name);
		// ForgeRegistries.BLOCKS.register(this);

		setTranslationKey(Cyberware.MODID + "." + name);

		GameRegistry.registerTileEntity(TileEntitySurgery.class, new ResourceLocation(Cyberware.MODID, name));

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState blockState,
									Player entityPlayer, EnumHand hand,
									Direction side, float hitX, float hitY, float hitZ)
	{
		if (world.isClientSide())
		{
			return true;
		}

		if (blockState.getValue(PART) != BlockBed.EnumPartType.HEAD)
		{
			pos = pos.offset(blockState.getValue(FACING));
			blockState = world.getBlockState(pos);

			if (blockState.getBlock() != this)
			{
				return true;
			}
		}

		if (world.provider.canRespawnHere()
			&& world.getBiome(pos) != Biomes.HELL)
		{
			if (blockState.getValue(OCCUPIED))
			{
				Player entityplayer = this.getPlayerInBed(world, pos);

				if (entityplayer != null)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.occupied"));
					return true;
				}

				blockState = blockState.withProperty(OCCUPIED, Boolean.FALSE);
				world.setBlockState(pos, blockState, 4);
			}

			Player.SleepResult entityplayer$sleepresult = entityPlayer.trySleep(pos);

			if (entityplayer$sleepresult == Player.SleepResult.OK)
			{
				blockState = blockState.withProperty(OCCUPIED, Boolean.TRUE);
				world.setBlockState(pos, blockState, 4);
				return true;
			} else
			{
				if (entityplayer$sleepresult == Player.SleepResult.NOT_POSSIBLE_NOW)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.noSleep"));
				} else if (entityplayer$sleepresult == Player.SleepResult.NOT_SAFE)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.notSafe"));
				}

				return true;
			}
		} else
		{
			world.setBlockToAir(pos);
			BlockPos blockpos = pos.offset(blockState.getValue(FACING).getOpposite());

			if (world.getBlockState(blockpos).getBlock() == this)
			{
				world.setBlockToAir(blockpos);
			}

			world.newExplosion(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
				(double) pos.getZ() + 0.5D, 5.0F, true, true
			);
			return true;
		}
	}

	@Nullable
	private Player getPlayerInBed(Level worldIn, BlockPos pos)
	{
		for (Player entityplayer : worldIn.playerEntities)
		{
			if (entityplayer.isPlayerSleeping() && entityplayer.getPosition().equals(pos))
			{
				return entityplayer;
			}
		}

		return null;
	}

	@Override
	public boolean isBed(BlockState state, IBlockAccess world, BlockPos pos, Entity player)
	{
		return true;
	}

	@SubscribeEvent
	public void handleSleep(PlayerSleepInBedEvent event)
	{
		// no operation
	}
}
