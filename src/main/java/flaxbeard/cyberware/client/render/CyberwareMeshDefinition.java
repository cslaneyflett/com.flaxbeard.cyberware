package flaxbeard.cyberware.client.render;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware.Quality;
import flaxbeard.cyberware.common.item.ItemCyberware;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CyberwareMeshDefinition implements ItemMeshDefinition
{
	@Override
	public ModelResourceLocation getModelLocation(ItemStack stack)
	{
		ItemStack test = stack.copy();
		if (!test.isEmpty() && test.hasTag())
		{
			assert test.getTag() != null;
			test.getTag().remove(CyberwareAPI.QUALITY_TAG);
		}

		ItemCyberware ware = (ItemCyberware) stack.getItem();
		String added = "";
		if (ware.subnames.length > 0)
		{
			int i = Math.min(ware.subnames.length - 1, CyberwareItemMetadata.get(stack));
			added = "_" + ware.subnames[i];
		}

		Quality q = CyberwareAPI.getCyberware(stack).getQuality(stack);

		if (q != null && CyberwareAPI.getCyberware(test).getQuality(test) != q && q.getSpriteSuffix() != null)
		{
			return new ModelResourceLocation(ware.getRegistryName() + added + "_" + q.getSpriteSuffix(), "inventory");
		} else
		{
			return new ModelResourceLocation(ware.getRegistryName() + added, "inventory");
		}
	}
}
