package flaxbeard.cyberware.common.registry.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Components extends ItemRegistry
{
	private Components()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> ACTUATOR = register("component_actuator", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> REACTOR = register("component_reactor", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> TITANIUM = register("component_titanium", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> SSC = register("component_ssc", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> PLATING = register("component_plating", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> FIBER_OPTICS = register("component_fiber_optics", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> FULLERENE = register("component_fullerene", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> SYNTH_NERVES = register("component_synth_nerves", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> STORAGE = register("component_storage", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> MICRO_ELECTRIC = register("component_micro_electric", () -> new Item(new Item.Properties()));
}
