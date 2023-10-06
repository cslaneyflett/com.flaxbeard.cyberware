package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import net.minecraft.world.item.ItemStack;

public class ItemBodyPart extends ItemCyberware implements ISidedLimb
{
	public ItemBodyPart(String name, EnumSlot[] slots, String[] subnames)
	{
		super(name, slots, subnames);
	}

	@Override
	public boolean isEssential(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getEssenceCost(ItemStack stack)
	{
		return 0;
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		// TODO: what the fuck is this magic number?
		if (CyberwareItemMetadata.predicate(stack, (int t) -> t <= 7))
		{
			return CyberwareAPI.getCyberware(other).isEssential(other);
		}

		ICyberware ware = CyberwareAPI.getCyberware(other);

		if (ware instanceof ISidedLimb)
		{
			return ware.isEssential(other) && ((ISidedLimb) ware).getSide(other) == this.getSide(stack);
		}

		return false;
	}

	@Override
	public EnumCategory getCategory(ItemStack stack)
	{
		return EnumCategory.BODY_PARTS;
	}

	@Override
	public EnumSide getSide(ItemStack stack)
	{
		return CyberwareItemMetadata.predicate(stack, (int t) -> t % 2 == 0) ? EnumSide.LEFT : EnumSide.RIGHT;
	}

	@Override
	public Quality getQuality(ItemStack stack)
	{
		return null;
	}

	@Override
	public boolean canHoldQuality(ItemStack stack, Quality quality)
	{
		return false;
	}
}
