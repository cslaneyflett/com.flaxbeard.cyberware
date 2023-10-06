package flaxbeard.cyberware.api.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ICyberware
{
	public EnumSlot getSlot(ItemStack stack);

	public int installedStackSize(ItemStack stack);

	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack);

	public boolean isIncompatible(ItemStack stack, ItemStack comparison);

	boolean isEssential(ItemStack stack);

	public List<String> getInfo(ItemStack stack);

	public int getCapacity(ItemStack wareStack);

	/**
	 * Returns a Quality object representing the quality of this stack - all
	 * changes that this Quality has to function must be handled internally,
	 * this is just for the tooltip and external factors. See CyberwareAPI for
	 * the base Qualities.
	 *
	 * @param stack The ItemStack to check
	 * @return An instance of Quality
	 */
	public Quality getQuality(ItemStack stack);

	public ItemStack setQuality(ItemStack stack, Quality quality);

	public boolean canHoldQuality(ItemStack stack, Quality quality);

	public class Quality
	{
		private static Map<String, Quality> mapping = new HashMap<>();
		public static List<Quality> qualities = new ArrayList<>();
		private String unlocalizedName;
		private String nameModifier;
		private String spriteSuffix;

		public Quality(String unlocalizedName)
		{
			this(unlocalizedName, null, null);
		}

		public Quality(String unlocalizedName, String nameModifier, String spriteSuffix)
		{
			this.unlocalizedName = unlocalizedName;
			this.nameModifier = nameModifier;
			this.spriteSuffix = spriteSuffix;

			mapping.put(unlocalizedName, this);
			qualities.add(this);
		}

		public String getUnlocalizedName()
		{
			return unlocalizedName;
		}

		public static Quality getQualityFromString(String name)
		{
			if (mapping.containsKey(name))
			{
				return mapping.get(name);
			}
			return null;
		}

		public String getNameModifier()
		{
			return nameModifier;
		}

		public String getSpriteSuffix()
		{
			return spriteSuffix;
		}
	}

	// @TODO rename to BodyRegion since it's more a type/category than an actual inventory slot
	public enum EnumSlot
	{
		EYES(12, "eyes"),
		CRANIUM(11, "cranium"),
		HEART(14, "heart"),
		LUNGS(15, "lungs"),
		LOWER_ORGANS(17, "lower_organs"),
		SKIN(18, "skin"),
		MUSCLE(19, "muscle"),
		BONE(20, "bone"),
		ARM(21, "arm", true, true),
		HAND(22, "hand", true, false),
		LEG(23, "leg", true, true),
		FOOT(24, "foot", true, false);
		private final int slotNumber;
		private final String name;
		private final boolean sidedSlot;
		private final boolean hasEssential;

		private EnumSlot(int slot, String name, boolean sidedSlot, boolean hasEssential)
		{
			this.slotNumber = slot;
			this.name = name;
			this.sidedSlot = sidedSlot;
			this.hasEssential = hasEssential;
		}

		private EnumSlot(int slot, String name)
		{
			this(slot, name, false, true);
		}

		public int getSlotNumber()
		{
			return slotNumber;
		}

		public static EnumSlot getSlotByPage(int page)
		{
			for (EnumSlot slot : values())
			{
				if (slot.getSlotNumber() == page)
				{
					return slot;
				}
			}
			return null;
		}

		public String getName()
		{
			return name;
		}

		public boolean isSided()
		{
			return sidedSlot;
		}

		public boolean hasEssential()
		{
			return hasEssential;
		}
	}

	public void onAdded(LivingEntity entityLivingBase, ItemStack stack);

	public void onRemoved(LivingEntity entityLivingBase, ItemStack stack);

	public interface ISidedLimb
	{
		public EnumSide getSide(ItemStack stack);

		public enum EnumSide
		{
			LEFT,
			RIGHT;
		}
	}

	public int getEssenceCost(ItemStack stack);
}
