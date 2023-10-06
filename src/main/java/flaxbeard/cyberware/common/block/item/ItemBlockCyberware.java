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
	private String[] tt;

	public ItemBlockCyberware(Block block, Properties properties)
	{
		super(block, properties);
	}

	public ItemBlockCyberware(Block block, Properties properties, String... tooltip)
	{
		super(block, properties);
		this.tt = tooltip;
	}

	public ItemBlockCyberware(Block block, String... tooltip)
	{
		super(block, new Properties());
		this.tt = tooltip;
	}

	@Override
	public EnumCategory getCategory(ItemStack stack)
	{
		return EnumCategory.BLOCKS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag advanced)
	{
		if (this.tt == null) return;

		for (String str : tt)
		{
			// TODO: chat components .translatable
			tooltip.add(Component.literal(ChatFormatting.GRAY + I18n.get(str)));
		}
	}
}
