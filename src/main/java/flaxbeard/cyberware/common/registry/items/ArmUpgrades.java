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

public class ArmUpgrades extends ItemRegistry
{
	private ArmUpgrades()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<ItemArmUpgrade> BOW = armUpgrade("arm_upgrades_bow", () -> new CyberwareProperties(
		Rarity.RARE, 3, 1,
		new ItemStack(ACTUATOR.get(), 4), new ItemStack(PLATING.get(), 2)
	));
	public static final RegistryObject<ItemHandUpgrade> CRAFT_HANDS = handUpgrade("hand_upgrades_craft_hands", () -> new CyberwareProperties(
		Rarity.RARE, 2, 1,
		new ItemStack(ACTUATOR.get(), 4), new ItemStack(SSC.get(), 1), new ItemStack(PLATING.get(), 1)
	));
	public static final RegistryObject<ItemHandUpgrade> CLAWS = handUpgrade("hand_upgrades_claws", () -> new CyberwareProperties(
		Rarity.RARE, 2, 1,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 2), new ItemStack(PLATING.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(STORAGE.get(), 2)
	));
	public static final RegistryObject<ItemHandUpgrade> MINING = handUpgrade("hand_upgrades_mining", () -> new CyberwareProperties(
		Rarity.RARE, 1, 1,
		new ItemStack(ACTUATOR.get(), 2), new ItemStack(TITANIUM.get(), 1), new ItemStack(PLATING.get(), 1), new ItemStack(FULLERENE.get(), 2)
	));

	private static RegistryObject<ItemArmUpgrade> armUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemArmUpgrade(new Item.Properties(), props.get()));
	}

	private static RegistryObject<ItemHandUpgrade> handUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemHandUpgrade(new Item.Properties(), props.get()));
	}
}