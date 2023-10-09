package flaxbeard.cyberware.common.block.item;

import flaxbeard.cyberware.api.item.ICyberwareTabItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockCyberware extends BlockItem implements ICyberwareTabItem
{
	private final String[] tooltip;
	
	public ItemBlockCyberware(Block block, Properties properties, String... tooltip)
	{
		super(block, properties);
		this.tooltip = tooltip;
	}
	
	@Override
	public EnumCategory getCategory(ItemStack stack)
	{
		return EnumCategory.BLOCKS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
	{
		super.appendHoverText(stack, level, tooltip, flag);
		if (this.tooltip == null) return;

		for (String str : this.tooltip)
		{
			tooltip.add(Component.literal(ChatFormatting.GRAY + I18n.get(str)));
		}
	}
}
