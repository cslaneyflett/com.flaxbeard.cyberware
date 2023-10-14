package flaxbeard.cyberware.common.tools;

import com.electronwill.nightconfig.core.CommentedConfig;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.client.data.CWBlockStateProvider;
import flaxbeard.cyberware.client.data.CWItemsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = Cyberware.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CWDataGenerator
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		bootstrapConfigs();

		DataGenerator gen = event.getGenerator();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

		gen.addProvider(event.includeClient(), new CWBlockStateProvider(gen, existingFileHelper));
		gen.addProvider(event.includeClient(), new CWItemsProvider(gen, existingFileHelper));
	}

	/**
	 * Used to bootstrap configs to their default values so that if we are querying if things exist we don't have issues with it happening to early or in cases we have
	 * fake tiles.
	 * Copied from Mekanism.
	 */
	public static void bootstrapConfigs() {
		ConfigTracker.INSTANCE.configSets().forEach((type, configs) -> {
			for (ModConfig config : configs) {
				if (config.getModId().equals(Cyberware.MODID)) {
					//Similar to how ConfigTracker#loadDefaultServerConfigs works for loading default server configs on the client
					// except we don't bother firing an event as it is private, and we are already at defaults if we had called earlier,
					// and we also don't fully initialize the mod config as the spec is what we care about, and we can do so without having
					// to reflect into package private methods
					CommentedConfig commentedConfig = CommentedConfig.inMemory();
					config.getSpec().correct(commentedConfig);
					config.getSpec().acceptConfig(commentedConfig);
				}
			}
		});
	}
}
