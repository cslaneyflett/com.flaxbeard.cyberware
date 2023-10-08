package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.*;
import flaxbeard.cyberware.common.item.CyberwareProperties.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class Heart extends ItemRegistry {
	public static final RegistryObject<Item> CYBERHEART_BASE = heart("cyberheart_upgrades.base", () -> new CyberwareProperties(
		Rarity.COMMON, 5,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 1), new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<Item> DEFIBRILLATOR = heartUpgrade("cyberheart_upgrades.defibrillator", () -> new CyberwareProperties(
		Rarity.COMMON, 10,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(FULLERENE.get(), 2), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));
	public static final RegistryObject<Item> PLATELETS = heartUpgrade("cyberheart_upgrades.platelets", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 5,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(REACTOR.get(), 2), new ItemStack(STORAGE.get(), 1)
	));
	public static final RegistryObject<Item> MEDKIT = heartUpgrade("cyberheart_upgrades.medkit", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 15,
		new ItemStack(REACTOR.get(), 3), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<Item> COUPLER = heartUpgrade("cyberheart_upgrades.coupler", () -> new CyberwareProperties(
		Rarity.VERY_COMMON, 10,
		new ItemStack(REACTOR.get(), 1), new ItemStack(SYNTH_NERVES.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));


	private static RegistryObject<Item> heartUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemHeartUpgrade(new Item.Properties(), props.get()));
	}

	private static RegistryObject<Item> heart(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemCyberheart(new Item.Properties(), props.get()));
	}
}