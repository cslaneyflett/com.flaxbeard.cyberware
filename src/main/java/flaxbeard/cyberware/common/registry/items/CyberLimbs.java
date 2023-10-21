package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.api.item.ICyberware.BodyPartEnum;
import flaxbeard.cyberware.common.item.ItemCyberlimb;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.registry.CWCreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static flaxbeard.cyberware.common.registry.items.Components.*;

public class CyberLimbs extends ItemRegistry
{
	private CyberLimbs()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<ItemCyberlimb> CYBERARM_LEFT = cyberLimb("cyberlimbs_cyberarm_left", BodyPartEnum.ARM_LEFT, () -> new CyberwareProperties(
		CyberwareProperties.Rarity.NEVER, 15, 1,
		new ItemStack(ACTUATOR.get(), 4),
		new ItemStack(TITANIUM.get(), 2),
		new ItemStack(PLATING.get(), 2),
		new ItemStack(FIBER_OPTICS.get(), 1),
		new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<ItemCyberlimb> CYBERARM_RIGHT = cyberLimb("cyberlimbs_cyberarm_right", BodyPartEnum.ARM_RIGHT, () -> new CyberwareProperties(
		CyberwareProperties.Rarity.NEVER, 15, 1,
		new ItemStack(ACTUATOR.get(), 4),
		new ItemStack(TITANIUM.get(), 2),
		new ItemStack(PLATING.get(), 2),
		new ItemStack(FIBER_OPTICS.get(), 1),
		new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<ItemCyberlimb> CYBERLEG_LEFT = cyberLimb("cyberlimbs_cyberleg_left", BodyPartEnum.LEG_LEFT, () -> new CyberwareProperties(
		CyberwareProperties.Rarity.NEVER, 15, 1,
		new ItemStack(ACTUATOR.get(), 4),
		new ItemStack(TITANIUM.get(), 2),
		new ItemStack(PLATING.get(), 2),
		new ItemStack(FIBER_OPTICS.get(), 1),
		new ItemStack(SYNTH_NERVES.get(), 1)
	));
	public static final RegistryObject<ItemCyberlimb> CYBERLEG_RIGHT = cyberLimb("cyberlimbs_cyberleg_right", BodyPartEnum.LEG_RIGHT, () -> new CyberwareProperties(
		CyberwareProperties.Rarity.NEVER, 15, 1,
		new ItemStack(ACTUATOR.get(), 4),
		new ItemStack(TITANIUM.get(), 2),
		new ItemStack(PLATING.get(), 2),
		new ItemStack(FIBER_OPTICS.get(), 1),
		new ItemStack(SYNTH_NERVES.get(), 1)
	));

	private static RegistryObject<ItemCyberlimb> cyberLimb(@Nonnull String name, @Nonnull BodyPartEnum slot, @Nonnull Supplier<CyberwareProperties> props)
	{
		return register(name, () -> new ItemCyberlimb(new Item.Properties().tab(CWCreativeTabs.CYBERWARE), props.get(), slot));
	}
}
