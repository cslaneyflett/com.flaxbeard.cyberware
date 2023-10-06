package flaxbeard.cyberware.api.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface IMenuItem
{
	public boolean hasMenu(ItemStack stack);

	public void use(Entity entity, ItemStack stack);

	public String getUnlocalizedLabel(ItemStack stack);

	public float[] getColor(ItemStack stack);
}
