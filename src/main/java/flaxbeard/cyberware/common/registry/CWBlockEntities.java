package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.block.tile.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class CWBlockEntities
{
	private CWBlockEntities()
	{
	}

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
		DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Cyberware.MODID);
	public static final RegistryObject<BlockEntityType<TileEntityBeacon>> BEACON =
		register("beacon", TileEntityBeacon::new, CWBlocks.BEACON);
	public static final RegistryObject<BlockEntityType<TileEntityBeaconLarge>> BEACON_LARGE =
		register("beacon_large", TileEntityBeaconLarge::new, CWBlocks.BEACON_LARGE);
	public static final RegistryObject<BlockEntityType<TileEntityBeaconPost>> BEACON_POST =
		register("beacon_post", TileEntityBeaconPost::new, CWBlocks.BEACON_POST);
	public static final RegistryObject<BlockEntityType<TileEntityBeaconPost.TileEntityBeaconPostMaster>> BEACON_POST_MASTER =
		register("beacon_post_master", TileEntityBeaconPost.TileEntityBeaconPostMaster::new, CWBlocks.BEACON_POST);
	public static final RegistryObject<BlockEntityType<TileEntityBlueprintArchive>> BLUEPRINT_ARCHIVE =
		register("blueprint_archive", TileEntityBlueprintArchive::new, CWBlocks.BLUEPRINT_ARCHIVE);
	public static final RegistryObject<BlockEntityType<TileEntityCharger>> CHARGER =
		register("charger", TileEntityCharger::new, CWBlocks.CHARGER);
	public static final RegistryObject<BlockEntityType<TileEntityComponentBox>> COMPONENT_BOX =
		register("component_box", TileEntityComponentBox::new, CWBlocks.COMPONENT_BOX);
	public static final RegistryObject<BlockEntityType<TileEntityEngineeringTable>> ENGINEERING_TABLE =
		register("engineering_table", TileEntityEngineeringTable::new, CWBlocks.ENGINEERING_TABLE);
	public static final RegistryObject<BlockEntityType<TileEntityEngineeringTable.TileEntityEngineeringDummy>> ENGINEERING_TABLE_DUMMY =
		register("engineering_table_dummy", TileEntityEngineeringTable.TileEntityEngineeringDummy::new, CWBlocks.ENGINEERING_TABLE);
	public static final RegistryObject<BlockEntityType<TileEntityScanner>> SCANNER =
		register("scanner", TileEntityScanner::new, CWBlocks.SCANNER);
	public static final RegistryObject<BlockEntityType<TileEntitySurgery>> SURGERY =
		register("surgery", TileEntitySurgery::new, CWBlocks.SURGERY);
	public static final RegistryObject<BlockEntityType<TileEntitySurgeryChamber>> SURGERY_CHAMBER =
		register("surgery_chamber", TileEntitySurgeryChamber::new, CWBlocks.SURGERY_CHAMBER);

	@SafeVarargs
	private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>>
	register(@Nonnull String name, BlockEntityType.BlockEntitySupplier<T> supplier, RegistryObject<Block>... pValidBlocks)
	{
		return BLOCK_ENTITY_TYPES.register(
			name,
			() -> BlockEntityType.Builder.of(
				supplier,
				Arrays.stream(pValidBlocks).map(RegistryObject::get).toArray(Block[]::new)
			).build(null)
		);
	}
}
