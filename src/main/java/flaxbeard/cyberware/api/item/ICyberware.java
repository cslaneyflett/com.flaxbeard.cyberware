package flaxbeard.cyberware.api.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ICyberware
{
	@Nonnull
	BodyRegionEnum getSlot(@Nonnull ItemStack stack);

	int maximumStackSize(@Nonnull ItemStack stack);

	@Nonnull
	NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack);

	boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack comparison);

	boolean isEssential(@Nonnull ItemStack stack);

	@Nonnull
	List<String> getInfo(@Nonnull ItemStack stack);

	int getPowerConsumption(@Nonnull ItemStack stack);

	int getPowerProduction(@Nonnull ItemStack stack);

	int getPowerCapacity(@Nonnull ItemStack stack);

	/**
	 * Returns a Quality object representing the quality of this stack - all
	 * changes that this Quality has to function must be handled internally,
	 * this is just for the tooltip and external factors. See CyberwareAPI for
	 * the base Qualities.
	 *
	 * @param stack The ItemStack to check
	 * @return An instance of Quality
	 */
	@Nonnull
	Quality getQuality(@Nonnull ItemStack stack);

	@Nonnull
	ItemStack setQuality(@Nonnull ItemStack stack, @Nonnull Quality quality);

	boolean canHoldQuality(@Nonnull ItemStack stack, @Nonnull Quality quality);

	class Quality
	{
		private static final Map<String, Quality> mapping = new HashMap<>();
		public static final List<Quality> qualities = new ArrayList<>();
		private final String unlocalizedName;
		private final String nameModifier;
		private final String spriteSuffix;

		public Quality(@Nonnull String unlocalizedName)
		{
			this(unlocalizedName, null, null);
		}

		public Quality(@Nonnull String unlocalizedName, String nameModifier, String spriteSuffix)
		{
			this.unlocalizedName = unlocalizedName;
			this.nameModifier = nameModifier;
			this.spriteSuffix = spriteSuffix;

			mapping.put(unlocalizedName, this);
			qualities.add(this);
		}

		public @Nonnull String getUnlocalizedName()
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

	enum BodyRegionEnum
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

		BodyRegionEnum(int slot, String name, boolean sidedSlot, boolean hasEssential)
		{
			this.slotNumber = slot;
			this.name = name;
			this.sidedSlot = sidedSlot;
			this.hasEssential = hasEssential;
		}

		BodyRegionEnum(int slot, String name)
		{
			this(slot, name, false, true);
		}

		public int getSlotNumber()
		{
			return slotNumber;
		}

		public static BodyRegionEnum getSlotByPage(int page)
		{
			for (BodyRegionEnum slot : values())
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

	enum BodyPartEnum
	{
		EYES(BodyRegionEnum.EYES),
		BRAIN(BodyRegionEnum.CRANIUM),
		HEART(BodyRegionEnum.HEART),
		LUNGS(BodyRegionEnum.LUNGS),
		STOMACH(BodyRegionEnum.LOWER_ORGANS),
		SKIN(BodyRegionEnum.SKIN),
		MUSCLES(BodyRegionEnum.MUSCLE),
		BONES(BodyRegionEnum.BONE),
		ARM_LEFT(BodyRegionEnum.ARM, ISidedLimb.EnumSide.LEFT),
		ARM_RIGHT(BodyRegionEnum.ARM, ISidedLimb.EnumSide.RIGHT),
		LEG_LEFT(BodyRegionEnum.LEG, ISidedLimb.EnumSide.LEFT),
		LEG_RIGHT(BodyRegionEnum.LEG, ISidedLimb.EnumSide.RIGHT);
		public final @Nonnull BodyRegionEnum slot;
		public final @Nullable ISidedLimb.EnumSide side;

		BodyPartEnum(@Nonnull BodyRegionEnum bodyRegionEnum)
		{
			this.slot = bodyRegionEnum;
			this.side = null;
		}

		BodyPartEnum(@Nonnull BodyRegionEnum bodyRegionEnum, @Nullable ISidedLimb.EnumSide side)
		{
			this.slot = bodyRegionEnum;
			this.side = side;
		}
	}

	void onAdded(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack);

	void onRemoved(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack);

	interface ISidedLimb extends ICyberware
	{
		@Nonnull
		EnumSide getSide(@Nonnull ItemStack stack);

		enum EnumSide
		{
			LEFT,
			RIGHT;
		}
	}

	int getEssenceCost(@Nonnull ItemStack stack);
}
