package flaxbeard.cyberware.common.misc;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.Quality;
import flaxbeard.cyberware.api.item.ICyberwareTabItem;
import flaxbeard.cyberware.api.item.ICyberwareTabItem.EnumCategory;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.handler.CreativeMenuHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TabCyberware extends CreativeModeTab
{
	public TabCyberware(String label)
	{
		super(label);
	}

	@Nonnull
	@Override
	public ItemStack makeIcon()
	{
		return new ItemStack(CyberwareContent.cybereyes);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list)
	{
		Map<EnumCategory, List<ItemStack>> subLists = new EnumMap<>(EnumCategory.class);
		for (EnumCategory category : EnumCategory.values())
		{
			subLists.put(category, new ArrayList<>());
		}
		NonNullList<ItemStack> unsorted = NonNullList.create();

		Quality q = CreativeMenuHandler.pageSelected == 0 ? CyberwareAPI.QUALITY_SCAVENGED :
			CyberwareAPI.QUALITY_MANUFACTURED;

		for (Item item : ForgeRegistries.ITEMS)
		{
			if (item == null)
			{
				continue;
			}
			for (CreativeModeTab tab : item.getCreativeTabs())
			{
				if (tab == this)
				{
					if (item instanceof ICyberwareTabItem)
					{
						NonNullList<ItemStack> tempList = NonNullList.create();
						item.getSubItems(this, tempList);

						for (ItemStack stack : tempList)
						{
							if (!stack.isEmpty())
							{
								if (CyberwareAPI.isCyberware(stack))
								{
									ICyberware ware = CyberwareAPI.getCyberware(stack);
									if (ware.canHoldQuality(stack, q))
									{
										stack = ware.setQuality(stack, q);
									}
								}
								EnumCategory cat = ((ICyberwareTabItem) stack.getItem()).getCategory(stack);
								subLists.get(cat).add(stack);
							}
						}
					} else
					{
						item.getSubItems(this, unsorted);
					}
				}
			}
		}

		for (EnumCategory category : EnumCategory.values())
		{
			List<ItemStack> toAdd = subLists.get(category);
			list.addAll(toAdd);
		}

		list.addAll(unsorted);
/*
		if (this.getRelevantEnchantmentTypes() != null)
		{
			//this.addEnchantmentBooksToList(list, this.getRelevantEnchantmentTypes());
			//this.ad(list, this.getRelevantEnchantmentTypes());
		}
		
 */
	}
}
