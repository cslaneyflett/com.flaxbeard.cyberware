package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.misc.NNLUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class ItemSwordCyberware extends SwordItem implements IDeconstructable
{
	public ItemSwordCyberware(String name, Tier tier)
	{
		// int pAttackDamageModifier, float pAttackSpeedModifier, Item.Properties pProperties
		super(tier);

		setRegistryName(name);
		// ForgeRegistries.ITEMS.register(this);
		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);

		CyberwareContent.items.add(this);
	}

	@Override
	public boolean canDestroy(ItemStack stack)
	{
		return true;
	}

	@Override
	public NonNullList<ItemStack> getComponents(ItemStack stack)
	{
		// TODO: magic numbers
		return NNLUtil.fromArray
			(new ItemStack[]{
				new ItemStack(Items.IRON_INGOT, 2, CyberwareItemMetadata.of(0)),
				new ItemStack(CyberwareContent.component, 1, CyberwareItemMetadata.of(2)),
				new ItemStack(CyberwareContent.component, 1, CyberwareItemMetadata.of(4))
			});
	}
}
