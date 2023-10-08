package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.*;
import flaxbeard.cyberware.common.item.CyberwareProperties.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class LegUpgrades extends ItemRegistry {
	public static final RegistryObject<Item> JUMP_BOOST = legUpgrade("leg_upgrades.jump_boost", () -> new CyberwareProperties(
		Rarity.RARE, 3,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 2)
	));
	public static final RegistryObject<Item> FALL_DAMAGE = legUpgrade("leg_upgrades.fall_damage", () -> new CyberwareProperties(
		Rarity.RARE, 2,
		new ItemStack(TITANIUM.get(), 3), new ItemStack(PLATING.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1)
	));

	public static final RegistryObject<Item> SPURS = footUpgrade("foot_upgrades.spurs", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 1,
		new ItemStack(ACTUATOR.get(), 1), new ItemStack(TITANIUM.get(), 1), new ItemStack(PLATING.get(), 1)
	));
	public static final RegistryObject<Item> AQUA = footUpgrade("foot_upgrades.aqua", () -> new CyberwareProperties(
		Rarity.RARE, 2,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 1), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));
	public static final RegistryObject<Item> WHEELS = footUpgrade("foot_upgrades.wheels", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 3,
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