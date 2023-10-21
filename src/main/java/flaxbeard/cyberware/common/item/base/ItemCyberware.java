package flaxbeard.cyberware.common.item.base;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberwareTabItem;
import flaxbeard.cyberware.api.item.IDeconstructable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemCyberware extends Item implements ICyberware, ICyberwareTabItem, IDeconstructable
{
	public final @Nonnull CyberwareProperties cyberwareProperties;
	public final @Nonnull BodyRegionEnum bodyRegionEnum;

	public ItemCyberware(@Nonnull Properties itemProperties, @Nonnull CyberwareProperties cyberwareProperties,
						 @Nonnull BodyRegionEnum bodyRegionEnum)
	{
		super(itemProperties);

		this.cyberwareProperties = cyberwareProperties;
		this.bodyRegionEnum = bodyRegionEnum;

		if (cyberwareProperties.rarity() != CyberwareProperties.Rarity.NEVER)
		{
			// TODO: item stack cant be made here, stick ourself into a list elsewhere and do the build in a later event
			//var stack = new ItemStack(this);
			//int maximumStackSize = maximumStackSize(stack);
			//stack.setCount(maximumStackSize);
			//this.setQuality(stack, CyberwareAPI.QUALITY_SCAVENGED);

			//ZombieItem.add(cyberwareProperties.rarity(), stack);
		}
	}

	@Override
	public int getEssenceCost(@Nonnull ItemStack stack)
	{
		int cost = getUnmodifiedEssenceCost(stack);
		if (getQuality(stack) == CyberwareAPI.QUALITY_SCAVENGED)
		{
			float half = cost / 2F;
			if (cost > 0)
			{
				cost = cost + (int) Math.ceil(half);
			} else
			{
				cost = cost - (int) Math.ceil(half);
			}
		}
		return cost;
	}

	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		return cyberwareProperties.essenceCost() * Math.min(stack.getCount(), maximumStackSize(stack));
	}

	@Override
	public @Nonnull BodyRegionEnum getSlot(@Nonnull ItemStack stack)
	{
		return this.bodyRegionEnum;
	}

	@Override
	public int maximumStackSize(@Nonnull ItemStack stack)
	{
		return this.cyberwareProperties.maxStack();
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return false;
	}

	@Override
	public boolean isEssential(@Nonnull ItemStack stack)
	{
		return false;
	}

	@Override
	public @Nonnull List<String> getInfo(@Nonnull ItemStack stack)
	{
		List<String> ret = new ArrayList<>();
		List<String> desc = this.getDescription(stack);
		if (desc != null && !desc.isEmpty())
		{
			ret.addAll(desc);
		}
		return ret;
	}

	public List<String> getStackDesc(ItemStack stack)
	{
		var raw = this.cyberwareProperties.tooltip(CyberwareProperties.TooltipType.MAIN);
		String[] toReturnArray = raw != null ? I18n.get(raw).split("\\\\n") : new String[]{};
		List<String> toReturn = new ArrayList<>(Arrays.asList(toReturnArray));

		if (!toReturn.isEmpty() && toReturn.get(0).isEmpty())
		{
			toReturn.remove(0);
		}

		return toReturn;
	}

	public List<String> getDescription(ItemStack stack)
	{
		List<String> toReturn = getStackDesc(stack);

		if (maximumStackSize(stack) > 1)
		{
			toReturn.add(ChatFormatting.BLUE + I18n.get("cyberware.tooltip.max_install", maximumStackSize(stack)));
		}

		boolean hasPowerConsumption = false;
		StringBuilder toAddPowerConsumption = new StringBuilder();
		for (int i = 0; i < maximumStackSize(stack); i++)
		{
			ItemStack temp = stack.copy();
			temp.setCount(i + 1);
			int cost = this.getPowerConsumption(temp);
			if (cost > 0)
			{
				hasPowerConsumption = true;
			}

			if (i != 0)
			{
				toAddPowerConsumption.append(I18n.get("cyberware.tooltip.joiner"));
			}

			toAddPowerConsumption.append(" ").append(cost);
		}

		if (hasPowerConsumption)
		{
			String toTranslate = hasCustomPowerMessage(stack)
				? this.cyberwareProperties.tooltip(CyberwareProperties.TooltipType.POWER_CONSUMPTION)
				: "cyberware.tooltip.power_consumption";

			toReturn.add(ChatFormatting.GREEN + I18n.get(toTranslate, toAddPowerConsumption.toString()));
		}

		boolean hasPowerProduction = false;
		StringBuilder toAddPowerProduction = new StringBuilder();
		for (int i = 0; i < maximumStackSize(stack); i++)
		{
			ItemStack temp = stack.copy();
			temp.setCount(i + 1);
			int cost = this.getPowerProduction(temp);
			if (cost > 0)
			{
				hasPowerProduction = true;
			}

			if (i != 0)
			{
				toAddPowerProduction.append(I18n.get("cyberware.tooltip.joiner"));
			}

			toAddPowerProduction.append(" ").append(cost);
		}

		if (hasPowerProduction)
		{
			String toTranslate = hasCustomPowerMessage(stack)
				? this.cyberwareProperties.tooltip(CyberwareProperties.TooltipType.POWER_PRODUCTION)
				: "cyberware.tooltip.power_production";

			toReturn.add(ChatFormatting.GREEN + I18n.get(toTranslate, toAddPowerProduction.toString()));
		}

		if (getPowerCapacity(stack) > 0)
		{
			String toTranslate = hasCustomCapacityMessage(stack)
				? this.cyberwareProperties.tooltip(CyberwareProperties.TooltipType.POWER_CAPACITY)
				: "cyberware.tooltip.capacity";

			toReturn.add(ChatFormatting.GREEN + I18n.get(toTranslate, getPowerCapacity(stack)));
		}


		boolean hasEssenceCost = false;
		boolean essenceCostNegative = true;
		StringBuilder toAddEssence = new StringBuilder();
		for (int i = 0; i < maximumStackSize(stack); i++)
		{
			ItemStack temp = stack.copy();
			temp.setCount(i + 1);
			int cost = this.getEssenceCost(temp);
			if (cost != 0)
			{
				hasEssenceCost = true;
			}
			if (cost < 0)
			{
				essenceCostNegative = false;
			}

			if (i != 0)
			{
				toAddEssence.append(I18n.get("cyberware.tooltip.joiner"));
			}

			toAddEssence.append(" ").append(Math.abs(cost));
		}

		if (hasEssenceCost)
		{
			toReturn.add(ChatFormatting.DARK_PURPLE + I18n.get(
				essenceCostNegative ? "cyberware.tooltip.essence" : "cyberware.tooltip.essence_add",
				toAddEssence.toString()
			));
		}


		return toReturn;
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return 0;
	}

	@Override
	public int getPowerProduction(@Nonnull ItemStack stack)
	{
		return 0;
	}

	@Override
	public int getPowerCapacity(@Nonnull ItemStack stack)
	{
		return 0;
	}

	public boolean hasCustomPowerMessage(ItemStack stack)
	{
		return false;
	}

	public boolean hasCustomCapacityMessage(ItemStack stack)
	{
		return false;
	}

	@Override
	public @Nonnull NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack)
	{
		return NonNullList.create();
	}

	@Override
	public EnumCategory getCategory(ItemStack stack)
	{
		return EnumCategory.values()[this.getSlot(stack).ordinal()];
	}

	@Override
	public void onAdded(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
	{
		// no operation
	}

	@Override
	public void onRemoved(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
	{
		// no operation
	}

	@Override
	public boolean canDestroy(@Nonnull ItemStack stack)
	{
		// TODO: weird original code
		return true;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getComponents(@Nonnull ItemStack stack)
	{
		return this.cyberwareProperties.componentList();
	}

	@Nonnull
	@Override
	public Quality getQuality(@Nonnull ItemStack stack)
	{
		Quality q = CyberwareAPI.getQualityTag(stack);

		if (q == null) return CyberwareAPI.QUALITY_MANUFACTURED;

		return q;
	}

	@Nonnull
	@Override
	public ItemStack setQuality(@Nonnull ItemStack stack, @Nonnull Quality quality)
	{
		if (quality == CyberwareAPI.QUALITY_MANUFACTURED)
		{
			if (!stack.isEmpty() && stack.hasTag())
			{
				assert stack.getTag() != null;
				stack.getTag().remove(CyberwareAPI.QUALITY_TAG);
				if (stack.getTag().isEmpty())
				{
					stack.setTag(null);
				}
			}
			return stack;
		}
		return this.canHoldQuality(stack, quality) ? CyberwareAPI.writeQualityTag(stack, quality) : stack;
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	@Override
	public Component getName(@Nonnull ItemStack stack)
	{
		var name = super.getName(stack);
		var q = getQuality(stack);

		if (q.getNameModifier() != null)
		{
			return Component.translatable(q.getNameModifier(), name);
		}

		return name;
	}

	@Override
	public boolean canHoldQuality(@Nonnull ItemStack stack, @Nonnull Quality quality)
	{
		return true;
	}
}
