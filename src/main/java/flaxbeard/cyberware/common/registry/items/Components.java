package flaxbeard.cyberware.common.registry.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Components extends ItemRegistry
{
	public static final RegistryObject<Item> ACTUATOR = register("components.actuator", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> REACTOR = register("components.reactor", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> TITANIUM = register("components.titanium", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> SSC = register("components.ssc", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> PLATING = register("components.plating", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> FIBER_OPTICS = register("components.fiber_optics", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> FULLERENE = register("components.fullerene", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> SYNTH_NERVES = register("components.synth_nerves", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> STORAGE = register("components.storage", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> MICRO_ELECTRIC = register("components.micro_electric", () -> new Item(new Item.Properties()));
}
