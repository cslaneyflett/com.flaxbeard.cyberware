package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemFootUpgrade;
import flaxbeard.cyberware.common.item.ItemLegUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class LegUpgrades extends ItemRegistry
{
	private LegUpgrades()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> JUMP_BOOST = legUpgrade("leg_upgrades_jump_boost", () -> new CyberwareProperties(
		Rarity.RARE, 3, 1,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 2)
	));
	public static final RegistryObject<Item> FALL_DAMAGE = legUpgrade("leg_upgrades_fall_damage", () -> new CyberwareProperties(
		Rarity.RARE, 2, 1,
		new ItemStack(TITANIUM.get(), 3), new ItemStack(PLATING.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1)
	));
	public static final RegistryObject<Item> SPURS = footUpgrade("foot_upgrades_spurs", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 1, 1,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(TITANIUM.get(), 1), new ItemStack(PLATING.get(), 1)
	));
	public static final RegistryObject<Item> AQUA = footUpgrade("foot_upgrades_aqua", () -> new CyberwareProperties(
		Rarity.RARE, 2, 1,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));
	public static final RegistryObject<Item> WHEELS = footUpgrade("foot_upgrades_wheels", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 3, 1,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(MICRO_ELECTRIC.get(), 2)
	));

	private static RegistryObject<Item> legUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemLegUpgrade(new Item.Properties(), props.get()));
	}

	private static RegistryObject<Item> footUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemFootUpgrade(new Item.Properties(), props.get()));
	}
}