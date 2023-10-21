package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.config.DefaultConfig;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CWAttributes
{
	private CWAttributes()
	{
	}

	public static final DeferredRegister<Attribute> ATTRIBUTES =
		DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Cyberware.MODID);
	public static final RegistryObject<Attribute> TOLERANCE = ATTRIBUTES.register(
		"essence",
		() -> new RangedAttribute("attribute.cyberware.tolerance",
			DefaultConfig.ESSENCE, // CyberwareConfig.INSTANCE.ESSENCE.get() TODO config not loaded
			0.0F, Double.MAX_VALUE
		)
	);
}
