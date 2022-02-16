package com.supermartijn642.movingelevators.data;

import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.function.Consumer;

/**
 * Created 14/02/2022 by SuperMartijn642
 */
public class MovingElevatorsRecipeProvider extends RecipeProvider {

    public MovingElevatorsRecipeProvider(GatherDataEvent e){
        super(e.getGenerator());
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> recipeConsumer){
        ShapedRecipeBuilder.shaped(MovingElevators.elevator_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ADA")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', Tags.Items.DUSTS_REDSTONE)
            .define('C', Blocks.OBSERVER)
            .define('D', Blocks.PISTON)
            .unlocks("has_iron", this.has(Tags.Items.INGOTS_IRON))
            .save(recipeConsumer);
        ShapedRecipeBuilder.shaped(MovingElevators.display_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ABA")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', Tags.Items.DUSTS_REDSTONE)
            .define('C', Tags.Items.GLASS)
            .unlocks("has_controller", this.has(MovingElevators.elevator_block))
            .save(recipeConsumer);
        ShapedRecipeBuilder.shaped(MovingElevators.button_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("AAA")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', Tags.Items.DUSTS_REDSTONE)
            .define('C', Tags.Items.ENDER_PEARLS)
            .unlocks("has_controller", this.has(MovingElevators.elevator_block))
            .save(recipeConsumer);
    }
}
