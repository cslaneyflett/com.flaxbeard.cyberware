package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.registry.Items;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

abstract public class ItemRegistry
{
	public static <I extends Item> RegistryObject<I> register(final String name, final Supplier<? extends I> sup)
	{
		return Items.ITEMS.register(name, sup);
	}
}
