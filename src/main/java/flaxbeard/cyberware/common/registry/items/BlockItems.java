package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.item.ItemComponentBox;
import flaxbeard.cyberware.common.block.item.ItemEngineeringTable;
import flaxbeard.cyberware.common.block.item.ItemSurgeryChamber;
import flaxbeard.cyberware.common.registry.Blocks;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class BlockItems extends ItemRegistry
{
	public static final RegistryObject<Item> BEACON = register("beacon", () -> new ItemBlockCyberware(
		Blocks.BEACON.get(), new Item.Properties(),
		"cyberware.tooltip.beacon"
	));
	public static final RegistryObject<Item> BEACON_LARGE =
		register("beacon_large", () -> new ItemBlockCyberware(
			Blocks.BEACON_LARGE.get(), new Item.Properties(),
			"cyberware.tooltip.beacon_large"
		));
	public static final RegistryObject<Item> BEACON_POST =
		register("beacon_post", () -> new ItemBlockCyberware(
			Blocks.BEACON_POST.get(), new Item.Properties(),
			"cyberware.tooltip.beacon_post.0", "cyberware.tooltip.beacon_post.1", "cyberware.tooltip.beacon_post.2"
		));
	public static final RegistryObject<Item> BLUEPRINT_ARCHIVE =
		register("blueprint_archive", () -> new ItemBlockCyberware(
			Blocks.BLUEPRINT_ARCHIVE.get(), new Item.Properties(),
			"cyberware.tooltip.blueprint_archive.0", "cyberware.tooltip.blueprint_archive.1"
		));
	public static final RegistryObject<Item> CHARGER =
		register("charger", () -> new ItemBlockCyberware(
			Blocks.CHARGER.get(), new Item.Properties(),
			"cyberware.tooltip.charger.0", "cyberware.tooltip.charger.1"
		));
	public static final RegistryObject<Item> COMPONENT_BOX =
		register("component_box", () -> new ItemComponentBox(
			Blocks.COMPONENT_BOX.get(), new Item.Properties()
		));
	public static final RegistryObject<Item> ENGINEERING_TABLE =
		register("engineering_table", () -> new ItemEngineeringTable(
			Blocks.ENGINEERING_TABLE.get(), new Item.Properties(),
			"cyberware.tooltip.engineering_table"
		));
	public static final RegistryObject<Item> SCANNER =
		register("scanner", () -> new ItemBlockCyberware(
			Blocks.SCANNER.get(), new Item.Properties(),
			"cyberware.tooltip.scanner"
		));
	public static final RegistryObject<Item> SURGERY =
		register("surgery", () -> new ItemBlockCyberware(
			Blocks.SURGERY.get(), new Item.Properties(),
			"cyberware.tooltip.surgery.0", "cyberware.tooltip.surgery.1"
		));
	public static final RegistryObject<Item> SURGERY_CHAMBER =
		register("surgery_chamber", () -> new ItemSurgeryChamber(
			Blocks.SURGERY_CHAMBER.get(), new Item.Properties(),
			"cyberware.tooltip.surgery_chamber.0", "cyberware.tooltip.surgery_chamber.1"
		));
}
