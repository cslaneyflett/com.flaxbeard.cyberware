package flaxbeard.cyberware.common.misc;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.item.ItemBlueprint;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlueprintCraftingHandler implements CraftingRecipe
{
	static
	{
		RecipeSorter.register(Cyberware.MODID + ":blueprintCrafting", BlueprintCraftingHandler.class,
			RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless"
		);
	}

	private ResourceLocation resourceLocation;
	private ItemStack itemStackCyberware;

	public BlueprintCraftingHandler()
	{
		// no operation
	}

	@Nonnull
	@Override
	public IRecipe setRegistryName(@Nonnull final ResourceLocation resourceLocation)
	{
		assert this.resourceLocation == null;
		this.resourceLocation = resourceLocation;
		return this;
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName()
	{
		return resourceLocation;
	}

	@Override
	public boolean canFit(final int width, final int height)
	{
		return width * height >= 2;
	}

	@Nonnull
	@Override
	public Class<IRecipe> getRegistryType()
	{
		return IRecipe.class;
	}

	@Override
	public boolean matches(@Nonnull final InventoryCrafting inventoryCrafting, @Nonnull final Level world)
	{
		return matches(inventoryCrafting);
	}

	private boolean matches(@Nonnull final InventoryCrafting inventoryCrafting)
	{
		boolean hasBlankBlueprint = false;
		itemStackCyberware = ItemStack.EMPTY;
		for (int indexSlot = 0; indexSlot < inventoryCrafting.getSizeInventory(); indexSlot++)
		{
			ItemStack itemStackSlot = inventoryCrafting.getStackInSlot(indexSlot);
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
				} else if (itemStackSlot.getItem() == CyberwareContent.blueprint
					&& (itemStackSlot.getTag() == null
					|| !itemStackSlot.getTag().hasKey("blueprintItem")))
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
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inventoryCrafting)
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
	public ItemStack getRecipeOutput()
	{
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inventoryCrafting)
	{
		if (!matches(inventoryCrafting))
		{
			return NonNullList.create();
		}
		final NonNullList<ItemStack> itemStackResults = NonNullList.withSize(
			inventoryCrafting.getSizeInventory(),
			ItemStack.EMPTY
		);
		for (int indexSlot = 0; indexSlot < itemStackResults.size(); indexSlot++)
		{
			if (itemStackCyberware == inventoryCrafting.getStackInSlot(indexSlot))
			{
				// note: we do need a copy here since caller decreases count on existing instance right after
				itemStackResults.set(indexSlot, itemStackCyberware.copy());
				break;
			}
		}
		return itemStackResults;
	}
}