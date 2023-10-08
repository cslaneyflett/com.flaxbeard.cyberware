package flaxbeard.cyberware.common.block.item;

import net.minecraft.world.item.Item;

public class ItemSurgeryTable extends Item
{
	public ItemSurgeryTable()
	{
		super(new Properties());

		//		String name = "surgery_table";

		//		setRegistryName(name);
		//		// ForgeRegistries.ITEMS.register(this);
		//		setTranslationKey(Cyberware.MODID + "." + name);
		//		setMaxDamage(0);
		//
		//		setCreativeTab(Cyberware.creativeTab);
	}

	/*
	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer entityPlayer, World worldIn, BlockPos pos,
	EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (worldIn.isClientSide())
		{
			return EnumActionResult.SUCCESS;
		}
		else if (facing != EnumFacing.UP)
		{
			return EnumActionResult.FAIL;
		}
		else
		{
			BlockState iblockstate = worldIn.getBlockState(pos);
			Block block = iblockstate.getBlock();
			boolean flag = block.isReplaceable(worldIn, pos);

			if (!flag)
			{
				pos = pos.up();
			}

			int i = MathHelper.floor_double((entityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			EnumFacing enumfacing = EnumFacing.byHorizontalIndex(i);
			BlockPos blockpos = pos.offset(enumfacing);

			if (entityPlayer.canPlayerEdit(pos, facing, stack) && entityPlayer.canPlayerEdit(blockpos, facing, stack))
			{
				boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
				boolean flag2 = flag || worldIn.isAirBlock(pos);
				boolean flag3 = flag1 || worldIn.isAirBlock(blockpos);

				if (flag2 && flag3 && worldIn.getBlockState(pos.down()).isFullyOpaque() && worldIn.getBlockState
				(blockpos.down()).isFullyOpaque())
				{
					BlockState iblockstate1 = CyberwareContent.surgeryTable.defaultBlockState().setValue
					(BlockSurgeryTable.OCCUPIED, Boolean.valueOf(false)).setValue(BlockSurgeryTable.FACING,
					enumfacing).setValue(BlockSurgeryTable.PART, BlockBed.EnumPartType.FOOT);

					if (worldIn.setBlockState(pos, iblockstate1, 11))
					{
						BlockState iblockstate2 = iblockstate1.setValue(BlockSurgeryTable.PART, BlockBed
						.EnumPartType.HEAD);
						worldIn.setBlockState(blockpos, iblockstate2, 11);
					}

					SoundType soundtype = iblockstate1.getBlock().getSoundType();
					worldIn.playSound((EntityPlayer)null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
					(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					--stack.stackSize;
					return EnumActionResult.SUCCESS;
				}
				else
				{
					return EnumActionResult.FAIL;
				}
			}
			else
			{
				return EnumActionResult.FAIL;
			}
		}
	}*/
}