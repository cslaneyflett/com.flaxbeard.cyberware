package flaxbeard.cyberware.common.block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySurgeryChamber extends BlockEntity
{
	public boolean lastOpen;
	public float openTicks;

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newState)
	{
		return oldState.getBlock() != newState.getBlock();
	}
}
