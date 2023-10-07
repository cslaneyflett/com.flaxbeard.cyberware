package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.CyberwareContent;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class ItemArmorCyberware extends ArmorItem implements IDeconstructable
{
	public ItemArmorCyberware(ArmorMaterial pMaterial, EquipmentSlot pSlot, Properties pProperties)
	{
		super(pMaterial, pSlot, pProperties);
	}

	@Override
	public boolean canDestroy(ItemStack stack)
	{
		return true;
	}

	@Override
	public NonNullList<ItemStack> getComponents(ItemStack stack)
	{
		Item item = stack.getItem();

		NonNullList<ItemStack> nnl = NonNullList.create();
		// TODO: damage states
		if (item == CyberwareContent.trenchCoat)
		{
			nnl.add(new ItemStack(CyberwareContent.component, 2, 2));
			nnl.add(new ItemStack(Items.LEATHER, 12, 0));
			nnl.add(new ItemStack(Items.DYE, 1, 0));
		} else if (item == CyberwareContent.jacket)
		{
			nnl.add(new ItemStack(CyberwareContent.component, 1, 2));
			nnl.add(new ItemStack(Items.LEATHER, 8, 0));
			nnl.add(new ItemStack(Items.DYE, 1, 0));
		} else
		{
			nnl.add(new ItemStack(Blocks.STAINED_GLASS, 4, 15));
			nnl.add(new ItemStack(CyberwareContent.component, 1, 4));
		}
		return nnl;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ModelBiped getArmorModel(LivingEntity entityLivingBase, ItemStack itemStack, EntityEquipmentSlot armorSlot,
									ModelBiped _default)
	{
		if (!itemStack.isEmpty()
			&& itemStack.getItem() == CyberwareContent.trenchCoat)
		{
			ClientUtils.modelTrenchCoat.setDefaultModel(_default);
			return ClientUtils.modelTrenchCoat;
		}

		return null;
	}

	@Override
	public boolean hasColor(@Nonnull ItemStack stack)
	{
		if (getMaterial() != CyberwareContent.trenchMat)
		{
			return false;
		}

		CompoundTag tagCompound = stack.getTag();
		return tagCompound != null
			&& tagCompound.contains("display", 10)
			&& tagCompound.getCompound("display").contains("color", 3);
	}

	@Override
	public int getColor(@Nonnull ItemStack stack)
	{
		if (getMaterial() != CyberwareContent.trenchMat)
		{
			return 16777215;
		} else
		{
			CompoundTag tagCompound = stack.getTag();

			if (tagCompound != null)
			{
				CompoundTag tagCompoundDisplay = tagCompound.getCompound("display");

				if (tagCompoundDisplay.contains("color", 3))
				{
					return tagCompoundDisplay.getInt("color");
				}
			}

			return 0x333333; // 0x664028
		}
	}

	@Override
	public void removeColor(@Nonnull ItemStack stack)
	{
		if (getMaterial() == CyberwareContent.trenchMat)
		{
			CompoundTag tagCompound = stack.getTag();

			if (tagCompound != null)
			{
				CompoundTag tagCompoundDisplay = tagCompound.getCompound("display");

				if (tagCompoundDisplay.contains("color"))
				{
					tagCompoundDisplay.remove("color");
				}
			}
		}
	}

	public void setColor(ItemStack stack, int color)
	{
		if (getMaterial() != CyberwareContent.trenchMat)
		{
			throw new UnsupportedOperationException("Can't dye non-leather!");
		} else
		{
			CompoundTag tagCompound = stack.getTag();

			if (tagCompound == null)
			{
				tagCompound = new CompoundTag();
				stack.setTag(tagCompound);
			}

			CompoundTag tagCompoundDisplay = tagCompound.getCompound("display");

			if (!tagCompound.contains("display", 10))
			{
				tagCompound.put("display", tagCompoundDisplay);
			}

			tagCompoundDisplay.putInt("color", color);
		}
	}

	@Override
	public void getSubItems(@Nonnull CreativeModeTab tab, @Nonnull NonNullList<ItemStack> list)
	{
		if (getCreativeTabs().contains(tab))
		{
			if (getMaterial() == CyberwareContent.trenchMat)
			{
				super.getSubItems(tab, list);
				ItemStack brown = new ItemStack(this);
				setColor(brown, 0x664028);
				list.add(brown);
				ItemStack white = new ItemStack(this);
				setColor(white, 0xEAEAEA);
				list.add(white);
			} else
			{
				super.getSubItems(tab, list);
			}
		}
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(@Nonnull EntityPlayer entityPlayer, @Nonnull World world,
									  @Nonnull BlockPos blockPos,
									  @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY,
									  float hitZ)
	{
		final BlockState blockState = world.getBlockState(blockPos);
		final ItemStack itemStack = entityPlayer.getHeldItem(hand);
		if (!world.isClientSide()
			&& blockState.getBlock() instanceof BlockCauldron
			&& hasColor(itemStack))
		{
			final int waterLevel = blockState.getValue(BlockCauldron.LEVEL);
			if (waterLevel > 0)
			{
				removeColor(itemStack);
				((BlockCauldron) blockState.getBlock()).setWaterLevel(world, blockPos, blockState, waterLevel - 1);
				entityPlayer.addStat(StatList.ARMOR_CLEANED);
				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUse(entityPlayer, world, blockPos, hand, facing, hitX, hitY, hitZ);
	}
}