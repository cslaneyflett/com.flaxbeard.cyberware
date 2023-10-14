package flaxbeard.cyberware.common.registry.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public enum ArmorMaterials implements ArmorMaterial
{
	SHADES(
		"cyberware:vanity", 5, new int[]{1, 2, 3, 1}, 15,
		SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.GLASS)
	),
	JACKET(
		"cyberware:jacket", 5, new int[]{1, 2, 3, 1}, 15,
		SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER)
	),
	TRENCH_COAT(
		"cyberware:trench_coat", 5, new int[]{1, 2, 3, 1}, 15,
		SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER)
	);
	private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
	private final String name;
	private final int durabilityMultiplier;
	private final int[] slotProtections;
	private final int enchantmentValue;
	private final SoundEvent sound;
	private final float toughness;
	private final float knockbackResistance;
	private final LazyLoadedValue<Ingredient> repairIngredient;

	private ArmorMaterials(String pName, int pDurabilityMultiplier, int[] pSlotProtections, int pEnchantmentValue, SoundEvent pSound, float pToughness, float pKnockbackResistance, Supplier<Ingredient> pRepairIngredient)
	{
		this.name = pName;
		this.durabilityMultiplier = pDurabilityMultiplier;
		this.slotProtections = pSlotProtections;
		this.enchantmentValue = pEnchantmentValue;
		this.sound = pSound;
		this.toughness = pToughness;
		this.knockbackResistance = pKnockbackResistance;
		this.repairIngredient = new LazyLoadedValue<>(pRepairIngredient);
	}

	public int getDurabilityForSlot(EquipmentSlot pSlot)
	{
		return HEALTH_PER_SLOT[pSlot.getIndex()] * this.durabilityMultiplier;
	}

	public int getDefenseForSlot(EquipmentSlot pSlot)
	{
		return this.slotProtections[pSlot.getIndex()];
	}

	public int getEnchantmentValue()
	{
		return this.enchantmentValue;
	}

	public @Nonnull SoundEvent getEquipSound()
	{
		return this.sound;
	}

	public @Nonnull Ingredient getRepairIngredient()
	{
		return this.repairIngredient.get();
	}

	public @Nonnull String getName()
	{
		return this.name;
	}

	public float getToughness()
	{
		return this.toughness;
	}

	public float getKnockbackResistance()
	{
		return this.knockbackResistance;
	}
}