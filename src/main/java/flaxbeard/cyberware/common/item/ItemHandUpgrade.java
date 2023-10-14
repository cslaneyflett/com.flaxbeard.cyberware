package flaxbeard.cyberware.common.item;

import com.google.common.collect.HashMultimap;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.registry.items.ArmUpgrades;
import flaxbeard.cyberware.common.registry.items.CyberLimbs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemHandUpgrade extends ItemCyberware implements IMenuItem
{
	private static Item itemTool;
	private static final UUID uuidClawsDamageAttribute = UUID.fromString("63c32801-94fb-40d4-8bd2-89135c1e44b1");
	private static final HashMultimap<Attribute, AttributeModifier> multimapClawsDamageAttribute;
	private static final Map<UUID, Boolean> lastClaws = new HashMap<>();
	public static float clawsTime;

	static
	{
		multimapClawsDamageAttribute = HashMultimap.create();
		multimapClawsDamageAttribute.put(
			Attributes.ATTACK_DAMAGE,
			new AttributeModifier(uuidClawsDamageAttribute, "Claws damage upgrade", 5.5F, AttributeModifier.Operation.ADDITION)
		);
	}

	public ItemHandUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.HAND);
	}

	@Nonnull
	@Override
	public NonNullList<NonNullList<ItemStack>> required(@Nonnull ItemStack stack)
	{
		return NNLUtil.fromArray(new ItemStack[][]{
			new ItemStack[]{
				CyberLimbs.CYBERARM_LEFT.get().getDefaultInstance(),
				CyberLimbs.CYBERARM_RIGHT.get().getDefaultInstance()
			}});
	}

	@Override
	public boolean isIncompatible(@Nonnull ItemStack stack, @Nonnull ItemStack other)
	{
		return other.getItem() == this;
	}

	private void updateHand(LivingEntity entityLivingBase, boolean delay)
	{
		if (Minecraft.getInstance().player != null &&
			entityLivingBase == Minecraft.getInstance().player)
		{
			clawsTime = Minecraft.getInstance().getPartialTick() + entityLivingBase.tickCount + (delay ? 5 : 0);
		}
	}

	@Override
	public void onRemoved(@Nonnull LivingEntity entityLivingBase, @Nonnull ItemStack stack)
	{
		if (stack.is(ArmUpgrades.CLAWS.get()))
		{
			removeClawsDamage(entityLivingBase);
		}
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return stack.is(ArmUpgrades.CLAWS.get());
	}

	@Override
	public void use(Entity entity, ItemStack stack)
	{
		EnableDisableHelper.toggle(stack);

		if (entity instanceof LivingEntity living)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> updateHand(living, false));
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
		entityLivingBase.getAttributes().addTransientAttributeModifiers(multimapClawsDamageAttribute);
	}

	private void removeClawsDamage(LivingEntity entityLivingBase)
	{
		entityLivingBase.getAttributes().removeAttributeModifiers(multimapClawsDamageAttribute);
	}

	public static class EventHandler
	{
		public static final EventHandler INSTANCE = new EventHandler();

		private ItemStack getItemStackTool()
		{
			// TODO: config disabled, resource getting magic is janky

			// if (itemTool == null)
			// {
			// 	Item itemConfig = ForgeRegistries.ITEMS.getValue(ResourceKey.) Item.getByNameOrId(DefaultConfig.FIST_MINING_TOOL_NAME);
			// 	if (itemConfig == null)
			// 	{
			// 		Cyberware.logger.error(String.format(
			// 			"Unable to find item with id %s, check your configuration. " +
			// 				"Defaulting fist mining tool to Iron pickaxe.",
			// 			DefaultConfig.FIST_MINING_TOOL_NAME
			// 		));
			// 		itemConfig = Items.IRON_PICKAXE;
			// 	}
			// 	itemTool = itemConfig;
			// }
			itemTool = Items.IRON_PICKAXE;
			return new ItemStack(itemTool);
		}

		@SubscribeEvent(priority = EventPriority.NORMAL)
		public void handleLivingUpdate(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

			var claws = (ItemHandUpgrade) ArmUpgrades.CLAWS.get();
			ItemStack itemStackClaws = cyberwareUserData.getCyberware(claws.getDefaultInstance());
			if (!itemStackClaws.isEmpty())
			{
				boolean wasEquipped = claws.getLastClaws(entityLivingBase);
				boolean isEquipped = entityLivingBase.getMainHandItem().isEmpty()
					&& (
					entityLivingBase.getMainArm() == HumanoidArm.RIGHT
						? cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERARM_RIGHT.get().getDefaultInstance())
						: cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERARM_LEFT.get().getDefaultInstance())
				)
					&& EnableDisableHelper.isEnabled(itemStackClaws);
				if (isEquipped)
				{
					if (!wasEquipped
						|| entityLivingBase.tickCount % 20 == 0)
					{
						claws.addClawsDamage(entityLivingBase);
						lastClaws.put(entityLivingBase.getUUID(), Boolean.TRUE);
					}

					if (!wasEquipped
						&& entityLivingBase.getLevel().isClientSide())
					{
						claws.updateHand(entityLivingBase, true);
					}
				} else if (wasEquipped
					|| entityLivingBase.tickCount % 20 == 0)
				{
					claws.removeClawsDamage(entityLivingBase);
					lastClaws.put(entityLivingBase.getUUID(), Boolean.FALSE);
				}
			} else if (entityLivingBase.tickCount % 20 == 0)
			{
				claws.removeClawsDamage(entityLivingBase);
				lastClaws.put(entityLivingBase.getUUID(), Boolean.FALSE);
			}
		}

		public Tuple<Boolean, ItemStack> mainArmIsCybernetic(Player entityPlayer)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData == null) return null;

			ItemStack itemStackMining = cyberwareUserData.getCyberware(ArmUpgrades.MINING.get().getDefaultInstance());
			boolean isCybernetic = entityPlayer.getMainArm() == HumanoidArm.RIGHT
				? cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERARM_RIGHT.get().getDefaultInstance())
				: cyberwareUserData.isCyberwareInstalled(CyberLimbs.CYBERARM_LEFT.get().getDefaultInstance());

			return new Tuple<>(isCybernetic, itemStackMining);
		}

		@SubscribeEvent
		public void handleMining(HarvestCheck event)
		{
			Player entityPlayer = event.getEntity();

			var tuple = mainArmIsCybernetic(entityPlayer);
			if (tuple == null) return;
			ItemStack itemStackMining = tuple.getB();
			boolean isArmCybernetic = tuple.getA();

			if (isArmCybernetic
				&& !itemStackMining.isEmpty()
				&& entityPlayer.getMainHandItem().isEmpty())
			{
				ItemStack itemStackTool = getItemStackTool();
				// TODO

				// if (event.getTargetBlock() itemStackTool.canHarvestBlock(event.getTargetBlock()))
				// {
				// 	event.setCanHarvest(true);
				// }
			}
		}

		@SubscribeEvent
		public void handleMineSpeed(BreakSpeed event)
		{
			Player entityPlayer = event.getEntity();

			var tuple = mainArmIsCybernetic(entityPlayer);
			if (tuple == null) return;
			ItemStack itemStackMining = tuple.getB();
			boolean isArmCybernetic = tuple.getA();

			if (isArmCybernetic
				&& !itemStackMining.isEmpty()
				&& entityPlayer.getMainHandItem().isEmpty())
			{
				final ItemStack itemStackTool = getItemStackTool();
				event.setNewSpeed(event.getNewSpeed() * itemStackTool.getDestroySpeed(entityPlayer.level.getBlockState(event.getPosition().orElseThrow())));
			}
		}
	}
}
