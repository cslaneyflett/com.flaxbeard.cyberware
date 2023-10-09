package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemBlueprint;
import flaxbeard.cyberware.common.item.ItemExpCapsule;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Misc extends ItemRegistry
{
	public static final RegistryObject<Item> EXP_CAPSULE =
		register("exp_capsule", () -> new ItemExpCapsule(new Item.Properties()));

	public static final RegistryObject<Item> BLUEPRINT =
		register("blueprint", () -> new ItemBlueprint(new Item.Properties()));
}
