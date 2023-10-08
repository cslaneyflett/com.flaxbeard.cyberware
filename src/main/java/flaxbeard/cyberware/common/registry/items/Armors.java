package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemArmorCyberware;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Armors extends ItemRegistry
{
	public static final RegistryObject<Item> SHADES =
		register("shades", () -> new ItemArmorCyberware(ArmorMaterials.SHADES, EquipmentSlot.HEAD, new Item.Properties()));
	public static final RegistryObject<Item> SHADES2 =
		register("shades2", () -> new ItemArmorCyberware(ArmorMaterials.SHADES, EquipmentSlot.HEAD, new Item.Properties()));
	public static final RegistryObject<Item> JACKET =
		register("jacket", () -> new ItemArmorCyberware(ArmorMaterials.JACKET, EquipmentSlot.CHEST, new Item.Properties()));
	public static final RegistryObject<Item> TRENCH_COAT =
		register("trench_coat", () -> new ItemArmorCyberware(ArmorMaterials.TRENCH_COAT, EquipmentSlot.CHEST, new Item.Properties()));
}
