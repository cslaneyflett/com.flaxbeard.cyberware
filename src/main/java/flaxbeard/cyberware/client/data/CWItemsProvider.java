package flaxbeard.cyberware.client.data;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.registry.CWBlockItems;
import flaxbeard.cyberware.common.registry.CWItems;
import flaxbeard.cyberware.common.registry.items.ArmUpgrades;
import flaxbeard.cyberware.common.registry.items.Armors;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class CWItemsProvider extends ItemModelProvider
{
	public CWItemsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper)
	{
		super(generator, Cyberware.MODID, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
		// they are all basic :)
		for (RegistryObject<Item> item: CWItems.ITEMS.getEntries()) {
			this.basicItem(item.get());
		}

		// TODO scavenged textures?
	}
}
