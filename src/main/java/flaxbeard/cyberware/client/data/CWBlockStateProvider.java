package flaxbeard.cyberware.client.data;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.block.BlockEngineeringTable;
import flaxbeard.cyberware.common.block.BlockSurgeryChamber;
import flaxbeard.cyberware.common.registry.CWBlockItems;
import flaxbeard.cyberware.common.registry.CWBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CWBlockStateProvider extends BlockStateProvider
{
	public CWBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper)
	{
		super(gen, Cyberware.MODID, exFileHelper);
	}

	// why the fuck is this private in the forge class? second you want to do anything literally other than absolute
	// basics you have to call models directly, and that's what these helpers are LITERALLY FOR!

	private ResourceLocation key(Block block) {
		return ForgeRegistries.BLOCKS.getKey(block);
	}

	private String name(Block block) {
		return key(block).getPath();
	}

	private ResourceLocation extend(ResourceLocation rl, String suffix) {
		return new ResourceLocation(rl.getNamespace(), rl.getPath() + suffix);
	}

	@Override
	protected void registerStatesAndModels()
	{
		var beaconBlock = CWBlocks.BEACON.get();
		var beacon = models().getExistingFile(modLoc("block/radio"));
		horizontalBlock(beaconBlock, state -> beacon);
		simpleBlockItem(beaconBlock, beacon);


		var beaconLargeBlock = CWBlocks.BEACON_LARGE.get();
		var beaconLarge = models().getExistingFile(modLoc("block/beacon"));
		horizontalBlock(beaconLargeBlock, state -> beaconLarge);
		simpleBlockItem(beaconLargeBlock, beaconLarge);


		var blueprintBlock = CWBlocks.BLUEPRINT_ARCHIVE.get();
		var blueprint = models().orientable(
			name(blueprintBlock),
			modLoc("block/blank_machine"),
			blockTexture(blueprintBlock),
			modLoc("block/blank_machine")
		);
		horizontalBlock(blueprintBlock, blueprint);
		simpleBlockItem(blueprintBlock, blueprint);


		var chargerBlock = CWBlocks.CHARGER.get();
		var chargerTexture = blockTexture(chargerBlock);
		var charger = models().cubeTop(
			name(chargerBlock),
			extend(chargerTexture, "_side"),
			extend(chargerTexture, "_top")
		);
		simpleBlock(chargerBlock, charger);
		simpleBlockItem(chargerBlock, charger);


		var componentBlock = CWBlocks.COMPONENT_BOX.get();
		var toolbox = models().getExistingFile(modLoc("block/toolbox"));
		horizontalBlock(componentBlock, state -> toolbox);
		simpleBlockItem(componentBlock, toolbox);


		var engineeringBlock = CWBlocks.ENGINEERING_TABLE.get();
		var engineeringTop = models().getExistingFile(modLoc("block/engineering_top"));
		var engineeringBottom = models().cube(
			name(engineeringBlock),
			modLoc("block/blank_machine"),
			modLoc("block/blank_machine"),
			modLoc("block/engineering_bottom_side1"),
			modLoc("block/engineering_bottom_side1"),
			modLoc("block/engineering_bottom_side2"),
			modLoc("block/engineering_bottom_side2")
		);

		getVariantBuilder(engineeringBlock)
			.forAllStates(state -> {
				int yRot = ((int) state.getValue(BlockEngineeringTable.FACING).toYRot());
				boolean lower = state.getValue(BlockEngineeringTable.HALF) == DoubleBlockHalf.LOWER;

				var model = lower ? engineeringBottom : engineeringTop;

				return ConfiguredModel.builder().modelFile(model)
					.rotationY(yRot)
					.build();
			});
		// BlockItem is a basic item, check item provider


		var scannerBlock = CWBlocks.SCANNER.get();
		var scanner = models().getExistingFile(modLoc("block/scanner"));
		simpleBlock(scannerBlock, scanner);
		simpleBlockItem(scannerBlock, scanner);


		var surgeryBlock = CWBlocks.SURGERY.get();
		var surgery = models().cubeTop(
			name(surgeryBlock),
			modLoc("block/robosurgeon_screen"),
			modLoc("block/blank_machine")
		);
		simpleBlock(surgeryBlock, surgery);
		simpleBlockItem(surgeryBlock, surgery);


		var surgeryChamberBlock = CWBlocks.SURGERY_CHAMBER.get();
		var surgeryTop = models().getExistingFile(modLoc("block/surgery_top"));
		var surgeryBottom = models().getExistingFile(modLoc("block/surgery_bottom"));

		getVariantBuilder(surgeryChamberBlock)
			.forAllStates(state -> {
				int yRot = ((int) state.getValue(BlockSurgeryChamber.FACING).toYRot());
				boolean lower = state.getValue(BlockSurgeryChamber.HALF) == DoubleBlockHalf.LOWER;

				var model = lower ? surgeryBottom : surgeryTop;

				return ConfiguredModel.builder().modelFile(model)
					.rotationY(yRot)
					.build();
			});
		// BlockItem is a basic item, check item provider
	}
}
