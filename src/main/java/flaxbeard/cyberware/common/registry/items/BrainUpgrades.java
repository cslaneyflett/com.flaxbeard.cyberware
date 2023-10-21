package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemBrainUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import flaxbeard.cyberware.common.registry.CWCreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class BrainUpgrades extends ItemRegistry
{
	private BrainUpgrades()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> CORTICAL_STACK = brainUpgrade("brain_upgrades_cortical_stack", () -> new CyberwareProperties(
		Rarity.RARE, 3, 1,
		new ItemStack(REACTOR.get(), 2), new ItemStack(SYNTH_NERVES.get(), 1), new ItemStack(STORAGE.get(), 2)
	));
	public static final RegistryObject<Item> ENDER_JAMMER = brainUpgrade("brain_upgrades_ender_jammer", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 10, 1,
		new ItemStack(TITANIUM.get(), 1), new ItemStack(SSC.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));
	public static final RegistryObject<Item> CONSCIOUSNESS_TRANSMITTER = brainUpgrade("brain_upgrades_consciousness_transmitter", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 2, 1,
		new ItemStack(SSC.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 3)
	));
	public static final RegistryObject<Item> NEURAL_CONTEXTUALIZER = brainUpgrade("brain_upgrades_neural_contextualizer", () -> new CyberwareProperties(
		Rarity.COMMON, 2, 1,
		new ItemStack(SSC.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 3)
	));
	public static final RegistryObject<Item> THREAT_MATRIX = brainUpgrade("brain_upgrades_matrix", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 8, 1,
		new ItemStack(SSC.get(), 3), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));
	public static final RegistryObject<Item> RADIO = brainUpgrade("brain_upgrades_radio", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 2, 1,
		new ItemStack(TITANIUM.get(), 1), new ItemStack(SSC.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));

	private static RegistryObject<Item> brainUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemBrainUpgrade(new Item.Properties().tab(CWCreativeTabs.CYBERWARE), props.get()));
	}
}