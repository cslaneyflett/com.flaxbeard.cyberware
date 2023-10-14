package flaxbeard.cyberware.client.data;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.registry.CWBlockItems;
import flaxbeard.cyberware.common.registry.CWBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class CWBlockStateProvider extends BlockStateProvider
{
	public CWBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper)
	{
		super(gen, Cyberware.MODID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels()
	{
		// TODO: im dum dum idk how work this
		//directionalBlock(CWBlocks.BEACON.get(), beacon);
	}
}
