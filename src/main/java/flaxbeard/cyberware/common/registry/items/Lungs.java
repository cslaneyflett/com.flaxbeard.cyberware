package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemLungsUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import flaxbeard.cyberware.common.registry.CWCreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class Lungs extends ItemRegistry
{
	private Lungs()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> OXYGEN = lungsUpgrade("lungs_upgrades_oxygen", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 15, 1,
		new ItemStack(REACTOR.get(), 2), new ItemStack(STORAGE.get(), 2)
	));
	public static final RegistryObject<Item> HYPER_OXYGENATION = lungsUpgrade("lungs_upgrades_hyperoxygenation", () -> new CyberwareProperties(
		Rarity.COMMON, 2, 1,
		new ItemStack(REACTOR.get(), 1), new ItemStack(STORAGE.get(), 2), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));

	private static RegistryObject<Item> lungsUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemLungsUpgrade(new Item.Properties().tab(CWCreativeTabs.CYBERWARE), props.get()));
	}
}