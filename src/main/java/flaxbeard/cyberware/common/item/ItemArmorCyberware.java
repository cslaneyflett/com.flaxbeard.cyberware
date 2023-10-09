package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.registry.items.ArmorMaterials;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ItemArmorCyberware extends ArmorItem implements IDeconstructable, DyeableLeatherItem
{
	private final NonNullList<ItemStack> components;

	public ItemArmorCyberware(ArmorMaterial pMaterial, EquipmentSlot pSlot, Properties pProperties, ItemStack... components)
	{
		super(pMaterial, pSlot, pProperties);
		this.components = NNLUtil.fromArray(components);
	}

	@Override
	public boolean canDestroy(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getComponents(@Nonnull ItemStack stack)
	{
		return this.components;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type)
	{
		if (!stack.isEmpty() && getMaterial() != ArmorMaterials.TRENCH_COAT)
		{
			// TODO: map to modelTrenchCoat refactor

			// ClientUtils.modelTrenchCoat.setDefaultModel(_default);
			// return ClientUtils.modelTrenchCoat;
		}

		return super.getArmorTexture(stack, entity, slot, type);
	}

	@Override
	public boolean hasCustomColor(@Nonnull ItemStack stack)
	{
		if (getMaterial() != ArmorMaterials.TRENCH_COAT)
		{
			return false;
		}

		return DyeableLeatherItem.super.hasCustomColor(stack);
	}

	@Override
	public int getColor(@Nonnull ItemStack stack)
	{
		if (getMaterial() != ArmorMaterials.TRENCH_COAT)
		{
			return 16777215;
		} else
		{
			return DyeableLeatherItem.super.getColor(stack);
		}
	}

	@Override
	public void clearColor(@Nonnull ItemStack stack)
	{
		if (getMaterial() == ArmorMaterials.TRENCH_COAT)
		{
			DyeableLeatherItem.super.clearColor(stack);
		}
	}

	public void setColor(@Nonnull ItemStack stack, int color)
	{
		if (getMaterial() != ArmorMaterials.TRENCH_COAT)
		{
			throw new UnsupportedOperationException("Can't dye non-leather!");
		} else
		{
			DyeableLeatherItem.super.setColor(stack, color);
		}
	}
}