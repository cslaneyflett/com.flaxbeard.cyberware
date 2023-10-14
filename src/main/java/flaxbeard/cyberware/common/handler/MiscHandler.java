package flaxbeard.cyberware.common.handler;

import com.mojang.blaze3d.platform.InputConstants;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.Quality;
import flaxbeard.cyberware.api.item.IDeconstructable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.stream.Collectors;

public class MiscHandler
{
	public static final MiscHandler INSTANCE = new MiscHandler();

	@SubscribeEvent(priority = EventPriority.LOWEST)
	@OnlyIn(Dist.CLIENT)
	public void handleCyberwareTooltip(ItemTooltipEvent event)
	{
		ItemStack stack = event.getItemStack();
		if (CyberwareAPI.isCyberware(stack))
		{
			ICyberware ware = CyberwareAPI.getCyberware(stack);
			Quality quality = ware.getQuality(stack);


			var settings = Minecraft.getInstance().options;
			// TODO: cursed
			if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), settings.keyShift.getKey().getValue()))
			{
				List<String> info = ware.getInfo(stack);
				if (info != null)
				{
					// TODO: ms paint
					List<Component> comp = info.stream().map(Component::literal).collect(Collectors.toList());
					event.getToolTip().addAll(comp);
				}

				NonNullList<NonNullList<ItemStack>> requirements = ware.required(stack);
				if (!requirements.isEmpty())
				{
					StringBuilder joined = new StringBuilder();
					for (int indexRequirement = 0; indexRequirement < requirements.size(); indexRequirement++)
					{
						StringBuilder toAdd = new StringBuilder();

						for (int indexSubRequirement = 0; indexSubRequirement < requirements.get(indexRequirement).size(); indexSubRequirement++)
						{
							if (indexSubRequirement != 0)
							{
								toAdd.append(" ").append(I18n.get("cyberware.tooltip.joiner_or")).append(" ");
							}
							toAdd.append(requirements.get(indexRequirement).get(indexSubRequirement).getDisplayName());
						}

						if (indexRequirement != 0)
						{
							joined.append(I18n.get("cyberware.tooltip.joiner")).append(" ");
						}
						joined.append(toAdd);
					}
					event.getToolTip().add(Component.literal(ChatFormatting.AQUA + I18n.get("cyberware.tooltip.requires") + " " + joined));
				}
				event.getToolTip().add(Component.literal(ChatFormatting.RED + I18n.get("cyberware.slot." + ware.getSlot(stack).getName())));


				if (quality != null)
				{
					event.getToolTip().add(Component.translatable(quality.getUnlocalizedName()));
				}
			} else
			{
				event.getToolTip().add(Component.literal(ChatFormatting.DARK_GRAY + I18n.get("cyberware.tooltip.shift_prompt")));
			}
		} else if (stack.getItem() instanceof IDeconstructable)
		{
			if (event.getToolTip().size() > 1)
			{
				event.getToolTip().add(1, Component.literal(ChatFormatting.DARK_GRAY + I18n.get("cyberware.tooltip.can_deconstruct")));
			} else
			{
				event.getToolTip().add(Component.literal(ChatFormatting.DARK_GRAY + I18n.get("cyberware.tooltip.can_deconstruct")));
			}
		}
	}

	@SubscribeEvent
	public void handleNeuropozynePopulation(LootTableLoadEvent event)
	{
		// TODO ??
		//		LootPoolSingletonContainer.simpleBuilder()
		if (event.getName() == BuiltInLootTables.SIMPLE_DUNGEON
			|| event.getName() == BuiltInLootTables.ABANDONED_MINESHAFT
			|| event.getName() == BuiltInLootTables.STRONGHOLD_CROSSING
			|| event.getName() == BuiltInLootTables.STRONGHOLD_CORRIDOR
			|| event.getName() == BuiltInLootTables.STRONGHOLD_LIBRARY
			|| event.getName() == BuiltInLootTables.DESERT_PYRAMID
			|| event.getName() == BuiltInLootTables.JUNGLE_TEMPLE)
		{
			LootTable table = event.getTable();
			LootPool main = table.getPool("main");
			if (main != null)
			{
				LootItemCondition[] lc = new LootItemCondition[0];
				//				LootItemFunction[] lf = new LootItemFunction[]{new SetItemCountFunction(lc, new RandomValueRange(16F, 64F))};
				//				main.addEntry(new LootPoolEntry(CyberwareContent.neuropozyne, 15, 0, lf, lc, "cyberware:neuropozyne"));
			}
		}

		if (event.getName() == BuiltInLootTables.NETHER_BRIDGE)
		{
			LootTable table = event.getTable();
			LootPool main = table.getPool("main");
			if (main != null)
			{
				LootItemCondition[] lc = new LootItemCondition[0];
				LootItemFunction[] lf = new LootItemFunction[0];
				//				main.addEntry(new LootPoolEntry(CWBlocks.SURGERY.get().asItem(), 15, 0, lf,
				//					lc, "cyberware:surgery_apparatus"
				//				));
			}
		}
	}
}
