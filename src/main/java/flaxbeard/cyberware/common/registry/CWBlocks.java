package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CWBlocks
{
	private CWBlocks()
	{
	}

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Cyberware.MODID);
	private static final BlockBehaviour.Properties props = BlockBehaviour.Properties
		.of(Material.METAL).destroyTime(5.0F).explosionResistance(10.0F).sound(SoundType.METAL);
	public static final RegistryObject<Block> BEACON = BLOCKS.register("beacon", () -> new BlockBeacon(props));
	public static final RegistryObject<Block> BEACON_LARGE = BLOCKS.register("beacon_large", () -> new BlockBeaconLarge(props));
	public static final RegistryObject<Block> BEACON_POST = BLOCKS.register("beacon_post", () -> new BlockBeaconPost(props));
	public static final RegistryObject<Block> BLUEPRINT_ARCHIVE = BLOCKS.register("blueprint_archive", () -> new BlockBlueprintArchive(props));
	public static final RegistryObject<Block> CHARGER = BLOCKS.register("charger", () -> new BlockCharger(props));
	public static final RegistryObject<Block> COMPONENT_BOX = BLOCKS.register("component_box", () -> new BlockComponentBox(props));
	public static final RegistryObject<Block> ENGINEERING_TABLE = BLOCKS.register("engineering_table", () -> new BlockEngineeringTable(props));
	public static final RegistryObject<Block> SCANNER = BLOCKS.register("scanner", () -> new BlockScanner(props));
	public static final RegistryObject<Block> SURGERY = BLOCKS.register("surgery", () -> new BlockSurgery(props));
	public static final RegistryObject<Block> SURGERY_CHAMBER = BLOCKS.register("surgery_chamber", () -> new BlockSurgeryChamber(props));
}
