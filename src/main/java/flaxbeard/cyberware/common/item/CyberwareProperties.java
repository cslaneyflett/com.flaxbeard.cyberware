package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.common.misc.NNLUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.util.random.Weight;
import net.minecraft.world.item.ItemStack;

public record CyberwareProperties(Rarity rarity, int essenceCost, ItemStack... components)
{
	public NonNullList<ItemStack> componentList()
	{
		return NNLUtil.fromArray(this.components);
	}

	public enum Rarity
	{
		NEVER(0),
		RARE(10),
		UNCOMMON(25),
		COMMON(50),
		VERY_COMMON(100);
		public final Weight weight;

		Rarity(int i)
		{
			this.weight = Weight.of(i);
		}
	}
}
