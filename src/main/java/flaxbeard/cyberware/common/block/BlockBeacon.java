package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityBeacon;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.item.ItemBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Mirror;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockBeacon extends Block
{
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

	public BlockBeacon()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);

		String name = "beacon";

		setRegistryName(name);
		// ForgeRegistries.BLOCKS.register(this);

		ItemBlock itemBlock = new ItemBlockCyberware(this, "cyberware.tooltip.beacon");
		itemBlock.setRegistryName(name);
		// ForgeRegistries.ITEMS.register(itemBlock);

		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntityBeacon.class, new ResourceLocation(Cyberware.MODID, name));

		CyberwareContent.blocks.add(this);
	}

	//private static final AxisAlignedBB ew = new AxisAlignedBB(5F / 16F, 0F, 3F / 16F, 11F / 16F, 1F, 13F / 16F);
	//private static final AxisAlignedBB ns = new AxisAlignedBB(3F / 16F, 0F, 5F / 16F, 13F / 16F, 1F, 11F / 16F);
	private static final AABB bound = new AABB(1F / 16F, 0F, 1F / 16F, 15F / 16F, 4F / 16F, 15F / 16F);

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos)
	{
		return bound;
		/*
		EnumFacing face = state.getValue(FACING);
		if (face == EnumFacing.NORTH || face == EnumFacing.SOUTH)
		{
			return ew;
		}
		else
		{
			return ns;
		}
		*/
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(BlockState state)
	{
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(BlockState state)
	{
		return false;
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		return new TileEntityBeacon();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Nonnull
	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY,
										   float hitZ, int meta, LivingEntity placer)
	{
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState getStateFromMeta(int metadata)
	{
		Direction enumfacing = Direction.byIndex(metadata);

		if (enumfacing.getAxis() == Direction.Axis.Y)
		{
			enumfacing = Direction.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(BlockState blockState)
	{
		return blockState.getValue(FACING).getIndex();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState withRotation(@Nonnull BlockState blockState, Rotation rotation)
	{
		return blockState.withProperty(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState withMirror(@Nonnull BlockState blockState, Mirror mirrorIn)
	{
		return blockState.withRotation(mirrorIn.toRotation(blockState.getValue(FACING)));
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public boolean canPlaceBlockAt(Level worldIn, BlockPos pos)
	{
		//return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isFullyOpaque();
		return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isFullBlock();
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		//if (!worldIn.getBlockState(pos.down()).isFullyOpaque())
		if (!worldIn.getBlockState(pos.relative(Direction.DOWN)).isCollisionShapeFullBlock(worldIn, pos))
		{
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.removeBlock(pos, false);
		}
	}
}
