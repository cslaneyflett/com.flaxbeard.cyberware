package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.common.item.ItemBlueprint;
import flaxbeard.cyberware.common.item.ItemExpCapsule;
import flaxbeard.cyberware.common.item.ItemSwordCyberware;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Misc extends ItemRegistry
{
	private Misc()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<ItemExpCapsule> EXP_CAPSULE =
		register("exp_capsule", () -> new ItemExpCapsule(new Item.Properties()));
	public static final RegistryObject<ItemBlueprint> BLUEPRINT =
		register("blueprint", () -> new ItemBlueprint(new Item.Properties()));
	public static final RegistryObject<ItemSwordCyberware> KATANA =
		register("katana", () -> new ItemSwordCyberware(CWTiers.KATANA, 0, 0.0F, new Item.Properties()));
}
