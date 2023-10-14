package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.Cyberware;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CWRecipes
{
	private CWRecipes()
	{
	}

	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Cyberware.MODID);

	// TODO
	//	public static final RegistryObject<RecipeType<?>> BLUEPRINT = RECIPE_TYPES.register("blueprint_crafting", () ->
	//		new BlueprintCraftingHandler(new ResourceLocation(Cyberware.MODID + ":blueprint_crafting")));


	//	RecipeSorter.register(Cyberware.MODID + ":blueprintCrafting", BlueprintCraftingHandler.class,
	//	RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
}
