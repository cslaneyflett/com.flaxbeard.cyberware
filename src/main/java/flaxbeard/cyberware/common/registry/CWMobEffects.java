package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.effect.PotionNeuropozyne;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CWMobEffects
{
	private CWMobEffects()
	{
	}

	public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Cyberware.MODID);
	public static final RegistryObject<MobEffect> NEUROPOZYNE = MOB_EFFECTS.register("neuropozyne", () ->
		new PotionNeuropozyne(MobEffectCategory.BENEFICIAL, 0x47453d, 0));
	public static final RegistryObject<MobEffect> REJECTION = MOB_EFFECTS.register("rejection", () ->
		new PotionNeuropozyne(MobEffectCategory.HARMFUL, 0xFF0000, 1));
}
