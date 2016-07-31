package flaxbeard.cyberware.common.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;

public class BlockEngineeringTable extends BlockContainer
{

	public BlockEngineeringTable()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
		
		String name = "engineeringTable";
		
		this.setRegistryName(name);
		GameRegistry.register(this);

		ItemBlock ib = new ItemBlockCyberware(this);
		ib.setRegistryName(name);
		GameRegistry.register(ib);
		
		this.setUnlocalizedName(Cyberware.MODID + "." + name);

		this.setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntityEngineeringTable.class, Cyberware.MODID + ":" + name);
		
		CyberwareContent.blocks.add(this);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if (stack.hasDisplayName())
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);

			if (tileentity instanceof TileEntityEngineeringTable)
			{
				((TileEntityEngineeringTable) tileentity).setCustomInventoryName(stack.getDisplayName());
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityEngineeringTable();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}
	

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileentity = worldIn.getTileEntity(pos);
		
		if (tileentity instanceof TileEntityEngineeringTable)
		{
			player.openGui(Cyberware.INSTANCE, 2, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return true;
		
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{ 
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if (tileentity instanceof TileEntityEngineeringTable && !worldIn.isRemote)
		{
			TileEntityEngineeringTable engineering = (TileEntityEngineeringTable) tileentity;
			
			for (int i = 0; i < engineering.slots.getSlots(); i++)
			{
				ItemStack stack = engineering.slots.getStackInSlot(i);
				if (stack != null)
				{
					InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}
		super.breakBlock(worldIn, pos, state);

	}

}
