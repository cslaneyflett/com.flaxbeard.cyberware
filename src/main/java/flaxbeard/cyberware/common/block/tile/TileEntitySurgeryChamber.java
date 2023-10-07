package flaxbeard.cyberware.common.block.tile;

import flaxbeard.cyberware.common.registry.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySurgeryChamber extends BlockEntity
{
	public boolean lastOpen;
	public float openTicks;

	public TileEntitySurgeryChamber(BlockPos pPos, BlockState pBlockState)
	{
		super(BlockEntities.SURGERY_CHAMBER.get(), pPos, pBlockState);
	}
}
