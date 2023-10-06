package flaxbeard.cyberware.common.item;

import com.google.common.collect.HashMultimap;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.config.DefaultConfig;
import flaxbeard.cyberware.common.misc.NNLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemHandUpgrade extends ItemCyberware implements IMenuItem
{
	public static final int META_CRAFT_HANDS = 0;
	public static final int META_CLAWS = 1;
	public static final int META_MINING = 2;
	private static Item itemTool;
	private static final UUID uuidClawsDamageAttribute = UUID.fromString("63c32801-94fb-40d4-8bd2-89135c1e44b1");
	private static final HashMultimap<String, AttributeModifier> multimapClawsDamageAttribute;
	private static final Map<UUID, Boolean> lastClaws = new HashMap<>();
	public static float clawsTime;

	static
	{
		multimapClawsDamageAttribute = HashMultimap.create();
		multimapClawsDamageAttribute.put(
			Attributes.ATTACK_DAMAGE,
			new AttributeModifier(uuidClawsDamageAttribute, "Claws damage upgrade", 5.5F, 0)
		);
	}

	public ItemHandUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
	{
		return NNLUtil.fromArray(new ItemStack[][]{
			new ItemStack[]{CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM),
				CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM)}});
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return other.getItem() == this;
	}

	private ItemStack getItemStackTool()
	{
		if (itemTool == null)
		{
			// TODO: new config system doesnt support strings??? wtf??
			Item itemConfig = Item.getByNameOrId(DefaultConfig.FIST_MINING_TOOL_NAME);
			if (itemConfig == null)
			{
				Cyberware.logger.error(String.format(
					"Unable to find item with id %s, check your configuration. " +
						"Defaulting fist mining tool to Iron pickaxe.",
					DefaultConfig.FIST_MINING_TOOL_NAME
				));
				itemConfig = Items.IRON_PICKAXE;
			}
			itemTool = itemConfig;
		}
		return new ItemStack(itemTool);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		ItemStack itemStackClaws = cyberwareUserData.getCyberware(getCachedStack(META_CLAWS));
		if (!itemStackClaws.isEmpty())
		{
			boolean wasEquipped = getLastClaws(entityLivingBase);
			boolean isEquipped = entityLivingBase.getHeldItemMainhand().isEmpty()
				&& (entityLivingBase.getPrimaryHand() == EnumHandSide.RIGHT
				? (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM)))
				: (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM))))
				&& EnableDisableHelper.isEnabled(itemStackClaws);
			if (isEquipped)
			{
				if (!wasEquipped
					|| entityLivingBase.tickCount % 20 == 0)
				{
					addClawsDamage(entityLivingBase);
					lastClaws.put(entityLivingBase.getUUID(), Boolean.TRUE);
				}

				if (!wasEquipped
					&& entityLivingBase.getLevel().isClientSide())
				{
					updateHand(entityLivingBase, true);
				}
			} else if (wasEquipped
				|| entityLivingBase.tickCount % 20 == 0)
			{
				removeClawsDamage(entityLivingBase);
				lastClaws.put(entityLivingBase.getUUID(), Boolean.FALSE);
			}
		} else if (entityLivingBase.tickCount % 20 == 0)
		{
			removeClawsDamage(entityLivingBase);
			lastClaws.put(entityLivingBase.getUUID(), Boolean.FALSE);
		}
	}

	private void updateHand(LivingEntity entityLivingBase, boolean delay)
	{
		if (Minecraft.getInstance() != null
			&& Minecraft.getInstance().player != null
			&& entityLivingBase == Minecraft.getInstance().player)
		{
			clawsTime = Minecraft.getInstance().getRenderPartialTicks() + entityLivingBase.tickCount + (delay ? 5 : 0);
		}
	}

	private boolean getLastClaws(LivingEntity entityLivingBase)
	{
		if (!lastClaws.containsKey(entityLivingBase.getUUID()))
		{
			lastClaws.put(entityLivingBase.getUUID(), Boolean.FALSE);
		}
		return lastClaws.get(entityLivingBase.getUUID());
	}

	private void addClawsDamage(LivingEntity entityLivingBase)
	{
		entityLivingBase.getAttributeMap().applyAttributeModifiers(multimapClawsDamageAttribute);
	}

	private void removeClawsDamage(LivingEntity entityLivingBase)
	{
		entityLivingBase.getAttributeMap().removeAttributeModifiers(multimapClawsDamageAttribute);
	}

	@Override
	public void onRemoved(LivingEntity entityLivingBase, ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_CLAWS)
		{
			removeClawsDamage(entityLivingBase);
		}
	}

	@SubscribeEvent
	public void handleMining(HarvestCheck event)
	{
		Player entityPlayer = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;

		ItemStack itemStackMining = cyberwareUserData.getCyberware(getCachedStack(META_MINING));
		boolean rightArm = (entityPlayer.getPrimaryHand() == EnumHandSide.RIGHT
			? (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM)))
			: (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM))));
		if (rightArm
			&& !itemStackMining.isEmpty()
			&& entityPlayer.getHeldItemMainhand().isEmpty())
		{
			ItemStack itemStackTool = getItemStackTool();
			if (itemStackTool.canHarvestBlock(event.getTargetBlock()))
			{
				event.setCanHarvest(true);
			}
		}
	}

	@SubscribeEvent
	public void handleMineSpeed(BreakSpeed event)
	{
		Player entityPlayer = event.getEntity();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;

		ItemStack itemStackMining = cyberwareUserData.getCyberware(getCachedStack(META_MINING));
		boolean rightArm = (entityPlayer.getMainArm() == HumanoidArm.RIGHT
			? (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM)))
			: (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM))));
		if (rightArm
			&& !itemStackMining.isEmpty()
			&& entityPlayer.getMainHandItem().isEmpty())
		{
			final ItemStack itemStackTool = getItemStackTool();
			event.setNewSpeed(event.getNewSpeed() * itemStackTool.getDestroySpeed(entityPlayer.level.getBlockState(event.getPosition().orElseThrow())));
		}
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_CLAWS;
	}

	@Override
	public void use(Entity entity, ItemStack stack)
	{
		EnableDisableHelper.toggle(stack);
		// TODO: siding
		if (entity instanceof LivingEntity && FMLCommonHandler.instance().getSide() == Dist.CLIENT)
		{
			updateHand((LivingEntity) entity, false);
		}
	}

	@Override
	public String getUnlocalizedLabel(ItemStack stack)
	{
		return EnableDisableHelper.getUnlocalizedLabel(stack);
	}

	private static final float[] f = new float[]{1.0F, 0.0F, 0.0F};

	@Override
	public float[] getColor(ItemStack stack)
	{
		return EnableDisableHelper.isEnabled(stack) ? f : null;
	}
}
