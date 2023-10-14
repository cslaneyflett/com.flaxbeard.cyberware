package flaxbeard.cyberware.common.misc;

import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ZombieItem implements WeightedEntry
{
	public static List<ZombieItem> entries = new ArrayList<>();

	public static void add(CyberwareProperties.Rarity rarity, ItemStack stack)
	{
		entries.add(new ZombieItem(rarity.weight, stack));
	}

	public ItemStack stack;
	private final Weight weight;

	public ZombieItem(Weight weight, ItemStack stack)
	{
		this.weight = weight;
		this.stack = stack;
	}

	public ItemStack getData()
	{
		return this.stack;
	}

	@Override
	public @Nonnull Weight getWeight()
	{
		return this.weight;
	}

	@Override
	public boolean equals(Object target)
	{
		if (!(target instanceof ZombieItem zombieItem)) return false;
		ItemStack stack2 = zombieItem.stack;

		return stack == stack2
			|| (!stack.isEmpty()
			&& !stack2.isEmpty()
			&& stack.getItem() == stack2.getItem()
			&& stack.getCount() == stack2.getCount());
	}
}