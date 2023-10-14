package flaxbeard.cyberware.common.misc;

import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.common.item.ItemBlueprint;
import flaxbeard.cyberware.common.registry.items.Misc;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class BlueprintCraftingHandler extends CustomRecipe
{
	private ItemStack itemStackCyberware;

	public BlueprintCraftingHandler(ResourceLocation pId)
	{
		super(pId);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width * height >= 2;
	}

	@Override
	public boolean matches(@Nonnull final CraftingContainer inventoryCrafting, @Nonnull final Level world)
	{
		return matches(inventoryCrafting);
	}

	private boolean matches(@Nonnull final CraftingContainer inventoryCrafting)
	{
		boolean hasBlankBlueprint = false;
		itemStackCyberware = ItemStack.EMPTY;
		for (int indexSlot = 0; indexSlot < inventoryCrafting.getContainerSize(); indexSlot++)
		{
			ItemStack itemStackSlot = inventoryCrafting.getItem(indexSlot);
			if (!itemStackSlot.isEmpty())
			{
				if (itemStackSlot.getItem() instanceof IDeconstructable
					&& itemStackSlot.getCount() == 1)
				{
					if (itemStackCyberware.isEmpty())
					{
						itemStackCyberware = itemStackSlot;
					} else
					{
						return false;
					}
				} else if (itemStackSlot.getItem() == Misc.BLUEPRINT.get()
					&& (itemStackSlot.getTag() == null
					|| !itemStackSlot.getTag().contains("blueprintItem")))
				{
					if (!hasBlankBlueprint)
					{
						hasBlankBlueprint = true;
					} else
					{
						return false;
					}
				} else
				{
					return false;
				}
			}
		}
		return !itemStackCyberware.isEmpty() && hasBlankBlueprint;
	}

	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull CraftingContainer inventoryCrafting)
	{
		if (matches(inventoryCrafting))
		{
			return ItemBlueprint.getBlueprintForItem(itemStackCyberware);
		} else
		{
			return ItemStack.EMPTY;
		}
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingContainer inventoryCrafting)
	{
		if (!matches(inventoryCrafting))
		{
			return NonNullList.create();
		}

		final NonNullList<ItemStack> itemStackResults = NonNullList.withSize(
			inventoryCrafting.getContainerSize(),
			ItemStack.EMPTY
		);

		for (int indexSlot = 0; indexSlot < itemStackResults.size(); indexSlot++)
		{
			if (itemStackCyberware == inventoryCrafting.getItem(indexSlot))
			{
				// note: we do need a copy here since caller decreases count on existing instance right after
				itemStackResults.set(indexSlot, itemStackCyberware.copy());
				break;
			}
		}

		return itemStackResults;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializer.SHAPELESS_RECIPE;
	}
}