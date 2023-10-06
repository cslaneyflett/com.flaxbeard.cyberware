package flaxbeard.cyberware.common.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntPredicate;

// TODO: implement
public class CyberwareItemMetadata
{
	public static int WILDCARD_VALUE = Integer.MAX_VALUE;

	public static boolean identical(ItemStack a, ItemStack b)
	{
		return false; // a.REWRITE_damageTag() == b.REWRITE_damageTag();
	}

	public static boolean matchesOrWildcard(ItemStack check, ItemStack candidate)
	{
		return check.getItem() == candidate.getItem() && (isWildcard(check) || identical(check, candidate));
	}

	public static boolean predicate(ItemStack stack, IntPredicate pred)
	{
		return pred.test(-1);
	}

	public static boolean matches(ItemStack item, int metaFlag)
	{
		return false; // CyberwareItemMetadata.get(stack) == META_CORTICAL_STACK
	}

	public static int get(ItemStack item)
	{
		return -1;
	}

	public static CompoundTag of(int metaFlag)
	{
		return new CompoundTag();
	}

	public static CompoundTag wildcard()
	{
		return new CompoundTag();
	}

	public static boolean isWildcard(ItemStack item)
	{
		return matches(item, WILDCARD_VALUE);
	}

	public static CompoundTag copy(ItemStack stack)
	{
		return new CompoundTag();
	}
}
