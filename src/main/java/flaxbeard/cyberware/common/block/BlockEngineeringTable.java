package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemEngineeringTable;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable.TileEntityEngineeringDummy;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockEngineeringTable extends Block implements EntityBlock
{
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<EnumEngineeringHalf> HALF = EnumProperty.create(
		"half",
		EnumEngineeringHalf.class
	);
	public final Item itemBlock;

	private static final AABB s = new AABB(4F / 16F, 0F, 0F / 16F, 12F / 16F, 1F, 12F / 16F);
	private static final AABB n = new AABB(4F / 16F, 0F, 4F / 16F, 12F / 16F, 1F, 16F / 16F);
	private static final AABB e = new AABB(0F / 16F, 0F, 4F / 16F, 12F / 16F, 1F, 12F / 16F);
	private static final AABB w = new AABB(4F / 16F, 0F, 4F / 16F, 16F / 16F, 1F, 12F / 16F);

	public BlockEngineeringTable(Properties pProperties)
	{
		super(pProperties);
//		this.setDefaultState(this.blockState.getBaseState().setValue(FACING, Direction.NORTH).setValue(
//			HALF,
//			EnumEngineeringHalf.LOWER
//		));
//		itemBlock = new ItemEngineeringTable(this, "cyberware.tooltip.engineering_table");
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AABB getBoundingBox(BlockState state, LevelReader source, BlockPos pos)
	{
		if (state.getValue(HALF) == EnumEngineeringHalf.UPPER)
		{
			Direction face = state.getValue(FACING);
			switch (face)
			{
				case NORTH:
					return s;
				case SOUTH:
					return n;
				case EAST:
					return w;
				case WEST:
					return e;
			}
		}

		return super.getBoundingBox(state, source, pos);
	}

	@Nonnull
	@Override
	public ItemStack getItem(Level worldIn, BlockPos pos, BlockState state)
	{
		return new ItemStack(itemBlock);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean moving)
	{
		if (state.getValue(HALF) == EnumEngineeringHalf.UPPER)
		{
			BlockPos blockpos = pos.down();
			BlockState iblockstate = worldIn.getBlockState(blockpos);

			if (iblockstate.getBlock() != this)
			{
				worldIn.setBlockToAir(pos);
			} else if (blockIn != this)
			{
				iblockstate.neighborChanged(worldIn, blockpos, blockIn, fromPos, moving);
			}
		} else
		{
			BlockPos blockpos1 = pos.up();
			BlockState iblockstate1 = worldIn.getBlockState(blockpos1);

			if (iblockstate1.getBlock() != this)
			{
				worldIn.setBlockToAir(pos);
				if (!worldIn.isClientSide())
				{
					this.dropBlockAsItem(worldIn, pos, state, 0);
				}
			}
		}
	}

	@Override
	public void onBlockHarvested(Level worldIn, BlockPos pos, BlockState state, Player entityPlayer)
	{
		BlockPos blockpos = pos.relative(Direction.DOWN);
		BlockPos blockpos1 = pos.relative(Direction.UP);

		if (entityPlayer.isCreative() && state.getValue(HALF) == EnumEngineeringHalf.UPPER && worldIn.getBlockState(blockpos).getBlock() == this)
		{
			worldIn.setBlockToAir(blockpos);
		}

		if (state.getValue(HALF) == EnumEngineeringHalf.LOWER && worldIn.getBlockState(blockpos1).getBlock() == this)
		{
			if (entityPlayer.isCreative())
			{
				worldIn.setBlockToAir(pos);
			}

			worldIn.setBlockToAir(blockpos1);
		}
	}

	@Override
	public void onBlockPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (stack.hasDisplayName())
		{
			BlockEntity tileentity = worldIn.getBlockEntity(pos);

			if (tileentity instanceof TileEntityEngineeringTable)
			{
				((TileEntityEngineeringTable) tileentity).setCustomInventoryName(stack.getDisplayName());
			}
		}
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		return (metadata & 1) > 0 ? new TileEntityEngineeringTable() : new TileEntityEngineeringDummy();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState blockState,
									Player entityPlayer, EnumHand hand,
									Direction side, float hitX, float hitY, float hitZ)
	{
		boolean top = blockState.getValue(HALF) == EnumEngineeringHalf.UPPER;
		BlockPos checkPos = top ? pos : pos.add(0, 1, 0);
		BlockEntity tileentity = world.getBlockEntity(checkPos);

		if (tileentity instanceof TileEntityEngineeringTable)
		{
			entityPlayer.openGui(Cyberware.INSTANCE, 2, world, checkPos.getX(), checkPos.getY(), checkPos.getZ());
		}

		return true;
	}

	@Override
	public void breakBlock(Level world, @Nonnull BlockPos pos, @Nonnull BlockState blockState)
	{
		boolean top = blockState.getValue(HALF) == EnumEngineeringHalf.UPPER;
		if (top)
		{
			BlockEntity tileentity = world.getBlockEntity(pos);

			if (tileentity instanceof TileEntityEngineeringTable engineering
				&& !world.isClientSide())
			{
				for (int indexSlot = 0; indexSlot < engineering.slots.getSlots(); indexSlot++)
				{
					ItemStack stack = engineering.slots.getStackInSlot(indexSlot);
					if (!stack.isEmpty())
					{
						InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
					}
				}
			}

			super.breakBlock(world, pos, blockState);
		}
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState getStateFromMeta(int metadata)
	{
		return this.defaultBlockState()
			.setValue(HALF, (metadata & 1) > 0 ? EnumEngineeringHalf.UPPER : EnumEngineeringHalf.LOWER)
			.setValue(FACING, Direction.byHorizontalIndex(metadata >> 1));
	}

	@Override
	public int getMetaFromState(BlockState blockState)
	{
		return (blockState.getValue(FACING).getHorizontalIndex() << 1)
			+ (blockState.getValue(HALF) == EnumEngineeringHalf.UPPER ? 1 : 0);
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, HALF, FACING);
	}

	@Override
	public boolean canPlaceBlockAt(Level worldIn, BlockPos pos)
	{
		return pos.getY() < worldIn.getHeight() - 1
			&& super.canPlaceBlockAt(worldIn, pos)
			&& super.canPlaceBlockAt(worldIn, pos.up());
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getPushReaction(BlockState blockState)
	{
		return EnumPushReaction.DESTROY;
	}

	@Nonnull
	@Override
	public Item getItemDropped(BlockState blockState, RandomSource rand, int fortune)
	{
		return blockState.getValue(HALF) == EnumEngineeringHalf.UPPER ? Items.AIR : this.itemBlock;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(BlockState blockState)
	{
		return blockState.getValue(HALF) == EnumEngineeringHalf.LOWER;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(BlockState blockState)
	{
		return blockState.getValue(HALF) == EnumEngineeringHalf.LOWER;
	}

	public enum EnumEngineeringHalf implements StringRepresentable
	{
		UPPER,
		LOWER;

		public String toString()
		{
			return this.getName();
		}

		@Override
		@Nonnull
		public String getSerializedName()
		{
			return this == UPPER ? "upper" : "lower";
		}
	}
}
