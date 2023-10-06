package flaxbeard.cyberware.common.config;

import flaxbeard.cyberware.api.item.ICyberware;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class StartingStacksConfig
{
	//    public static void postInit()
	//    {
	//        int index = 0;
	//        for (String[] items : startingItems)
	//        {
	//            EnumSlot slot = EnumSlot.values()[index];
	//            if (items.length > LibConstants.WARE_PER_SLOT)
	//            {
	//                throw new RuntimeException("Cyberware configuration error! Too many items for slot " + slot
	//                .getName());
	//            }
	//
	//            for (int indexItem = 0; indexItem < items.length; indexItem++)
	//            {
	//                String itemEncoded = items[indexItem];
	//                String[] params = itemEncoded.split("\\s+");
	//
	//                String itemName;
	//                int metadata;
	//                int quantity;
	//
	//                if (params.length == 1)
	//                {
	//                    itemName = params[0];
	//                    metadata = 0;
	//                    quantity = 0;
	//                }
	//                else if (params.length == 3)
	//                {
	//                    itemName = params[0];
	//                    try
	//                    {
	//                        metadata = Integer.parseInt(params[2]);
	//                    }
	//                    catch (NumberFormatException e)
	//                    {
	//                        throw new RuntimeException("Cyberware configuration error! Item " + (indexItem + 1) + "
	//                        for "
	//                                + slot.getName() + " slot has invalid metadata: '" + params[2] + "'");
	//                    }
	//                    try
	//                    {
	//                        quantity = Integer.parseInt(params[1]);
	//                    }
	//                    catch (NumberFormatException e)
	//                    {
	//                        throw new RuntimeException("Cyberware configuration error! Item " + (indexItem + 1) + "
	//                        for "
	//                                + slot.getName() + " slot has invalid quantity: '" + params[1] + "'");
	//                    }
	//                }
	//                else
	//                {
	//                    throw new RuntimeException("Cyberware configuration error! Item " + (indexItem + 1) + " for "
	//                            + slot.getName() + " slot has too many arguments!");
	//                }
	//
	//                Item item;
	//                try
	//                {
	//                    item = CommandBase.getItemByText(null, itemName);
	//                }
	//                catch (NumberInvalidException e)
	//                {
	//                    throw new RuntimeException("Cyberware configuration error! Item '" + (indexItem + 1) + "'
	//                    for "
	//                            + slot.getName() + " slot has a nonexistant item: " + itemName);
	//                }
	//
	//                ItemStack stack = new ItemStack(item, quantity, metadata);
	//
	//                if (!CyberwareAPI.isCyberware(stack))
	//                {
	//                    throw new RuntimeException("Cyberware configuration error! " + itemName + " is not a valid
	//                    piece of cyberware!");
	//                }
	//                if ((CyberwareAPI.getCyberware(stack)).getSlot(stack) != slot)
	//                {
	//                    throw new RuntimeException("Cyberware configuration error! " + itemEncoded + " will not fit
	//                    in slot " + slot.getName());
	//                }
	//
	//                startingStacks.get(index).set(indexItem, stack);
	//            }
	//
	//            index++;
	//        }
	//    }
	//
	private static String[][] defaultStartingItems;
	private static String[][] startingItems;
	private static NonNullList<NonNullList<ItemStack>> startingStacks;

	public static NonNullList<ItemStack> getStartingItems(@Nonnull ICyberware.EnumSlot slot)
	{
		return startingStacks.get(slot.ordinal());
	}

	//    private static final String C_MOBS = "Mobs";
	//    private static final String C_OTHER = "Other";
	//    private static final String C_HUD = "HUD";
	//    private static final String C_MACHINES = "Machines";
	//    private static final String C_ESSENCE = "Essence";
	//    private static final String C_GAMERULES = "Gamerules";
	//    private static final String C_INTEGRATION = "Integration";


	//    public static void preInit()
	//    {
	//        config = new ForgeConfigSpec.Builder();
	//        startingItems = defaultStartingItems = new String[EnumSlot.values().length][0];
	//        startingStacks = NonNullList.create();
	//        for (EnumSlot slot : EnumSlot.values())
	//        {
	//            NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
	//            for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
	//            {
	//                nnlCyberwaresInSlot.add(ItemStack.EMPTY);
	//            }
	//            startingStacks.add(nnlCyberwaresInSlot);
	//        }
	//
	//        int metadata = 0;
	//        for (int index = 0; index < EnumSlot.values().length; index++)
	//        {
	//            if (EnumSlot.values()[index].hasEssential())
	//            {
	//                if (EnumSlot.values()[index].isSided())
	//                {
	//                    defaultStartingItems[index] = new String[] { "cyberware:body_part 1 " + metadata,
	//                    "cyberware:body_part 1 " + (metadata + 1)  };
	//                    metadata += 2;
	//                }
	//                else
	//                {
	//                    defaultStartingItems[index] = new String[] { "cyberware:body_part 1 " + metadata };
	//                    metadata++;
	//                }
	//            }
	//            else
	//            {
	//                defaultStartingItems[index] = new String[0];
	//            }
	//        }
	//        loadConfig();
	//
	//        config.load();
	//        for (int index = 0; index < EnumSlot.values().length; index++)
	//        {
	//            EnumSlot slot = EnumSlot.values()[index];
	//            startingItems[index] = config.getStringList("Default augments for " + slot.getName() + " slot",
	//                    "Defaults", defaultStartingItems[index], "Use format 'id amount metadata'");
	//        }
	//        config.save();
	//
	//    }
}
