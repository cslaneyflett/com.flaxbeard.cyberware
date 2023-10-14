package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.Cyberware;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CWTags
{
	private CWTags()
	{
	}

	// TODO: provider
	public static TagKey<Item> COMPONENTS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Cyberware.MODID, "components"));
	public static TagKey<Item> BODY_PARTS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Cyberware.MODID, "body_parts"));
}
