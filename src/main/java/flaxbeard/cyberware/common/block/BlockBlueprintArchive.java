package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityBlueprintArchive;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
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
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockBlueprintArchive extends Block
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockBlueprintArchive()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);

		String name = "blueprint_archive";

		setRegistryName(name);
		// ForgeRegistries.BLOCKS.register(this);

		ItemBlock itemBlock = new ItemBlockCyberware(this, "cyberware.tooltip.blueprint_archive.0", "cyberware.tooltip" +
			".blueprint_archive.1");
		itemBlock.setRegistryName(name);
		// ForgeRegistries.ITEMS.register(itemBlock);

		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntityBlueprintArchive.class, new ResourceLocation(Cyberware.MODID, name));

		CyberwareContent.blocks.add(this);

		setDefaultState(blockState.getBaseState().withProperty(FACING, Direction.NORTH));
	}

	@Nonnull
	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY,
										   float hitZ, int meta, LivingEntity placer)
	{
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		return new TileEntityBlueprintArchive();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState blockState, LivingEntity placer, ItemStack stack)
	{
		world.setBlockState(pos, blockState.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		if (stack.hasDisplayName())
		{
			BlockEntity tileentity = world.getBlockEntity(pos);

			if (tileentity instanceof TileEntityBlueprintArchive)
			{
				((TileEntityBlueprintArchive) tileentity).setCustomInventoryName(stack.getDisplayName());
			}
		}
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
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState blockState,
									Player entityPlayer, EnumHand hand,
									Direction side, float hitX, float hitY, float hitZ)
	{
		BlockEntity tileentity = world.getBlockEntity(pos);

		if (tileentity instanceof TileEntityBlueprintArchive)
		{
			entityPlayer.openGui(Cyberware.INSTANCE, 4, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

	@Override
	public void breakBlock(Level world, @Nonnull BlockPos pos, @Nonnull BlockState blockState)
	{
		BlockEntity tileentity = world.getBlockEntity(pos);

		if (tileentity instanceof TileEntityBlueprintArchive
			&& !world.isClientSide())
		{
			TileEntityBlueprintArchive scanner = (TileEntityBlueprintArchive) tileentity;

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
