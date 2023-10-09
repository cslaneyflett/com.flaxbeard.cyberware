package flaxbeard.cyberware.common.item.base;

import flaxbeard.cyberware.common.misc.NNLUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.util.random.Weight;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CyberwareProperties
{
	private final Rarity rarity;
	private final int essenceCost;
	private final int maxStack;
	private final ItemStack[] components;
	private final Map<TooltipType, String> tooltips;

	public CyberwareProperties(Rarity rarity, int essenceCost, int maxStack, ItemStack... components)
	{
		this.rarity = rarity;
		this.essenceCost = essenceCost;
		this.maxStack = maxStack;
		this.components = components;
		this.tooltips = new HashMap<>();

		// TODO: rarity, zombie handler
		// maybe its not gonna go here, but has to go somewhere

		// ItemStack stack = new ItemStack(this, 1, CyberwareItemMetadata.of(meta));
		// int installedStackSize = installedStackSize(stack);
		// stack.setCount(installedStackSize);
		// this.setQuality(stack, CyberwareAPI.QUALITY_SCAVENGED);
		// CyberwareContent.zombieItems.add(new ZombieItem(weight.get(meta), stack));
	}

	public NonNullList<ItemStack> componentList()
	{
		return NNLUtil.fromArray(this.components);
	}

	public Rarity rarity() {return rarity;}

	public int essenceCost() {return essenceCost;}

	public int maxStack() {return maxStack;}

	public ItemStack[] components() {return components;}

	public Map<TooltipType, String> tooltips() {return tooltips;}

	public String tooltip(TooltipType t) {return tooltips.get(t);}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (CyberwareProperties) obj;
		return Objects.equals(this.rarity, that.rarity) &&
			this.essenceCost == that.essenceCost &&
			this.maxStack == that.maxStack &&
			Arrays.equals(this.components, that.components);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(rarity, essenceCost, maxStack, Arrays.hashCode(components));
	}

	@Override
	public String toString()
	{
		return "CyberwareProperties[" +
			"rarity=" + rarity + ", " +
			"essenceCost=" + essenceCost + ", " +
			"maxStack=" + maxStack + ", " +
			"components=" + Arrays.toString(components) + ", " +
			"tooltips=" + tooltips + ']';
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

	public enum TooltipType
	{
		MAIN,
		POWER_PRODUCTION,
		POWER_CONSUMPTION,
		POWER_CAPACITY;
	}
}
