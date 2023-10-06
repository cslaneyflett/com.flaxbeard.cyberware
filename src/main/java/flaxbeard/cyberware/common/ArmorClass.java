package flaxbeard.cyberware.common;

import com.google.common.collect.Multimap;
import flaxbeard.cyberware.Cyberware;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map.Entry;

public enum ArmorClass
{
	NONE(),
	LIGHT(),
	HEAVY;
	static boolean enableLogging = false;
	static long timeLastLog_ms;
	static int maxEntityArmor = 10;
	static final EquipmentSlot[] armorSlots = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST,
		EquipmentSlot.HEAD};
	static final double[] maxPartArmors = {1.5D, 3.0D, 4.0D, 1.5D};

	public static boolean isWearingLightOrNone(LivingEntity entityLivingBase)
	{
		return get(entityLivingBase) != HEAVY;
	}

	public static ArmorClass get(@Nonnull LivingEntity entityLivingBase)
	{
		// development support
		// Boosted leather chestplate is heavy
		// /give xxx leather_chestplate 1 0 {AttributeModifiers:[{UUIDMost: 436328, UUIDLeast: 436329, Amount: 6,
		// Slot: "chest", AttributeName: "generic.armor", Operation: 0, Name: "generic.armor"}]}
		// Nerfed diamond chestplate is heavy
		// /give xxx diamond_chestplate 1 0 {AttributeModifiers:[{UUIDMost: 436328, UUIDLeast: 436329, Amount: 1,
		// Slot: "chest", AttributeName: "generic.armor", Operation: 0, Name: "generic.armor"}]}
		// Nerfed leather leggings is light
		// /give xxx leather_leggings 1 0 {AttributeModifiers:[{UUIDMost: 436328, UUIDLeast: 436329, Amount: 1, Slot:
		// "legs", AttributeName: "generic.armor", Operation: 0, Name: "generic.armor"}]}
		// Boosted leather boots is heavy
		// /give xxx leather_boots 1 0 {AttributeModifiers:[{UUIDMost: 436328, UUIDLeast: 436329, Amount: 4, Slot:
		// "feet", AttributeName: "generic.armor", Operation: 0, Name: "generic.armor"}]}
		boolean isLogging = false;
		if (enableLogging)
		{
			final long timeCurrent_ms = System.currentTimeMillis();
			if (timeCurrent_ms > timeLastLog_ms + 2000)
			{
				timeLastLog_ms = timeCurrent_ms;
				isLogging = true;
			}
		}

		// fast check for heavy armor
		final int entityArmor = entityLivingBase.getArmorValue();
		if (entityArmor > maxEntityArmor)
		{
			if (isLogging)
			{
				Cyberware.logger.warn(String.format("Total armor %d is greater than %d => this is HEAVY armor",
					entityArmor, maxEntityArmor
				));
			}
			return HEAVY;
		}

		// slow check per armor part
		boolean hasNoArmor = true;
		for (final EquipmentSlot entityEquipmentSlot : armorSlots)
		{
			// skip empty slots
			final ItemStack itemStack = entityLivingBase.getItemBySlot(entityEquipmentSlot);
			if (itemStack.isEmpty()) continue;

			hasNoArmor = false;
			final double maxPartArmor = maxPartArmors[entityEquipmentSlot.getIndex()];

			// caps on forge absorption
			//			if (itemStack.getItem() instanceof ISpecialArmor itemArmor) {
			//				final ArmorProperties armorProperties = itemArmor.getProperties(entityLivingBase,
			//				itemStack, DamageSource.CACTUS, 1.0D, 1);
			//				if (armorProperties.AbsorbRatio * 25.0D > maxPartArmor) {
			//					if (isLogging) {
			//						Cyberware.logger.warn(String.format("ISpecialArmor absorption %.1f is greater than
			//						%.1f (%.1f) => this is HEAVY armor",
			//								armorProperties.AbsorbRatio, 25.0D / maxPartArmor, maxPartArmor));
			//					}
			//					return HEAVY;
			//				}
			//			}

			// caps on vanilla armor
			if (itemStack.getItem() instanceof ArmorItem itemArmor)
			{
				final int defenseChestplate = itemArmor.getMaterial().getDefenseForSlot(EquipmentSlot.CHEST);
				final double maxChestplateArmor = maxPartArmors[EquipmentSlot.CHEST.getIndex()];
				if (defenseChestplate > maxChestplateArmor)
				{
					if (isLogging)
					{
						Cyberware.logger.warn(String.format("ArmorItem material chestplate armor %d is greater then %" +
								".1f => this is HEAVY armor",
							defenseChestplate, maxChestplateArmor
						));
					}
					return HEAVY;
				}
			}

			// caps on attributes
			// note: modded armor can change attributes without using NBT, see Vampirism Armor of Swiftness
			// note: the ISpecialArmor check might be already covered by this, but not the other way around. 
			final Multimap<Attribute, AttributeModifier> attributeModifiers =
				itemStack.getAttributeModifiers(entityEquipmentSlot);
			for (final Entry<Attribute, AttributeModifier> entry : attributeModifiers.entries())
			{
				if (!entry.getKey().equals(Attributes.ARMOR))
				{
					continue;
				}

				final double armorValue = entry.getValue().getAmount();
				if (armorValue > maxPartArmor)
				{
					if (isLogging)
					{
						Cyberware.logger.warn(String.format("Armor attribute %.1f is greater then %.1f => this is " +
								"HEAVY armor",
							armorValue, maxPartArmor
						));
					}
					return HEAVY;
				}
			}
		}

		if (isLogging)
		{
			Cyberware.logger.warn(String.format(
				"No heavy armor detected, hasNoArmor is %s",
				hasNoArmor
			));
		}

		return hasNoArmor ? NONE : LIGHT;
	}
}