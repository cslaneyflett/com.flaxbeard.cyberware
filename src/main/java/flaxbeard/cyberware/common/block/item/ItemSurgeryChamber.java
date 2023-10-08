package flaxbeard.cyberware.common.block.item;

import flaxbeard.cyberware.api.item.ICyberwareTabItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemSurgeryChamber extends ItemBlockCyberware
{
	public ItemSurgeryChamber(Block block, Properties properties, String... tooltip)
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
	//		BlockState iblockstate = door.defaultBlockState().setValue(BlockSurgeryChamber.FACING, facing);
	//		worldIn.setBlockState(pos, iblockstate.setValue(
	//			BlockSurgeryChamber.HALF,
	//			BlockSurgeryChamber.EnumChamberHalf.LOWER
	//		), 2);
	//		worldIn.setBlockState(blockpos2, iblockstate.setValue(
	//			BlockSurgeryChamber.HALF,
	//			BlockSurgeryChamber.EnumChamberHalf.UPPER
	//		), 2);
	//		worldIn.updateNeighborsAt(pos, door);
	//		worldIn.updateNeighborsAt(blockpos2, door);
	//	}
}