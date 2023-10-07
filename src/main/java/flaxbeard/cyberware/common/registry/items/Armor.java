package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemArmorCyberware;
import flaxbeard.cyberware.common.registry.Items;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class Armor
{
	public static final RegistryObject<Item> SHADES =
		register("shades", () -> new ItemArmorCyberware(ArmorMaterials.SHADES, EquipmentSlot.HEAD, new Item.Properties()));
	public static final RegistryObject<Item> SHADES2 =
		register("shades2", () -> new ItemArmorCyberware(ArmorMaterials.SHADES, EquipmentSlot.HEAD, new Item.Properties()));
	public static final RegistryObject<Item> JACKET =
		register("jacket", () -> new ItemArmorCyberware(ArmorMaterials.JACKET, EquipmentSlot.CHEST, new Item.Properties()));
	public static final RegistryObject<Item> TRENCH_COAT =
		register("trench_coat", () -> new ItemArmorCyberware(ArmorMaterials.TRENCH_COAT, EquipmentSlot.CHEST, new Item.Properties()));

	public static <I extends Item> RegistryObject<I> register(final String name, final Supplier<? extends I> sup)
	{
		return Items.ITEMS.register(name, sup);
	}
}
