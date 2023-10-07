package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityScanner;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockScanner extends Block implements EntityBlock
{
	public BlockScanner(Properties pProperties)
	{
		super(pProperties);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(BlockState blockState)
	{
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(BlockState blockState)
	{
		return false;
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		return new TileEntityScanner();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState blockState)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState blockState, LivingEntity placer, ItemStack stack)
	{
		if (stack.hasDisplayName())
		{
			BlockEntity tileentity = world.getBlockEntity(pos);

			if (tileentity instanceof TileEntityScanner)
			{
				((TileEntityScanner) tileentity).setCustomInventoryName(stack.getDisplayName());
			}
		}
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState blockState,
									Player entityPlayer, EnumHand hand,
									Direction side, float hitX, float hitY, float hitZ)
	{
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof TileEntityScanner)
		{
			if (entityPlayer.isCreative()
				&& entityPlayer.isShiftKeyDown())
			{
				TileEntityScanner scanner = ((TileEntityScanner) tileentity);
				scanner.ticks = CyberwareConfig.INSTANCE.SCANNER_TIME.get() - 200;
			}
			entityPlayer.openGui(Cyberware.INSTANCE, 3, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

	@Override
	public void breakBlock(Level world, @Nonnull BlockPos pos, @Nonnull BlockState blockState)
	{
		BlockEntity tileentity = world.getBlockEntity(pos);

		if (tileentity instanceof TileEntityScanner
			&& !world.isClientSide())
		{
			TileEntityScanner scanner = (TileEntityScanner) tileentity;

			for (int indexSlot = 0; indexSlot < scanner.slots.getSlots(); indexSlot++)
			{
				ItemStack stack = scanner.slots.getStackInSlot(indexSlot);
				if (!stack.isEmpty())
				{
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}

		super.breakBlock(world, pos, blockState);
	}
}
