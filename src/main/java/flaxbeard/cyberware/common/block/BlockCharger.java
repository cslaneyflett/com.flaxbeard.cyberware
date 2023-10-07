package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.common.block.tile.TileEntityCharger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCharger extends Block implements EntityBlock
{
	public BlockCharger(Properties pProperties)
	{
		super(pProperties);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState)
	{
		return new TileEntityCharger(pPos, pState);
	}
}
