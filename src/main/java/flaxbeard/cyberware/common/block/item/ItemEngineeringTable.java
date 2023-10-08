package flaxbeard.cyberware.common.block.item;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class ItemEngineeringTable extends ItemBlockCyberware
{
	public ItemEngineeringTable(Block block, Properties properties, String... tooltip)
	{
		super(block, properties, tooltip);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext useOnContext)
	{
		ItemStack stack = useOnContext.getItemInHand();
		if (useOnContext.getClickedFace() != Direction.UP)
		{
			return InteractionResult.FAIL;
		} else
		{
			return super.useOn(useOnContext);
		}
	}

	//	public static void placeDoor(Level worldIn, BlockPos pos, Direction facing, Block door)
	//	{
	//		BlockPos blockpos2 = pos.above();
	//
	//		BlockState iblockstate = door.defaultBlockState().setValue(BlockEngineeringTable.FACING, facing);
	//		worldIn.setBlockState(pos, iblockstate.setValue(
	//			BlockEngineeringTable.HALF,
	//			BlockEngineeringTable.EnumEngineeringHalf.LOWER
	//		), 2);
	//		worldIn.setBlockState(blockpos2, iblockstate.setValue(
	//			BlockEngineeringTable.HALF,
	//			BlockEngineeringTable.EnumEngineeringHalf.UPPER
	//		), 2);
	//		worldIn.updateNeighborsAt(pos, door);
	//		worldIn.updateNeighborsAt(blockpos2, door);
	//	}
}