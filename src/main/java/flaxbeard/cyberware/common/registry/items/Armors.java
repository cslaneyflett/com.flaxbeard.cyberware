package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemArmorCyberware;
import flaxbeard.cyberware.common.registry.CWCreativeTabs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Armors extends ItemRegistry
{
	private Armors()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> SHADES =
		register("shades", () -> new ItemArmorCyberware(ArmorMaterials.SHADES, EquipmentSlot.HEAD, new Item.Properties().tab(CWCreativeTabs.CYBERWARE)));
	public static final RegistryObject<Item> SHADES2 =
		register("shades2", () -> new ItemArmorCyberware(ArmorMaterials.SHADES, EquipmentSlot.HEAD, new Item.Properties().tab(CWCreativeTabs.CYBERWARE)));
	public static final RegistryObject<Item> JACKET =
		register("jacket", () -> new ItemArmorCyberware(ArmorMaterials.JACKET, EquipmentSlot.CHEST, new Item.Properties().tab(CWCreativeTabs.CYBERWARE)));
	public static final RegistryObject<Item> TRENCH_COAT =
		register("trench_coat", () -> new ItemArmorCyberware(ArmorMaterials.TRENCH_COAT, EquipmentSlot.CHEST, new Item.Properties().tab(CWCreativeTabs.CYBERWARE)));

	// TODO

	//	if (item == CyberwareContent.trenchCoat)
	//	{
	//		nnl.add(new ItemStack(CyberwareContent.component, 2, 2));
	//		nnl.add(new ItemStack(Items.LEATHER, 12, 0));
	//		nnl.add(new ItemStack(Items.DYE, 1, 0));
	//	} else if (item == CyberwareContent.jacket)
	//	{
	//		nnl.add(new ItemStack(CyberwareContent.component, 1, 2));
	//		nnl.add(new ItemStack(Items.LEATHER, 8, 0));
	//		nnl.add(new ItemStack(Items.DYE, 1, 0));
	//	} else
	//	{
	//		nnl.add(new ItemStack(Blocks.STAINED_GLASS, 4, 15));
	//		nnl.add(new ItemStack(CyberwareContent.component, 1, 4));
	//	}
}
