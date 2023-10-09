package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemArmUpgrade;
import flaxbeard.cyberware.common.item.ItemHandUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class ArmUpgrades extends ItemRegistry {
	public static final RegistryObject<Item> BOW = armUpgrade("arm_upgrades.bow", () -> new CyberwareProperties(
		Rarity.RARE, 3, 1,
		new ItemStack(ACTUATOR.get(), 4), new ItemStack(PLATING.get(), 2)
	));

	public static final RegistryObject<Item> CRAFT_HANDS = handUpgrade("hand_upgrades.craft_hands", () -> new CyberwareProperties(
		Rarity.RARE, 2, 1,
		new ItemStack(ACTUATOR.get(), 4), new ItemStack(SSC.get(), 1), new ItemStack(PLATING.get(), 1)
	));
	public static final RegistryObject<Item> CLAWS = handUpgrade("hand_upgrades.claws", () -> new CyberwareProperties(
		Rarity.RARE, 2, 1,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 2), new ItemStack(PLATING.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(STORAGE.get(), 2)
	));
	public static final RegistryObject<Item> MINING = handUpgrade("hand_upgrades.mining", () -> new CyberwareProperties(
		Rarity.RARE, 1, 1,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 1), new ItemStack(PLATING.get(), 1), new ItemStack(FULLERENE.get(), 2)
	));


	private static RegistryObject<Item> armUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemArmUpgrade(new Item.Properties(), props.get()));
	}

	private static RegistryObject<Item> handUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemHandUpgrade(new Item.Properties(), props.get()));
	}
}