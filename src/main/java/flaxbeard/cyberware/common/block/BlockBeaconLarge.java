package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityBeaconLarge;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.item.ItemBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Mirror;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockBeaconLarge extends Block
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockBeaconLarge()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);

		String name = "beacon_large";

		setRegistryName(name);
		// ForgeRegistries.BLOCKS.register(this);

		ItemBlock itemBlock = new ItemBlockCyberware(this, "cyberware.tooltip.beacon_large");
		itemBlock.setRegistryName(name);
		// ForgeRegistries.ITEMS.register(itemBlock);

		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntityBeaconLarge.class, new ResourceLocation(Cyberware.MODID, name));

		CyberwareContent.blocks.add(this);
	}

	private static final AABB ew = new AABB(5F / 16F, 0F, 3F / 16F, 11F / 16F, 1F, 13F / 16F);
	private static final AABB ns = new AABB(3F / 16F, 0F, 5F / 16F, 13F / 16F, 1F, 11F / 16F);
	private static final AABB middle = new AABB(6.5F / 16F, 0F, 6.5F / 16F, 9.5F / 16F, 1F, 9.5F / 16F);

	@Override
	public void addCollisionBoxToList(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos,
									  @Nonnull AABB entityBox, @Nonnull List<AABB> collidingBoxes,
									  @Nullable Entity entity, boolean isActualState)
	{
		addCollisionBoxToList(pos, entityBox, collidingBoxes, middle);
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos)
	{
		Direction face = state.getValue(FACING);
		if (face == Direction.NORTH || face == Direction.SOUTH)
		{
			return ew;
		} else
		{
			return ns;
		}
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
		return new TileEntityBeaconLarge();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return EnumBlockRenderType.MODEL;
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
}
