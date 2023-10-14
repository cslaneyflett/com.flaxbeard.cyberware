package flaxbeard.cyberware.common.registry.items;

import flaxbeard.cyberware.api.item.ICyberware.BodyPartEnum;
import flaxbeard.cyberware.common.item.ItemBodyPart;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.CyberwareProperties.Rarity;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class BodyParts extends ItemRegistry
{
	private BodyParts()
	{
	}

	public static void init()
	{
		// controls static class init
	}

	public static final RegistryObject<Item> EYES = bodyPart("body_part_eyes", BodyPartEnum.EYES);
	public static final RegistryObject<Item> BRAIN = bodyPart("body_part_brain", BodyPartEnum.BRAIN);
	public static final RegistryObject<Item> HEART = bodyPart("body_part_heart", BodyPartEnum.HEART);
	public static final RegistryObject<Item> LUNGS = bodyPart("body_part_lungs", BodyPartEnum.LUNGS);
	public static final RegistryObject<Item> STOMACH = bodyPart("body_part_stomach", BodyPartEnum.STOMACH);
	public static final RegistryObject<Item> SKIN = bodyPart("body_part_skin", BodyPartEnum.SKIN);
	public static final RegistryObject<Item> MUSCLES = bodyPart("body_part_muscles", BodyPartEnum.MUSCLES);
	public static final RegistryObject<Item> BONES = bodyPart("body_part_bones", BodyPartEnum.BONES);
	public static final RegistryObject<Item> ARM_LEFT = bodyPart("body_part_arm_left", BodyPartEnum.ARM_LEFT);
	public static final RegistryObject<Item> ARM_RIGHT = bodyPart("body_part_arm_right", BodyPartEnum.ARM_RIGHT);
	public static final RegistryObject<Item> LEG_LEFT = bodyPart("body_part_leg_left", BodyPartEnum.LEG_LEFT);
	public static final RegistryObject<Item> LEG_RIGHT = bodyPart("body_part_leg_right", BodyPartEnum.LEG_RIGHT);

	private static RegistryObject<Item> bodyPart(@Nonnull String name, @Nonnull BodyPartEnum part)
	{
		return register(name, () -> new ItemBodyPart(
			new Item.Properties(), new CyberwareProperties(Rarity.NEVER, 0, 1),
			part
		));
	}
}
