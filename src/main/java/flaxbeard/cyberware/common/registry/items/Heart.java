package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemCyberheart;
import flaxbeard.cyberware.common.item.ItemHeartUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import flaxbeard.cyberware.common.registry.CWCreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class Heart extends ItemRegistry
{
	private Heart()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> CYBERHEART_BASE = heart("cyberheart", () -> new CyberwareProperties(
		Rarity.COMMON, 5, 1,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 1), new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<Item> DEFIBRILLATOR = heartUpgrade("heart_upgrades_defibrillator", () -> new CyberwareProperties(
		Rarity.COMMON, 10, 1,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(FULLERENE.get(), 2), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));
	public static final RegistryObject<Item> PLATELETS = heartUpgrade("heart_upgrades_platelets", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 5, 1,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(REACTOR.get(), 2), new ItemStack(STORAGE.get(), 1)
	));
	public static final RegistryObject<Item> MEDKIT = heartUpgrade("heart_upgrades_medkit", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 15, 1,
		new ItemStack(REACTOR.get(), 3), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<Item> CARDIOVASCULAR_COUPLER = heartUpgrade("heart_upgrades_coupler", () -> new CyberwareProperties(
		Rarity.VERY_COMMON, 10, 1,
		new ItemStack(REACTOR.get(), 1), new ItemStack(SYNTH_NERVES.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));

	private static RegistryObject<Item> heartUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemHeartUpgrade(new Item.Properties(), props.get()));
	}

	private static RegistryObject<Item> heart(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemCyberheart(new Item.Properties().tab(CWCreativeTabs.CYBERWARE), props.get()));
	}
}