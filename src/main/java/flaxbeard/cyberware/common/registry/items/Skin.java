package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemSkinUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import flaxbeard.cyberware.common.registry.CWCreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class Skin extends ItemRegistry
{
	private Skin()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> SOLAR_SKIN = skinUpgrade("skin_upgrades_solar_skin", () -> new CyberwareProperties(
		Rarity.VERY_COMMON, 12, 1,
		new ItemStack(REACTOR.get(), 1), new ItemStack(PLATING.get(), 1), new ItemStack(FIBER_OPTICS.get(), 2), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));
	public static final RegistryObject<Item> SUBDERMAL_SPIKES = skinUpgrade("skin_upgrades_subdermal_spikes", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 12, 1,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(TITANIUM.get(), 2), new ItemStack(PLATING.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));
	public static final RegistryObject<Item> FAKE_SKIN = skinUpgrade("skin_upgrades_fake_skin", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 0, 1,
		new ItemStack(REACTOR.get(), 1), new ItemStack(PLATING.get(), 3), new ItemStack(FIBER_OPTICS.get(), 2)
	));
	public static final RegistryObject<Item> IMMUNO = skinUpgrade("skin_upgrades_immuno", () -> new CyberwareProperties(
		Rarity.RARE, -25, 1,
		new ItemStack(REACTOR.get(), 3), new ItemStack(TITANIUM.get(), 1), new ItemStack(SYNTH_NERVES.get(), 1), new ItemStack(STORAGE.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));

	private static RegistryObject<Item> skinUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemSkinUpgrade(new Item.Properties().tab(CWCreativeTabs.CYBERWARE), props.get()));
	}
}