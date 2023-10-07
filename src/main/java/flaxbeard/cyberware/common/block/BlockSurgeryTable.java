package flaxbeard.cyberware.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public class BlockSurgeryTable extends BedBlock
{
	public BlockSurgeryTable(Properties pProperties)
	{
		super(DyeColor.WHITE, pProperties);
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

		if (blockState.getValue(PART) != BedPart.HEAD)
		{
			pos = pos.relative(blockState.getValue(FACING));
			blockState = world.getBlockState(pos);

			if (blockState.getBlock() != this)
			{
				return true;
			}
		}

		if (world.dimensionType().bedWorks())
		{
			if (blockState.getValue(OCCUPIED))
			{
				Player entityplayer = this.getPlayerInBed(world, pos);

				if (entityplayer != null)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.occupied"));
					return true;
				}

				blockState = blockState.setValue(OCCUPIED, Boolean.FALSE);
				world.setBlockState(pos, blockState, 4);
			}

			Player.BedSleepingProblem entityplayer$sleepresult = entityPlayer.trySleep(pos);

			if (entityplayer$sleepresult == Player.BedSleepingProblem.OK)
			{
				blockState = blockState.setValue(OCCUPIED, Boolean.TRUE);
				world.setBlockState(pos, blockState, 4);
				return true;
			} else
			{
				if (entityplayer$sleepresult == Player.BedSleepingProblem.NOT_POSSIBLE_NOW)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.noSleep"));
				} else if (entityplayer$sleepresult == Player.BedSleepingProblem.NOT_SAFE)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.notSafe"));
				}

				return true;
			}
		} else
		{
			world.removeBlock(pos, false);
			BlockPos blockpos = pos.offset(blockState.getValue(FACING).getOpposite());

			if (world.getBlockState(blockpos).getBlock() == this)
			{
				world.removeBlock(blockpos, false);
			}

			world.explode(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
				(double) pos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.BREAK
			);
			return true;
		}
	}

	@Nullable
	private Player getPlayerInBed(Level worldIn, BlockPos pos)
	{
		for (Player entityplayer : worldIn.players())
		{
			if (entityplayer.isSleeping() && entityplayer.getSleepingPos().orElseThrow().equals(pos))
			{
				return entityplayer;
			}
		}

		return null;
	}

	@Override
	public boolean isBed(BlockState state, LevelReader world, BlockPos pos, Entity player)
	{
		return true;
	}

	@SubscribeEvent
	public void handleSleep(PlayerSleepInBedEvent event)
	{
		// no operation
	}
}
