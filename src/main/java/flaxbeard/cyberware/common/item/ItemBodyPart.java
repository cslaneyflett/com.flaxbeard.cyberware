package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBodyPart extends ItemCyberware implements ISidedLimb
{
	public final @Nonnull BodyPartEnum bodyPart;

	public ItemBodyPart(Properties properties, CyberwareProperties cyberwareProperties, BodyPartEnum bodyPart)
	{
		super(properties, cyberwareProperties, bodyPart.slot);
		this.bodyPart = bodyPart;
	}

	@Override
	public boolean isEssential(@Nonnull ItemStack stack)
	{
		return this.bodyPart.slot.hasEssential();
	}

	@Override
	public int getEssenceCost(@Nonnull ItemStack stack)
	{
		return 0;
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		var thisWare = (ItemBodyPart) CyberwareAPI.getCyberware(stack);
		var otherWare = CyberwareAPI.getCyberware(other);

		if (thisWare.isEssential(stack))
		{
			return otherWare.isEssential(other);
		}

		if (otherWare instanceof ISidedLimb sidedWare)
		{
			return sidedWare.isEssential(other) && sidedWare.getSide(other) == this.getSide(stack);
		}

		return false;
	}

	@Override
	public EnumCategory getCategory(ItemStack stack)
	{
		return EnumCategory.BODY_PARTS;
	}

	@Nonnull
	@Override
	public EnumSide getSide(@Nonnull ItemStack stack)
	{
		// TODO: Potential issue, not all BodyParts have sides
		return this.bodyPart.side;
	}

	// TODO: i don't think this is correct, but im enforcing nonnull on quality now
	// and manufactured has no prefix, its not like you have scavenged organs... right?
	@Nonnull
	@Override
	public Quality getQuality(@Nonnull ItemStack stack)
	{
		return CyberwareAPI.QUALITY_MANUFACTURED;
	}

	@Override
	public boolean canHoldQuality(@Nonnull ItemStack stack, @Nonnull Quality quality)
	{
		return false;
	}
}
