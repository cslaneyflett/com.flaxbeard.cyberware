package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemSurgeryChamber;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgeryChamber;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockSurgeryChamber extends Block implements EntityBlock
{
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty OPEN = BooleanProperty.create("open");
	public static final EnumProperty<EnumChamberHalf> HALF = EnumProperty.create("half", EnumChamberHalf.class);
	public final Item itemBlock;

	public BlockSurgeryChamber(Properties pProperties)
	{
		super(pProperties);
//		this.setDefaultState(this.blockState.getBaseState()
//			.setValue(FACING, Direction.NORTH)
//			.setValue(OPEN, Boolean.FALSE)
//			.setValue(HALF, EnumChamberHalf.LOWER));
//
//		itemBlock = new ItemSurgeryChamber(this, "cyberware.tooltip.surgery_chamber.0", "cyberware.tooltip" +
//			".surgery_chamber.1");
	}

	private static final AABB top = new AABB(0F, 15F / 16F, 0F, 1F, 1F, 1F);
	private static final AABB south = new AABB(0F, 0F, 0F, 1F, 1F, 1F / 16F);
	private static final AABB north = new AABB(0F, 0F, 15F / 16F, 1F, 1F, 1F);
	private static final AABB east = new AABB(0F, 0F, 0F, 1F / 16F, 1F, 1F);
	private static final AABB west = new AABB(15F / 16F, 0F, 0F, 1F, 1F, 1F);
	private static final AABB bottom = new AABB(0F, 0F, 0F, 1F, 1F / 16F, 1F);

	@Nonnull
	@Override
	public ItemStack getItem(Level worldIn, BlockPos pos, BlockState state)
	{
		return new ItemStack(itemBlock);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos,
									  @Nonnull AABB entityBox, @Nonnull List<AABB> collidingBoxes,
									  @Nullable Entity entity, boolean isActualState)
	{
		Direction face = state.getValue(FACING);
		boolean open = state.getValue(OPEN);

		if (state.getValue(HALF) == EnumChamberHalf.UPPER)
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, top);
			if (!open || face != Direction.SOUTH)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, south);
			}
			if (!open || face != Direction.NORTH)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, north);
			}
			if (!open || face != Direction.EAST)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, east);
			}
			if (!open || face != Direction.WEST)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, west);
			}
		} else
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, bottom);
			if (!open || face != Direction.SOUTH)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, south);
			}
			if (!open || face != Direction.NORTH)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, north);
			}
			if (!open || face != Direction.EAST)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, east);
			}
			if (!open || face != Direction.WEST)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, west);
			}
		}
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState blockState,
									Player entityPlayer, EnumHand hand,
									Direction side, float hitX, float hitY, float hitZ)
	{
		boolean top = blockState.getValue(HALF) == EnumChamberHalf.UPPER;
		if (canOpen(top ? pos : pos.up(), world))
		{
			toggleDoor(top, blockState, pos, world);

			notifySurgeon(top ? pos : pos.up(), world);
		}

		return true;
	}

	public void toggleDoor(boolean top, BlockState blockState, BlockPos pos, Level worldIn)
	{
		BlockState blockStateNew = blockState.cycleProperty(OPEN);
		worldIn.setBlockState(pos, blockStateNew, 2);

		BlockPos otherPos = pos.up();
		if (top)
		{
			otherPos = pos.down();
		}
		BlockState otherState = worldIn.getBlockState(otherPos);

		if (otherState.getBlock() == this)
		{
			otherState = otherState.cycleProperty(OPEN);
			worldIn.setBlockState(otherPos, otherState, 2);
		}
	}

	private boolean canOpen(BlockPos pos, Level worldIn)
	{
		BlockEntity above = worldIn.getBlockEntity(pos.up());

		if (above instanceof TileEntitySurgery)
		{
			return ((TileEntitySurgery) above).canOpen();
		}
		return true;
	}

	private void notifySurgeon(BlockPos pos, Level worldIn)
	{
		BlockEntity above = worldIn.getBlockEntity(pos.up());

		if (above instanceof TileEntitySurgery)
		{
			((TileEntitySurgery) above).notifyChange();
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		if (blockState.getValue(HALF) == EnumChamberHalf.UPPER)
		{
			BlockPos blockpos = pos.down();
			BlockState iblockstate = world.getBlockState(blockpos);

			if (iblockstate.getBlock() != this)
			{
				world.setBlockToAir(pos);
			} else if (blockIn != this)
			{
				iblockstate.neighborChanged(world, blockpos, blockIn, fromPos);
			}
		} else
		{
			BlockPos blockpos1 = pos.up();
			BlockState iblockstate1 = world.getBlockState(blockpos1);

			if (iblockstate1.getBlock() != this)
			{
				world.setBlockToAir(pos);
				if (!world.isClientSide())
				{
					dropBlockAsItem(world, pos, blockState, 0);
				}
			}
		}
	}

	@Nonnull
	@Override
	public Item getItemDropped(BlockState state, RandomSource rand, int fortune)
	{
		return state.getValue(HALF) == EnumChamberHalf.UPPER ? Items.AIR : this.itemBlock;
	}

	@Override
	public boolean canPlaceBlockAt(Level worldIn, BlockPos pos)
	{
		return pos.getY() < worldIn.getHeight() - 1
			&& worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), Direction.UP)
			&& super.canPlaceBlockAt(worldIn, pos)
			&& super.canPlaceBlockAt(worldIn, pos.up());
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getPushReaction(BlockState state)
	{
		return EnumPushReaction.DESTROY;
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState getStateFromMeta(int metadata)
	{
		return this.defaultBlockState()
			.setValue(HALF, (metadata & 1) > 0 ? EnumChamberHalf.UPPER : EnumChamberHalf.LOWER)
			.setValue(OPEN, (metadata & 2) > 0)
			.setValue(FACING, Direction.byHorizontalIndex(metadata >> 2));
	}

	@Override
	public int getMetaFromState(BlockState blockState)
	{
		return (blockState.getValue(FACING).getHorizontalIndex() << 2)
			+ (blockState.getValue(HALF) == EnumChamberHalf.UPPER ? 1 : 0)
			+ (blockState.getValue(OPEN) ? 2 : 0);
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, HALF, FACING, OPEN);
	}

	@Override
	public void onBlockHarvested(Level worldIn, BlockPos pos, BlockState state, Player entityPlayer)
	{
		BlockPos blockpos = pos.down();
		BlockPos blockpos1 = pos.up();

		if (entityPlayer.capabilities.isCreativeMode && state.getValue(HALF) == EnumChamberHalf.UPPER && worldIn.getBlockState(blockpos).getBlock() == this)
		{
			worldIn.setBlockToAir(blockpos);
		}

		if (state.getValue(HALF) == EnumChamberHalf.LOWER && worldIn.getBlockState(blockpos1).getBlock() == this)
		{
			if (entityPlayer.capabilities.isCreativeMode)
			{
				worldIn.setBlockToAir(pos);
			}

			worldIn.setBlockToAir(blockpos1);
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

	public enum EnumChamberHalf implements StringRepresentable
	{
		UPPER,
		LOWER;

		public String toString()
		{
			return this.getName();
		}

		public String getName()
		{
			return this == UPPER ? "upper" : "lower";
		}
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		return (metadata & 1) > 0 ? new TileEntitySurgeryChamber() : null;
	}
}
