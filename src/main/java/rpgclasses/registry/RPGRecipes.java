package rpgclasses.registry;

import aphorea.registry.AphRecipes;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.inventory.recipe.Ingredient;

public class RPGRecipes {

    public static void initRecipes() {
        AphRecipes.addCraftingList("diamondbackpack", RecipeTechRegistry.WORKSTATION,
                AphRecipes.AphCraftingRecipe.showAfter("ringsbox", 1,
                        new Ingredient("anylog", 10),
                        new Ingredient("ironbar", 1)
                )
        );
    }

}
