package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemComponentBox;
import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import java.util.Random;

public class BlockComponentBox extends Block
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public ItemBlock itemBlock;

	public BlockComponentBox()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);

		String name = "component_box";

		setRegistryName(name);
		// ForgeRegistries.BLOCKS.register(this);

		itemBlock = new ItemComponentBox(this);
		itemBlock.setRegistryName(name);
		// ForgeRegistries.ITEMS.register(itemBlock);

		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntityComponentBox.class, new ResourceLocation(Cyberware.MODID, name));

		CyberwareContent.items.add(itemBlock);

		setDefaultState(blockState.getBaseState().withProperty(FACING, Direction.NORTH));
	}

	private static final AABB ns = new AABB(4F / 16F, 0F, 1F / 16F, 12F / 16F, 10F / 16F, 15F / 16F);
	private static final AABB ew = new AABB(1F / 16F, 0F, 4F / 16F, 15F / 16F, 10F / 16F, 12F / 16F);

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos)
	{
		EnumFacing face = state.getValue(FACING);
		if (face == EnumFacing.NORTH || face == EnumFacing.SOUTH)
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

	@Nonnull
	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
										   float hitZ, int meta, LivingEntity placer)
	{
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		return new TileEntityComponentBox();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void onBlockPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		if (stack.hasDisplayName())
		{
			BlockEntity tileentity = worldIn.getBlockEntity(pos);

			if (tileentity instanceof TileEntityComponentBox)
			{
				((TileEntityComponentBox) tileentity).setCustomInventoryName(stack.getDisplayName());
			}
		}
		if (stack.hasTag())
		{
			assert stack.getTag() != null;
			if (stack.getTag().contains("contents"))
			{
				BlockEntity tileentity = worldIn.getBlockEntity(pos);

				if (tileentity instanceof TileEntityComponentBox)
				{
					((TileEntityComponentBox) tileentity).slots.deserializeNBT(stack.getTag().getCompound("contents"));
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockState getStateFromMeta(int metadata)
	{
		EnumFacing enumfacing = EnumFacing.byIndex(metadata);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y)
		{
			enumfacing = EnumFacing.NORTH;
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
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState blockState,
									Player entityPlayer, EnumHand hand,
									EnumFacing side, float hitX, float hitY, float hitZ)
	{
		BlockEntity tileentity = world.getBlockEntity(pos);

		if (tileentity instanceof TileEntityComponentBox)
		{
			if (entityPlayer.isShiftKeyDown())
			{
				TileEntityComponentBox box = (TileEntityComponentBox) tileentity;
				ItemStack toDrop = this.getStack(box);

				if (entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem).isEmpty())
				{
					entityPlayer.inventory.mainInventory.set(entityPlayer.inventory.currentItem, toDrop);
				} else
				{
					if (!entityPlayer.inventory.addItemStackToInventory(toDrop))
					{
						InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), toDrop);
					}
				}
				box.doDrop = false;
				world.setBlockToAir(pos);
			} else
			{
				entityPlayer.openGui(Cyberware.INSTANCE, 5, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}

		return true;
	}

	private ItemStack getStack(TileEntityComponentBox box)
	{
		ItemStack stackToDrop = new ItemStack(itemBlock);

		CompoundTag tagCompound = new CompoundTag();
		tagCompound.put("contents", box.slots.serializeNBT());
		stackToDrop.setTag(tagCompound);

		if (box.hasCustomName())
		{
			stackToDrop = stackToDrop.setStackDisplayName(box.getName());
		}
		return stackToDrop;
	}

	@Override
	public void breakBlock(Level world, @Nonnull BlockPos pos, @Nonnull BlockState blockState)
	{
		BlockEntity tileentity = world.getBlockEntity(pos);

		if (tileentity instanceof TileEntityComponentBox
			&& !world.isClientSide())
		{
			TileEntityComponentBox box = (TileEntityComponentBox) tileentity;
			if (box.doDrop)
			{
				ItemStack stackToDrop = getStack(box);
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stackToDrop);
			}
		}

		super.breakBlock(world, pos, blockState);
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}
}
