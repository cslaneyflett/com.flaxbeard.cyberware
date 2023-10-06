package flaxbeard.cyberware.common.item;

import com.google.common.collect.HashMultimap;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

public class ItemBoneUpgrade extends ItemCyberware
{
	public static final int META_LACING = 0;
	public static final int META_FLEX = 1;
	public static final int META_BATTERY = 2;
	public static final int MAX_STACK_SIZE_LACING = 5;

	public ItemBoneUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private static final UUID idBoneHealthAttribute = UUID.fromString("8bce997a-4c3a-11e6-beb8-9e71128cae77");
	private static final HashMap<Integer, HashMultimap<Attribute, AttributeModifier>> multimapBoneHealthAttributes =
		new HashMap<>(MAX_STACK_SIZE_LACING + 1);

	private static HashMultimap<Attribute, AttributeModifier> getBoneHealthAttribute(int stackSize)
	{
		HashMultimap<Attribute, AttributeModifier> multimapBoneHealthAttribute =
			multimapBoneHealthAttributes.get(stackSize);
		if (multimapBoneHealthAttribute == null)
		{
			multimapBoneHealthAttribute = HashMultimap.create();
			multimapBoneHealthAttribute.put(Attributes.MAX_HEALTH, new AttributeModifier(idBoneHealthAttribute, "Bone " +
				"hp upgrade", 4F * stackSize, AttributeModifier.Operation.ADDITION));
			multimapBoneHealthAttributes.put(stackSize, multimapBoneHealthAttribute);
		}
		return multimapBoneHealthAttribute;
	}

	@Override
	public void onAdded(LivingEntity entityLivingBase, ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_LACING)
		{
			entityLivingBase.getAttributes().addTransientAttributeModifiers(getBoneHealthAttribute(stack.getCount()));
		}
	}

	@Override
	public void onRemoved(LivingEntity entityLivingBase, ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_LACING)
		{
			entityLivingBase.getAttributes().removeAttributeModifiers(getBoneHealthAttribute(stack.getCount()));
		}
	}

	@SubscribeEvent
	public void handleJoinWorld(EntityJoinLevelEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity entityLivingBase)) return;
		if (entityLivingBase.tickCount % 20 != 0) return;

		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			ItemStack itemStackMetalLacing = cyberwareUserData.getCyberware(getCachedStack(META_LACING));
			if (!itemStackMetalLacing.isEmpty())
			{
				onAdded(entityLivingBase, cyberwareUserData.getCyberware(itemStackMetalLacing));
			} else
			{
				onRemoved(entityLivingBase, itemStackMetalLacing);
			}
		}
	}

	@SubscribeEvent
	public void handleFallDamage(LivingHurtEvent event)
	{
		if (event.getSource() != DamageSource.FALL) return;

		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;

		if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_FLEX)))
		{
			event.setAmount(event.getAmount() * .3333F);
		}
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return other.getItem() == this;
	}

	@Override

	public int getCapacity(ItemStack wareStack)
	{
		return CyberwareItemMetadata.matches(wareStack, META_BATTERY)
			? LibConstants.BONE_BATTERY_CAPACITY * wareStack.getCount()
			: 0;
	}

	@Override
	public int installedStackSize(ItemStack stack)
	{
		return CyberwareItemMetadata.matches(stack, META_LACING)
			? MAX_STACK_SIZE_LACING
			: CyberwareItemMetadata.matches(stack, META_BATTERY) ? 4 : 1;
	}

	@Override
	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		if (CyberwareItemMetadata.matches(stack, META_LACING))
		{
			switch (stack.getCount())
			{
				case 1:
					return 3;
				case 2:
					return 6;
				case 3:
					return 9;
				case 4:
					return 12;
				case 5:
					return 15;
			}
		}
		if (CyberwareItemMetadata.get(stack) == META_BATTERY)
		{
			switch (stack.getCount())
			{
				case 1:
					return 2;
				case 2:
					return 3;
				case 3:
					return 4;
				case 4:
					return 5;
			}
		}
		return super.getUnmodifiedEssenceCost(stack);
	}
}
