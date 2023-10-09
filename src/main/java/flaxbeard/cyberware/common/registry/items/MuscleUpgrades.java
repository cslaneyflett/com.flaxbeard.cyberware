package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemMuscleUpgrade;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class MuscleUpgrades extends ItemRegistry
{
	public static final RegistryObject<Item> WIRED_REFLEXES = muscleUpgrade("muscle_upgrades.wired_reflexes", () -> new CyberwareProperties(
		Rarity.UNCOMMON, 5, 1,
		new ItemStack(SSC.get(), 1), new ItemStack(FIBER_OPTICS.get(), 1), new ItemStack(SYNTH_NERVES.get(), 3)
	));
	public static final RegistryObject<Item> MUSCLE_REPLACEMENTS = muscleUpgrade("muscle_upgrades.muscle_replacements", () -> new CyberwareProperties(
		Rarity.RARE, 15, 1,
		new ItemStack(ACTUATOR.get(), 3), new ItemStack(TITANIUM.get(), 1), new ItemStack(FIBER_OPTICS.get(), 2)
	));

	private static RegistryObject<Item> muscleUpgrade(@Nonnull String name, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemMuscleUpgrade(new Item.Properties(), props.get()));
	}
}