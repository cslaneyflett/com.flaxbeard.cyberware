package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.CyberwareProperties;
import flaxbeard.cyberware.common.item.CyberwareProperties.Rarity;
import flaxbeard.cyberware.common.item.ItemBrainUpgrade;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class BrainUpgrades extends ItemRegistry
{
	public static final RegistryObject<Item> CORTICAL_STACK = brainUpgrade("brain_upgrades.cortical_stack", () -> new CyberwareProperties(
		Rarity.RARE, 3,
		new ItemStack(REACTOR.get(), 2), new ItemStack(SYNTH_NERVES.get(), 1), new ItemStack(STORAGE.get(), 2)
	));
	public static final RegistryObject<Item> ENDER_JAMMER = brainUpgrade("brain_upgrades.ender_jammer", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 10,
		new ItemStack(TITANIUM.get(), 1), new ItemStack(SSC.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));
	public static final RegistryObject<Item> CONSCIOUSNESS_TRANSMITTER = brainUpgrade("brain_upgrades.consciousness_transmitter", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 2,
		new ItemStack(SSC.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 3)
	));
	public static final RegistryObject<Item> NEURAL_CONTEXTUALIZER = brainUpgrade("brain_upgrades.neural_contextualizer", () -> new CyberwareProperties(
		Rarity.COMMON, 2,
		new ItemStack(SSC.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 3)
	));
	public static final RegistryObject<Item> MATRIX = brainUpgrade("brain_upgrades.matrix", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 8,
		new ItemStack(SSC.get(), 3), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));
	public static final RegistryObject<Item> RADIO = brainUpgrade("brain_upgrades.radio", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 2,
		new ItemStack(TITANIUM.get(), 1), new ItemStack(SSC.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));

	private static RegistryObject<Item> brainUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemBrainUpgrade(new Item.Properties(), props.get()));
	}
}