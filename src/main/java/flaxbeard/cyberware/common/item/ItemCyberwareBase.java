package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemCyberwareBase extends Item
{
	public String[] subnames;
	private final ItemStack[] itemStackCache;

	public ItemCyberwareBase(String name, String... subnames)
	{
		super(new Item.Properties());

		//		setCreativeTab(Cyberware.creativeTab);

		this.subnames = subnames;
		itemStackCache = new ItemStack[Math.max(subnames.length, 1)];

		CyberwareContent.items.add(this);
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack itemstack)
	{
		int damage = itemstack.getItemDamage();
		if (damage >= subnames.length)
		{
			return super.getTranslationKey();
		}
		return super.getTranslationKey(itemstack) + "." + subnames[damage];
	}

	public void getSubItems(@Nonnull CreativeModeTab tab, @Nonnull NonNullList<ItemStack> list)
	{
		if (this.getCreativeTabs().contains(tab))
		{
			if (subnames.length == 0)
			{
				list.add(new ItemStack(this));
			}
			for (int metadata = 0; metadata < subnames.length; metadata++)
			{
				list.add(new ItemStack(this, 1, CyberwareItemMetadata.of(metadata)));
			}
		}
	}

	public ItemStack getCachedStack(int flag)
	{
		ItemStack itemStack = itemStackCache[flag];
		if (itemStack != null
			&& (itemStack.getItem() != this
			|| itemStack.getCount() != 1
			|| !CyberwareItemMetadata.matches(itemStack, flag))
		)
		{
			Cyberware.logger.error(String.format("Corrupted item stack cache: found %s as %s:%d, expected %s:%d",
				itemStack, itemStack.getItem(), CyberwareItemMetadata.get(itemStack),
				this, flag
			));
			itemStack = null;
		}
		if (itemStack == null)
		{
			itemStack = new ItemStack(this, 1, CyberwareItemMetadata.of(flag));
			itemStackCache[flag] = itemStack;
		}
		return itemStack;
	}
}
