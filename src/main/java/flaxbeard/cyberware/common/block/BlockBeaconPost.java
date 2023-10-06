package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityBeaconPost;
import flaxbeard.cyberware.common.block.tile.TileEntityBeaconPost.TileEntityBeaconPostMaster;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemLead;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockBeaconPost extends Block
{
	/**
	 * Whether this fence connects in the northern direction
	 */
	public static final PropertyBool NORTH = PropertyBool.create("north");
	/**
	 * Whether this fence connects in the eastern direction
	 */
	public static final PropertyBool EAST = PropertyBool.create("east");
	/**
	 * Whether this fence connects in the southern direction
	 */
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	/**
	 * Whether this fence connects in the western direction
	 */
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyInteger TRANSFORMED = PropertyInteger.create("transformed", 0, 2);
	protected static final AABB[] BOUNDING_BOXES = new AABB[]{new AABB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D),
		new AABB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 1.0D), new AABB(0.0D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D),
		new AABB(0.0D, 0.0D, 0.375D, 0.625D, 1.0D, 1.0D), new AABB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 0.625D),
		new AABB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D), new AABB(0.0D, 0.0D, 0.0D, 0.625D, 1.0D, 0.625D),
		new AABB(0.0D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D), new AABB(0.375D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D),
		new AABB(0.375D, 0.0D, 0.375D, 1.0D, 1.0D, 1.0D), new AABB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D),
		new AABB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 1.0D), new AABB(0.375D, 0.0D, 0.0D, 1.0D, 1.0D, 0.625D),
		new AABB(0.375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.625D),
		new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};
	public static final AABB PILLAR_AABB = new AABB(0.375D, 0.0D, 0.375D, 0.625D, 1D, 0.625D);
	public static final AABB SOUTH_AABB = new AABB(0.375D, 0.0D, 0.625D, 0.625D, 1D, 1.0D);
	public static final AABB WEST_AABB = new AABB(0.0D, 0.0D, 0.375D, 0.375D, 1D, 0.625D);
	public static final AABB NORTH_AABB = new AABB(0.375D, 0.0D, 0.0D, 0.625D, 1D, 0.375D);
	public static final AABB EAST_AABB = new AABB(0.625D, 0.0D, 0.375D, 1.0D, 1D, 0.625D);

	public BlockBeaconPost()
	{
		super(Material.IRON);

		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);

		String name = "radio_post";

		setRegistryName(name);
		// ForgeRegistries.BLOCKS.register(this);


		ItemBlock itemBlock = new ItemBlockCyberware(
			this,
			"cyberware.tooltip.beacon_post.0",
			"cyberware.tooltip.beacon_post.1",
			"cyberware.tooltip.beacon_post.2"
		);
		itemBlock.setRegistryName(name);
		// ForgeRegistries.ITEMS.register(itemBlock);

		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);

		CyberwareContent.blocks.add(this);

		GameRegistry.registerTileEntity(TileEntityBeaconPost.class, new ResourceLocation(Cyberware.MODID, name));
		GameRegistry.registerTileEntity(TileEntityBeaconPostMaster.class, new ResourceLocation(
			Cyberware.MODID,
			name + "_master"
		));

		setDefaultState(blockState.getBaseState()
			.withProperty(TRANSFORMED, 0)
			.withProperty(NORTH, Boolean.FALSE)
			.withProperty(EAST, Boolean.FALSE)
			.withProperty(SOUTH, Boolean.FALSE)
			.withProperty(WEST, Boolean.FALSE));
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos blockPos, BlockState blockState, LivingEntity placer,
								ItemStack itemStack)
	{
		super.onBlockPlacedBy(world, blockPos, blockState, placer, itemStack);

		for (int y = -9; y <= 0; y++)
		{
			for (int x = -1; x <= 1; x++)
			{
				for (int z = -1; z <= 1; z++)
				{
					BlockPos start = blockPos.add(x, y, z);

					boolean isCompleted = complete(world, start);
					if (isCompleted)
					{
						return;
					}
				}
			}
		}
	}

	private boolean complete(Level world, BlockPos start)
	{
		// validate the structure
		for (int y = 0; y <= 9; y++)
		{
			for (int x = -1; x <= 1; x++)
			{
				for (int z = -1; z <= 1; z++)
				{
					if (y > 3 && (x != 0 || z != 0))
					{
						continue;
					}

					BlockPos newPos = start.add(x, y, z);

					BlockState state = world.getBlockState(newPos);
					Block block = state.getBlock();
					if (block != this || state.getValue(TRANSFORMED) != 0)
					{
						return false;
					}
				}
			}
		}

		// update the block states
		for (int y = 0; y <= 9; y++)
		{
			for (int x = -1; x <= 1; x++)
			{
				for (int z = -1; z <= 1; z++)
				{
					if (y > 3 && (x != 0 || z != 0))
					{
						continue;
					}

					BlockPos newPos = start.add(x, y, z);

					if (newPos.equals(start))
					{
						world.setBlockState(newPos, world.getBlockState(newPos).withProperty(TRANSFORMED, 2), 2);
					} else
					{
						world.setBlockState(newPos, world.getBlockState(newPos).withProperty(TRANSFORMED, 1), 2);

						TileEntityBeaconPost post = (TileEntityBeaconPost) world.getBlockEntity(newPos);
						post.setMasterLoc(start);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void addCollisionBoxToList(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos,
									  @Nonnull AABB entityBox, @Nonnull List<AABB> collidingBoxes,
									  @Nullable Entity entity, boolean isActualState)
	{
		state = state.getActualState(world, pos);

		if (state.getValue(TRANSFORMED) > 0)
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, getBoundingBox(state, world, pos));
			return;
		}

		addCollisionBoxToList(pos, entityBox, collidingBoxes, PILLAR_AABB);

		if (state.getValue(NORTH))
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
		}

		if (state.getValue(EAST))
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
		}

		if (state.getValue(SOUTH))
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
		}

		if (state.getValue(WEST))
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
		}
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos)
	{
		state = this.getActualState(state, source, pos);
		return BOUNDING_BOXES[getBoundingBoxIdx(state)];
	}

	/**
	 * Returns the correct index into boundingBoxes, based on what the fence is connected to.
	 */
	private static int getBoundingBoxIdx(BlockState state)
	{
		int i = 0;

		if (state.getValue(NORTH))
		{
			i |= 1 << Direction.NORTH.getHorizontalIndex();
		}

		if (state.getValue(EAST))
		{
			i |= 1 << Direction.EAST.getHorizontalIndex();
		}

		if (state.getValue(SOUTH))
		{
			i |= 1 << Direction.SOUTH.getHorizontalIndex();
		}

		if (state.getValue(WEST))
		{
			i |= 1 << Direction.WEST.getHorizontalIndex();
		}

		return i;
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
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

	public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
	{
		return false;
	}

	public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos)
	{
		BlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();
		return block != Blocks.BARRIER
			&& ((block instanceof BlockBeaconPost && block.getMaterial(iblockstate) == this.material)
			|| block instanceof BlockFenceGate
			|| ((block.getMaterial(iblockstate).isOpaque() && iblockstate.isFullCube()) && block.getMaterial(iblockstate) != Material.GOURD));
	}

	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("deprecation")
	@Override
	public boolean shouldSideBeRendered(BlockState blockState, @Nonnull IBlockAccess blockAccess,
										@Nonnull BlockPos pos, Direction side)
	{
		return true;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState blockState,
									Player entityPlayer, EnumHand hand,
									Direction facing, float hitX, float hitY, float hitZ)
	{
		return world.isClientSide()
			|| ItemLead.attachToFence(entityPlayer, world, pos);
	}

	public int getMetaFromState(BlockState blockState)
	{
		return blockState.getValue(TRANSFORMED);
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState getStateFromMeta(int metadata)
	{
		return this.getDefaultState().withProperty(TRANSFORMED, metadata);
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		return state.withProperty(NORTH, this.canConnectTo(worldIn, pos.north()))
			.withProperty(EAST, this.canConnectTo(worldIn, pos.east()))
			.withProperty(SOUTH, this.canConnectTo(worldIn, pos.south()))
			.withProperty(WEST, this.canConnectTo(worldIn, pos.west()));
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState withRotation(@Nonnull BlockState blockState, Rotation rotation)
	{
		switch (rotation)
		{
			case CLOCKWISE_180:
				return blockState.withProperty(NORTH, blockState.getValue(SOUTH)).withProperty(
					EAST,
					blockState.getValue(WEST)
				).withProperty(SOUTH, blockState.getValue(NORTH)).withProperty(WEST, blockState.getValue(EAST));
			case COUNTERCLOCKWISE_90:
				return blockState.withProperty(NORTH, blockState.getValue(EAST)).withProperty(
					EAST,
					blockState.getValue(SOUTH)
				).withProperty(SOUTH, blockState.getValue(WEST)).withProperty(WEST, blockState.getValue(NORTH));
			case CLOCKWISE_90:
				return blockState.withProperty(NORTH, blockState.getValue(WEST)).withProperty(
					EAST,
					blockState.getValue(NORTH)
				).withProperty(SOUTH, blockState.getValue(EAST)).withProperty(WEST, blockState.getValue(SOUTH));
			default:
				return blockState;
		}
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState withMirror(@Nonnull BlockState blockState, Mirror mirrorIn)
	{
		switch (mirrorIn)
		{
			case LEFT_RIGHT:
				return blockState.withProperty(NORTH, blockState.getValue(SOUTH)).withProperty(
					SOUTH,
					blockState.getValue(NORTH)
				);
			case FRONT_BACK:
				return blockState.withProperty(EAST, blockState.getValue(WEST)).withProperty(
					WEST,
					blockState.getValue(EAST)
				);
			default:
				return super.withMirror(blockState, mirrorIn);
		}
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, NORTH, EAST, WEST, SOUTH, TRANSFORMED);
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return state.getValue(TRANSFORMED) > 0 ? EnumBlockRenderType.INVISIBLE : EnumBlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		switch (metadata)
		{
			case 2:
				return new TileEntityBeaconPostMaster();
			case 1:
				return new TileEntityBeaconPost();
			default:
				return null;
		}
	}

	@Override
	public void breakBlock(Level world, @Nonnull BlockPos pos, @Nonnull BlockState blockState)
	{
		if (world != null
			&& blockState.getValue(TRANSFORMED) > 0)
		{
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (tileEntity instanceof TileEntityBeaconPost)
			{
				TileEntityBeaconPost tileEntityBeaconPost = (TileEntityBeaconPost) tileEntity;
				if (blockState.getValue(TRANSFORMED) == 2)
				{
					tileEntityBeaconPost.destruct();
				} else if (tileEntityBeaconPost.master != null
					&& !tileEntityBeaconPost.master.equals(pos)
					&& !tileEntityBeaconPost.destructing)
				{
					BlockEntity masterTe = world.getBlockEntity(tileEntityBeaconPost.master);

					if (masterTe instanceof TileEntityBeaconPost)
					{
						TileEntityBeaconPost post2 = (TileEntityBeaconPost) masterTe;

						if (post2 instanceof TileEntityBeaconPostMaster
							&& !post2.destructing)
						{
							post2.destruct();
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isLadder(BlockState state, IBlockAccess world, BlockPos pos, LivingEntity entityLivingBase)
	{
		return state.getValue(TRANSFORMED) > 0;
	}
}