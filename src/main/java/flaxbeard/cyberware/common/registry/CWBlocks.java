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
	public static final RegistryObject<Block> BEACON = BLOCKS.register("block_beacon", () -> new BlockBeacon(props));
	public static final RegistryObject<Block> BEACON_LARGE = BLOCKS.register("block_beacon_large", () -> new BlockBeaconLarge(props));
	public static final RegistryObject<Block> BEACON_POST = BLOCKS.register("block_beacon_post", () -> new BlockBeaconPost(props));
	public static final RegistryObject<Block> BLUEPRINT_ARCHIVE = BLOCKS.register("block_blueprint_archive", () -> new BlockBlueprintArchive(props));
	public static final RegistryObject<Block> CHARGER = BLOCKS.register("block_charger", () -> new BlockCharger(props));
	public static final RegistryObject<Block> COMPONENT_BOX = BLOCKS.register("block_component_box", () -> new BlockComponentBox(props));
	public static final RegistryObject<Block> ENGINEERING_TABLE = BLOCKS.register("block_engineering_table", () -> new BlockEngineeringTable(props));
	public static final RegistryObject<Block> SCANNER = BLOCKS.register("block_scanner", () -> new BlockScanner(props));
	public static final RegistryObject<Block> SURGERY = BLOCKS.register("block_surgery", () -> new BlockSurgery(props));
	public static final RegistryObject<Block> SURGERY_CHAMBER = BLOCKS.register("block_surgery_chamber", () -> new BlockSurgeryChamber(props));
}
