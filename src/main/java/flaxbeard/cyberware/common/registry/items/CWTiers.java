package flaxbeard.cyberware.common.registry.items;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum CWTiers implements Tier
{
	KATANA(Tiers.IRON.getLevel(), Tiers.DIAMOND.getUses(), Tiers.DIAMOND.getSpeed(),
		Tiers.DIAMOND.getAttackDamageBonus(), Tiers.GOLD.getEnchantmentValue(),
		() -> Ingredient.of(Components.PLATING.get())
	);
	private final int level;
	private final int uses;
	private final float speed;
	private final float damage;
	private final int enchantmentValue;
	private final Supplier<Ingredient> repairIngredient;

	CWTiers(int pLevel, int pUses, float pSpeed, float pDamage, int pEnchantmentValue, Supplier<Ingredient> pRepairIngredient)
	{
		this.level = pLevel;
		this.uses = pUses;
		this.speed = pSpeed;
		this.damage = pDamage;
		this.enchantmentValue = pEnchantmentValue;
		this.repairIngredient = pRepairIngredient;
	}

	public int getUses()
	{
		return this.uses;
	}

	public float getSpeed()
	{
		return this.speed;
	}

	public float getAttackDamageBonus()
	{
		return this.damage;
	}

	public int getLevel()
	{
		return this.level;
	}

	public int getEnchantmentValue()
	{
		return this.enchantmentValue;
	}

	@Nonnull
	public Ingredient getRepairIngredient()
	{
		return this.repairIngredient.get();
	}

	@Nullable
	public TagKey<Block> getTag() {return null;}
}
