package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberwareTabItem;
import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.CyberwareContent.ZombieItem;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.util.random.Weight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemCyberware extends ItemCyberwareBase implements ICyberware, ICyberwareTabItem, IDeconstructable
{
	private final EnumSlot[] slots;
	private int[] essence;
	private NonNullList<NonNullList<ItemStack>> components;

	public ItemCyberware(String name, EnumSlot[] slots, String[] subnames)
	{
		super(name, subnames);

		this.slots = slots;

		this.essence = new int[subnames.length + 1];
		this.components = NonNullList.create();
	}

	public ItemCyberware(String name, EnumSlot slot, String[] subnames)
	{
		this(name, new EnumSlot[]{slot}, subnames);
	}

	public ItemCyberware(String name, EnumSlot slot)
	{
		this(name, slot, new String[0]);
	}

	public ItemCyberware setWeights(Integer... raw)
	{
		assert raw.length == Math.max(1, subnames.length);
		List<Weight> weight = Arrays.stream(raw)
			.map(Weight::of)
			.toList();

		for (int meta = 0; meta < weight.size(); meta++)
		{
			ItemStack stack = new ItemStack(this, 1, CyberwareItemMetadata.of(meta));
			int installedStackSize = installedStackSize(stack);
			stack.setCount(installedStackSize);
			this.setQuality(stack, CyberwareAPI.QUALITY_SCAVENGED);
			CyberwareContent.zombieItems.add(new ZombieItem(weight.get(meta), stack));
		}

		return this;
	}

	public ItemCyberware setEssenceCost(int... essence)
	{
		assert essence.length == Math.max(1, subnames.length);
		this.essence = essence;
		return this;
	}

	@SafeVarargs
	public final ItemCyberware setComponents(NonNullList<ItemStack>... components)
	{
		assert components.length == Math.max(1, subnames.length);
		NonNullList<NonNullList<ItemStack>> list = NonNullList.create();
		Collections.addAll(list, components);
		this.components = list;
		return this;
	}

	@Override
	public int getEssenceCost(ItemStack stack)
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
		return essence[Math.min(this.subnames.length, CyberwareItemMetadata.get(stack))];
	}

	@Override
	public EnumSlot getSlot(ItemStack stack)
	{
		return slots[Math.min(slots.length - 1, CyberwareItemMetadata.get(stack))];
	}

	@Override
	public int installedStackSize(ItemStack stack)
	{
		return 1;
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return false;
	}

	@Override
	public boolean isEssential(ItemStack stack)
	{
		return false;
	}

	@Override
	public List<String> getInfo(ItemStack stack)
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
		String[] toReturnArray = I18n.get("cyberware.tooltip." + this.getRegistryName().toString().substring(10)
			+ (this.subnames.length > 0 ? "." + CyberwareItemMetadata.get(stack) :
			"")).split("\\\\n");
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

		if (installedStackSize(stack) > 1)
		{
			toReturn.add(ChatFormatting.BLUE + I18n.get("cyberware.tooltip.max_install", installedStackSize(stack)));
		}

		boolean hasPowerConsumption = false;
		StringBuilder toAddPowerConsumption = new StringBuilder();
		for (int i = 0; i < installedStackSize(stack); i++)
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
			String toTranslate = hasCustomPowerMessage(stack) ?
				"cyberware.tooltip." + this.getRegistryName().toString().substring(10)
					+ (this.subnames.length > 0 ? "." + CyberwareItemMetadata.get(stack) : "") +
					".power_consumption"
				:
					"cyberware.tooltip.power_consumption";
			toReturn.add(ChatFormatting.GREEN + I18n.get(toTranslate, toAddPowerConsumption.toString()));
		}

		boolean hasPowerProduction = false;
		StringBuilder toAddPowerProduction = new StringBuilder();
		for (int i = 0; i < installedStackSize(stack); i++)
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
			String toTranslate = hasCustomPowerMessage(stack) ?
				"cyberware.tooltip." + this.getRegistryName().toString().substring(10)
					+ (this.subnames.length > 0 ? "." + CyberwareItemMetadata.get(stack) : "") +
					".power_production"
				:
					"cyberware.tooltip.power_production";
			toReturn.add(ChatFormatting.GREEN + I18n.get(toTranslate, toAddPowerProduction.toString()));
		}

		if (getCapacity(stack) > 0)
		{
			String toTranslate = hasCustomCapacityMessage(stack) ?
				"cyberware.tooltip." + this.getRegistryName().toString().substring(10)
					+ (this.subnames.length > 0 ? "." + CyberwareItemMetadata.get(stack) : "") +
					".capacity"
				:
					"cyberware.tooltip.capacity";
			toReturn.add(ChatFormatting.GREEN + I18n.get(toTranslate, getCapacity(stack)));
		}


		boolean hasEssenceCost = false;
		boolean essenceCostNegative = true;
		String toAddEssence = "";
		for (int i = 0; i < installedStackSize(stack); i++)
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
				toAddEssence += I18n.get("cyberware.tooltip.joiner");
			}

			toAddEssence += " " + Math.abs(cost);
		}

		if (hasEssenceCost)
		{
			toReturn.add(ChatFormatting.DARK_PURPLE + I18n.get(essenceCostNegative ? "cyberware.tooltip.essence" :
				"cyberware.tooltip.essence_add", toAddEssence));
		}


		return toReturn;
	}

	public int getPowerConsumption(ItemStack stack)
	{
		return 0;
	}

	public int getPowerProduction(ItemStack stack)
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
	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
	{
		return NonNullList.create();
	}

	@Override
	public EnumCategory getCategory(ItemStack stack)
	{
		return EnumCategory.values()[this.getSlot(stack).ordinal()];
	}

	@Override
	public int getCapacity(ItemStack wareStack)
	{
		return 0;
	}

	@Override
	public void onAdded(LivingEntity entityLivingBase, ItemStack stack)
	{
		// no operation
	}

	@Override
	public void onRemoved(LivingEntity entityLivingBase, ItemStack stack)
	{
		// no operation
	}

	@Override
	public boolean canDestroy(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) < this.components.size();
	}

	@Override
	public NonNullList<ItemStack> getComponents(ItemStack stack)
	{
		return components.get(Math.min(this.components.size() - 1, CyberwareItemMetadata.get(stack)));
	}

	@Override
	public Quality getQuality(ItemStack stack)
	{
		Quality q = CyberwareAPI.getQualityTag(stack);

		if (q == null) return CyberwareAPI.QUALITY_MANUFACTURED;

		return q;
	}

	@Override
	public ItemStack setQuality(ItemStack stack, Quality quality)
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
	public String getItemStackDisplayName(ItemStack stack)
	{
		Quality q = getQuality(stack);

		if (q != null && q.getNameModifier() != null)
		{
			return I18n.get(q.getNameModifier(), ("" + I18n.get(this.getTranslationKey(stack) + ".name")).trim()).trim();
		}

		return ("" + I18n.get(this.getTranslationKey(stack) + ".name")).trim();
	}

	@Override
	public boolean canHoldQuality(ItemStack stack, Quality quality)
	{
		return true;
	}
}
