package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
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

public class BlockSurgery extends Block implements EntityBlock
{
	public BlockSurgery(Properties pProperties)
	{
		super(pProperties);
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		return new TileEntitySurgery();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState blockState)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState blockState,
									Player entityPlayer, EnumHand hand,
									Direction side, float hitX, float hitY, float hitZ)
	{
		BlockEntity tileEntity = world.getBlockEntity(pos);

		if (tileEntity instanceof TileEntitySurgery)
		{
			TileEntitySurgery tileEntitySurgery = (TileEntitySurgery) tileEntity;

			//Ensure the Base Tolerance Attribute has been updated for any Config Changes
			entityPlayer.getEntityAttribute(CyberwareAPI.TOLERANCE_ATTR).setBaseValue(CyberwareConfig.INSTANCE.ESSENCE.get());

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			tileEntitySurgery.updatePlayerSlots(entityPlayer, cyberwareUserData);
			entityPlayer.openGui(Cyberware.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

	@Override
	public void breakBlock(Level world, @Nonnull BlockPos pos, @Nonnull BlockState blockState)
	{
		BlockEntity tileentity = world.getBlockEntity(pos);

		if (tileentity instanceof TileEntitySurgery
			&& !world.isClientSide())
		{
			TileEntitySurgery surgery = (TileEntitySurgery) tileentity;

			for (int indexSlot = 0; indexSlot < surgery.slots.getSlots(); indexSlot++)
			{
				ItemStack stack = surgery.slots.getStackInSlot(indexSlot);
				if (!stack.isEmpty())
				{
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}

		super.breakBlock(world, pos, blockState);
	}
}
