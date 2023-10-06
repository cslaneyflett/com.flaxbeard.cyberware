package flaxbeard.cyberware.client.integration.jei;

import flaxbeard.cyberware.Cyberware;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;

@JEIPlugin
public class JEICyberwarePlugin implements IModPlugin
{
	@Override
	public void register(IModRegistry registry)
	{
		Cyberware.logger.info("Augmenting JEI");

		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		//registry.addRecipeCategories(recipeCategories);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry)
	{
		// TODO Auto-generated method stub

	}
}
