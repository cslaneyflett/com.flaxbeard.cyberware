package flaxbeard.cyberware.common.misc;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;

public class NNLUtil
{
	public static NonNullList<ItemStack> copyList(NonNullList<ItemStack> nnl)
	{
		NonNullList<ItemStack> nnlCopy = NonNullList.create();
		nnlCopy.addAll(nnl);
		return nnlCopy;
	}

	public static NonNullList<ItemStack> fromArray(@Nonnull ItemStack[] array)
	{
		NonNullList<ItemStack> nnl = NonNullList.create();
		Collections.addAll(nnl, array);
		return nnl;
	}

	public static NonNullList<NonNullList<ItemStack>> fromArray(@Nonnull ItemStack[][] array)
	{
		NonNullList<NonNullList<ItemStack>> nnlRoot = NonNullList.create();
		for (ItemStack[] arraySub : array)
		{
			NonNullList<ItemStack> nnlSub = NonNullList.create();
			nnlSub.addAll(Arrays.asList(arraySub));
			nnlRoot.add(nnlSub);
		}
		return nnlRoot;
	}

	public static NonNullList<ItemStack> initListOfSize(int size)
	{
		NonNullList<ItemStack> nnl = NonNullList.create();
		for (int index = 0; index < size; index++)
		{
			nnl.add(ItemStack.EMPTY);
		}
		return nnl;
	}

	public static int findIndex(ItemStack stack, NonNullList<ItemStack> list)
	{
		int indexLoop = 0;

		for (ItemStack stackExisting : list)
		{
			if (!stackExisting.isEmpty()
				&& stackExisting.getItem() == stack.getItem()
				&& ItemStack.tagMatches(stackExisting, stack)
			)
			{
				return indexLoop;
			}

			indexLoop++;
		}

		return -1;
	}
}
