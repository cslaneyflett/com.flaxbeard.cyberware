package flaxbeard.cyberware.common.item;

import com.google.common.collect.HashMultimap;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.registry.items.BoneUpgrades;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public class ItemBoneUpgrade extends ItemCyberware
{
	// TODO: this shouldn't be here anymore
	public static final int MAX_STACK_SIZE_LACING = 5;
	private static final UUID idBoneHealthAttribute = UUID.fromString("8bce997a-4c3a-11e6-beb8-9e71128cae77");
	private static final HashMap<Integer, HashMultimap<Attribute, AttributeModifier>> multimapBoneHealthAttributes =
		new HashMap<>(MAX_STACK_SIZE_LACING + 1);

	public ItemBoneUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.BONE);
	}

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
	public void onAdded(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
	{
		if (stack.getItem() == BoneUpgrades.LACING.get())
		{
			entityLivingBase.getAttributes().addTransientAttributeModifiers(getBoneHealthAttribute(stack.getCount()));
		}
	}

	@Override
	public void onRemoved(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
	{
		if (stack.getItem() == BoneUpgrades.LACING.get())
		{
			entityLivingBase.getAttributes().removeAttributeModifiers(getBoneHealthAttribute(stack.getCount()));
		}
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return other.getItem() == this;
	}

	public static class EventHandler
	{
		@SubscribeEvent
		public void handleJoinWorld(EntityJoinLevelEvent event)
		{
			if (!(event.getEntity() instanceof LivingEntity entityLivingBase)) return;
			if (entityLivingBase.tickCount % 20 != 0) return;

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
			if (cyberwareUserData != null)
			{
				var itemStackMetalLacing = cyberwareUserData.getCyberware(BoneUpgrades.LACING.get().getDefaultInstance());
				var lacingItem = (ItemBoneUpgrade) itemStackMetalLacing.getItem();
				if (!itemStackMetalLacing.isEmpty())
				{
					lacingItem.onAdded(entityLivingBase, cyberwareUserData.getCyberware(itemStackMetalLacing));
				} else
				{
					lacingItem.onRemoved(entityLivingBase, itemStackMetalLacing);
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

			if (cyberwareUserData.isCyberwareInstalled(BoneUpgrades.FLEX.get().getDefaultInstance()))
			{
				event.setAmount(event.getAmount() * .3333F);
			}
		}
	}
}
