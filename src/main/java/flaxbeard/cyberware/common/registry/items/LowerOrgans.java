package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemCreativeBattery;
import flaxbeard.cyberware.common.item.ItemDenseBattery;
import flaxbeard.cyberware.common.item.ItemLowerOrgansUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import flaxbeard.cyberware.common.registry.CWCreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class LowerOrgans extends ItemRegistry
{
	private LowerOrgans()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<ItemDenseBattery> BATTERY_DENSE = batteryDense("dense_battery", () -> new CyberwareProperties(
		Rarity.RARE, 15, 1,
		new ItemStack(FULLERENE.get(), 3), new ItemStack(MICRO_ELECTRIC.get(), 4)
	));
	public static final RegistryObject<ItemCreativeBattery> BATTERY_CREATIVE = batteryCreative("creative_battery", () -> new CyberwareProperties(
		Rarity.NEVER, 0, 1
	));
	public static final RegistryObject<ItemLowerOrgansUpgrade> LIVER_FILTER = lowerOrgansUpgrade("lower_organs_upgrades_liver_filter", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 5, 1,
		new ItemStack(REACTOR.get(), 3), new ItemStack(STORAGE.get(), 2)
	));
	public static final RegistryObject<ItemLowerOrgansUpgrade> METABOLIC = lowerOrgansUpgrade("lower_organs_upgrades_metabolic", () -> new CyberwareProperties(
		Rarity.COMMON, 5, 1,
		new ItemStack(REACTOR.get(), 3), new ItemStack(SSC.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));
	public static final RegistryObject<ItemLowerOrgansUpgrade> BATTERY = lowerOrgansUpgrade("lower_organs_upgrades_battery", () -> new CyberwareProperties(
		Rarity.VERY_COMMON, 10, 1,
		new ItemStack(REACTOR.get(), 1), new ItemStack(STORAGE.get(), 2), new ItemStack(MICRO_ELECTRIC.get(), 3)
	));
	public static final RegistryObject<ItemLowerOrgansUpgrade> ADRENALINE = lowerOrgansUpgrade("lower_organs_upgrades_adrenaline", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 5, 1,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(REACTOR.get(), 2)
	));

	private static RegistryObject<ItemDenseBattery> batteryDense(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemDenseBattery(new Item.Properties().tab(CWCreativeTabs.CYBERWARE), props.get()));
	}

	private static RegistryObject<ItemCreativeBattery> batteryCreative(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemCreativeBattery(new Item.Properties().tab(CWCreativeTabs.CYBERWARE), props.get()));
	}

	private static RegistryObject<ItemLowerOrgansUpgrade> lowerOrgansUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemLowerOrgansUpgrade(new Item.Properties().tab(CWCreativeTabs.CYBERWARE), props.get()));
	}
}