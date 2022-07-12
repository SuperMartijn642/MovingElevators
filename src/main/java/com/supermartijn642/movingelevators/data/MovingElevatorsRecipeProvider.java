package com.supermartijn642.movingelevators.data;

import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.function.Consumer;

/**
 * Created 14/02/2022 by SuperMartijn642
 */
public class MovingElevatorsRecipeProvider extends RecipeProvider {

    public MovingElevatorsRecipeProvider(GatherDataEvent e){
        super(e.getGenerator());
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipeConsumer){
        ShapedRecipeBuilder.shaped(MovingElevators.elevator_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ADA")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', Tags.Items.DUSTS_REDSTONE)
            .define('C', Blocks.OBSERVER)
            .define('D', Blocks.PISTON)
            .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
            .save(recipeConsumer);
        ShapedRecipeBuilder.shaped(MovingElevators.display_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ABA")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', Tags.Items.DUSTS_REDSTONE)
            .define('C', Tags.Items.GLASS)
            .unlockedBy("has_controller", has(MovingElevators.elevator_block))
            .save(recipeConsumer);
        ShapedRecipeBuilder.shaped(MovingElevators.button_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("AAA")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', Tags.Items.DUSTS_REDSTONE)
            .define('C', Tags.Items.ENDER_PEARLS)
            .unlockedBy("has_controller", has(MovingElevators.elevator_block))
            .save(recipeConsumer);
    }
}
