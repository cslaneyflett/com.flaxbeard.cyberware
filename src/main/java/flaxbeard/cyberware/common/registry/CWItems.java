package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.registry.items.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CWItems
{
	private CWItems()
	{
	}

	public static void init() {
		Components.init();
		BodyParts.init();
		Armors.init();
		Misc.init();

		ArmUpgrades.init();
		BoneUpgrades.init();
		BrainUpgrades.init();
		CyberLimbs.init();
		Eyes.init();
		Heart.init();
		LegUpgrades.init();
		LowerOrgans.init();
		Lungs.init();
		MuscleUpgrades.init();
		Skin.init();
	}

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Cyberware.MODID);
}
