package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemCybereyeUpgrade;
import flaxbeard.cyberware.common.item.ItemCybereyes;
import flaxbeard.cyberware.common.item.ItemEyeUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class Eyes extends ItemRegistry
{
	private Eyes()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> CYBEREYE_BASE = eyes("cybereyes", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 8, 1,
		new ItemStack(PLATING.get(), 1), new ItemStack(FIBER_OPTICS.get(), 2), new ItemStack(SYNTH_NERVES.get(), 2)
	));
	public static final RegistryObject<Item> HUDLENS = eyeUpgrade2("eye_upgrades_hudlens", () -> new CyberwareProperties(
		Rarity.VERY_COMMON, 1, 1,
		new ItemStack(SSC.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 2)
	));
	public static final RegistryObject<Item> NIGHT_VISION = eyeUpgrade("cybereye_upgrades_night_vision", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 2, 1,
		new ItemStack(PLATING.get(), 1), new ItemStack(FIBER_OPTICS.get(), 2), new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<Item> UNDERWATER_VISION = eyeUpgrade("cybereye_upgrades_underwater_vision", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 2, 1,
		new ItemStack(FIBER_OPTICS.get(), 2), new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<Item> HUDJACK = eyeUpgrade("cybereye_upgrades_hudjack", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 1, 1,
		new ItemStack(SSC.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 2)
	));
	public static final RegistryObject<Item> TARGETING = eyeUpgrade("cybereye_upgrades_targeting", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 1, 1,
		new ItemStack(SSC.get(), 2), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(FULLERENE.get(), 1), new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<Item> ZOOM = eyeUpgrade("cybereye_upgrades_zoom", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 1, 1,
		new ItemStack(FIBER_OPTICS.get(), 2), new ItemStack(SYNTH_NERVES.get(), 4)
	));

	private static RegistryObject<Item> eyeUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemCybereyeUpgrade(new Item.Properties(), props.get()));
	}

	private static RegistryObject<Item> eyeUpgrade2(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemEyeUpgrade(new Item.Properties(), props.get()));
	}

	private static RegistryObject<Item> eyes(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemCybereyes(new Item.Properties(), props.get()));
	}
}