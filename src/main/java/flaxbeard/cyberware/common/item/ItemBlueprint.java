package flaxbeard.cyberware.common.item;

import com.mojang.blaze3d.platform.InputConstants;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.IBlueprint;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.registry.items.Misc;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBlueprint extends Item implements IBlueprint
{
	public ItemBlueprint(Properties pProperties)
	{
		super(pProperties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		CompoundTag tagCompound = stack.getTag();
		if (tagCompound != null
			&& tagCompound.contains("blueprintItem"))
		{
			var window = Minecraft.getInstance().getWindow().getWindow();

			if (InputConstants.isKeyDown(window, Minecraft.getInstance().options.keyShift.getKey().getValue()))
			{
				ItemStack blueprintItem = ItemStack.of(tagCompound.getCompound("blueprintItem"));
				if (!blueprintItem.isEmpty() && CyberwareAPI.canDeconstruct(blueprintItem))
				{
					NonNullList<ItemStack> items = NNLUtil.copyList(CyberwareAPI.getComponents(blueprintItem));
					tooltip.add(Component.literal(I18n.get(
						"cyberware.tooltip.blueprint",
						blueprintItem.getDisplayName()
					)));
					for (ItemStack item : items)
					{
						if (!item.isEmpty())
						{
							tooltip.add(Component.literal(item.getCount() + " x " + item.getDisplayName()));
						}
					}
					return;
				}
			} else
			{
				tooltip.add(Component.literal(ChatFormatting.DARK_GRAY + I18n.get("cyberware.tooltip.shift_prompt")));
				return;
			}
		}
		tooltip.add(Component.literal(ChatFormatting.DARK_GRAY + I18n.get("cyberware.tooltip.craft_blueprint")));
	}

	//	@Override
	//	public void getSubItems(@Nonnull CreativeModeTab tab, @Nonnull NonNullList<ItemStack> list)
	//	{
	//		if (this.getCreativeTabs().contains(tab))
	//		{
	//			list.add(new ItemStack(this, 1, 1));
	//		}
	//	}

	public static ItemStack getBlueprintForItem(ItemStack stack)
	{
		if (!stack.isEmpty() && CyberwareAPI.canDeconstruct(stack))
		{
			ItemStack toBlue = stack.copy();


			toBlue.setCount(1);
			if (toBlue.isDamageableItem())
			{
				toBlue.setDamageValue(0);
			}
			toBlue.setTag(null);

			ItemStack ret = new ItemStack(Misc.BLUEPRINT.get());
			CompoundTag tagCompound = new CompoundTag();
			tagCompound.put("blueprintItem", toBlue.save(new CompoundTag()));

			ret.setTag(tagCompound);
			return ret;
		} else
		{
			return ItemStack.EMPTY;
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	@Override
	public Component getName(ItemStack pStack)
	{
		CompoundTag tagCompound = pStack.getTag();

		if (tagCompound != null &&
			tagCompound.contains("blueprintItem"))
		{
			ItemStack blueprintItem = ItemStack.of(tagCompound.getCompound("blueprintItem"));
			if (!blueprintItem.isEmpty())
			{
				return Component.translatable("item.cyberware.blueprint.not_blank.name", blueprintItem.getDisplayName());
			}
		}

		return super.getName(pStack);
	}

	@Override
	public ItemStack getResult(ItemStack stack, NonNullList<ItemStack> craftingItems)
	{
		CompoundTag tagCompound = stack.getTag();
		if (tagCompound != null
			&& tagCompound.contains("blueprintItem"))
		{
			ItemStack blueprintItem = ItemStack.of(tagCompound.getCompound("blueprintItem"));
			if (!blueprintItem.isEmpty() && CyberwareAPI.canDeconstruct(blueprintItem))
			{
				NonNullList<ItemStack> requiredItems = NNLUtil.copyList(CyberwareAPI.getComponents(blueprintItem));
				for (ItemStack requiredItem : requiredItems)
				{
					ItemStack required = requiredItem.copy();

					boolean satisfied = false;
					for (ItemStack crafting : craftingItems)
					{
						if (!crafting.isEmpty() && !required.isEmpty())
						{
							if (crafting.getItem() == required.getItem() &&
								CyberwareItemMetadata.identical(crafting, required) &&
								(!required.hasTag() || ItemStack.tagMatches(required, crafting)))
							{
								required.shrink(crafting.getCount());
							}

							if (required.getCount() <= 0)
							{
								satisfied = true;
								break;
							}
						}
					}

					if (!satisfied) return ItemStack.EMPTY;
				}

				return blueprintItem;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> consumeItems(ItemStack stack, NonNullList<ItemStack> craftingItems)
	{
		CompoundTag tagCompound = stack.getTag();
		if (tagCompound != null
			&& tagCompound.contains("blueprintItem"))
		{
			ItemStack blueprintItem = ItemStack.of(tagCompound.getCompound("blueprintItem"));
			if (!blueprintItem.isEmpty() && CyberwareAPI.canDeconstruct(blueprintItem))
			{
				NonNullList<ItemStack> requiredItems = NNLUtil.copyList(CyberwareAPI.getComponents(blueprintItem));
				NonNullList<ItemStack> newCrafting = NonNullList.create();
				newCrafting.addAll(craftingItems);
				for (ItemStack requiredItem : requiredItems)
				{
					ItemStack required = requiredItem.copy();
					for (int c = 0; c < newCrafting.size(); c++)
					{
						ItemStack crafting = newCrafting.get(c);
						if (!crafting.isEmpty() && !required.isEmpty())
						{
							if (crafting.getItem() == required.getItem() &&
								CyberwareItemMetadata.identical(crafting, required) &&
								(!required.hasTag() || ItemStack.tagMatches(required, crafting)))
							{
								int toSubtract = Math.min(required.getCount(), crafting.getCount());
								required.shrink(toSubtract);
								crafting.shrink(toSubtract);
								if (crafting.getCount() <= 0)
								{
									crafting = ItemStack.EMPTY;
								}
								newCrafting.set(c, crafting);
							}
							if (required.getCount() <= 0)
							{
								break;
							}
						}
					}
				}

				return newCrafting;
			}
		}
		throw new IllegalStateException("Consuming items when items shouldn't be consumed!");
	}

	@Override
	public NonNullList<ItemStack> getRequirementsForDisplay(ItemStack stack)
	{
		CompoundTag tagCompound = stack.getTag();
		if (tagCompound != null
			&& tagCompound.contains("blueprintItem"))
		{
			ItemStack blueprintItem = ItemStack.of(tagCompound.getCompound("blueprintItem"));
			if (!blueprintItem.isEmpty() && CyberwareAPI.canDeconstruct(blueprintItem))
			{
				return CyberwareAPI.getComponents(blueprintItem);
			}
		}

		return NonNullList.create();
	}

	@Override
	public ItemStack getIconForDisplay(ItemStack stack)
	{
		CompoundTag tagCompound = stack.getTag();
		if (tagCompound != null
			&& tagCompound.contains("blueprintItem"))
		{
			return ItemStack.of(tagCompound.getCompound("blueprintItem"));
		}

		return ItemStack.EMPTY;
	}
}
