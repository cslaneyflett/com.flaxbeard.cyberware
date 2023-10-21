package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.common.registry.items.Eyes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CWCreativeTabs
{
	private CWCreativeTabs()
	{
	}

	public static CreativeModeTab CYBERWARE = new CreativeModeTab("cyberware")
	{
		@Override
		@Nonnull
		public ItemStack makeIcon()
		{
			return Eyes.CYBEREYE_BASE.get().getDefaultInstance();
		}
	};
}
