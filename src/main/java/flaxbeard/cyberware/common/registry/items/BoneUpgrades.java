package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.CyberwareProperties.Rarity;
import flaxbeard.cyberware.common.item.CyberwareProperties;
import flaxbeard.cyberware.common.item.ItemBoneUpgrade;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class BoneUpgrades extends ItemRegistry
{
	public static final RegistryObject<Item> LACING = boneUpgrade("bone_upgrades.lacing", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 3,
		new ItemStack(REACTOR.get(), 1), new ItemStack(TITANIUM.get(), 2), new ItemStack(FULLERENE.get(), 2)
	));
	public static final RegistryObject<Item> FLEX = boneUpgrade("bone_upgrades.flex", () -> new CyberwareProperties(
		Rarity.RARE, 5,
		new ItemStack(REACTOR.get(), 3), new ItemStack(TITANIUM.get(), 2), new ItemStack(STORAGE.get(), 2)
	));
	public static final RegistryObject<Item> BATTERY = boneUpgrade("bone_upgrades.battery", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 2,
		new ItemStack(REACTOR.get(), 2), new ItemStack(STORAGE.get(), 2), new ItemStack(MICRO_ELECTRIC.get(), 1)
	));

	private static RegistryObject<Item> boneUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemBoneUpgrade(new Item.Properties(), props.get()));
	}
}
