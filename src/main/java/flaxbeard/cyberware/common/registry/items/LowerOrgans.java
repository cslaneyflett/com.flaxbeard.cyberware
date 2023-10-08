package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.CyberwareProperties;
import flaxbeard.cyberware.common.item.CyberwareProperties.Rarity;
import flaxbeard.cyberware.common.item.ItemCreativeBattery;
import flaxbeard.cyberware.common.item.ItemDenseBattery;
import flaxbeard.cyberware.common.item.ItemLowerOrgansUpgrade;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class LowerOrgans extends ItemRegistry
{
	public static final RegistryObject<Item> BATTERY_DENSE = batteryDense("lowerorgans_upgrades.battery_dense", () -> new CyberwareProperties(
		Rarity.RARE, 15,
		new ItemStack(FULLERENE.get(), 3), new ItemStack(MICRO_ELECTRIC.get(), 4)
	));
	public static final RegistryObject<Item> BATTERY_CREATIVE = batteryCreative("lowerorgans_upgrades.battery_creative", () -> new CyberwareProperties(
		Rarity.NEVER, 0
	));
	public static final RegistryObject<Item> LIVER_FILTER = lowerOrgansUpgrade("lower_organs_upgrades.liver_filter", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 5,
		new ItemStack(REACTOR.get(), 3), new ItemStack(STORAGE.get(), 2)
	));
	public static final RegistryObject<Item> METABOLIC = lowerOrgansUpgrade("lower_organs_upgrades.metabolic", () -> new CyberwareProperties(
		Rarity.COMMON, 5,
		new ItemStack(REACTOR.get(), 3), new ItemStack(SSC.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));
	public static final RegistryObject<Item> BATTERY = lowerOrgansUpgrade("lower_organs_upgrades.battery", () -> new CyberwareProperties(
		Rarity.VERY_COMMON, 10,
		new ItemStack(REACTOR.get(), 1), new ItemStack(STORAGE.get(), 2), new ItemStack(MICRO_ELECTRIC.get(), 3)
	));
	public static final RegistryObject<Item> ADRENALINE = lowerOrgansUpgrade("lower_organs_upgrades.adrenaline", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 5,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(REACTOR.get(), 2)
	));

	private static RegistryObject<Item> batteryDense(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemDenseBattery(new Item.Properties(), props.get()));
	}

	private static RegistryObject<Item> batteryCreative(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemCreativeBattery(new Item.Properties(), props.get()));
	}

	private static RegistryObject<Item> lowerOrgansUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemLowerOrgansUpgrade(new Item.Properties(), props.get()));
	}
}