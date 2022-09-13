package com.supermartijn642.movingelevators.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/**
 * Created 14/02/2022 by SuperMartijn642
 */
public class MovingElevatorsRecipeGenerator extends RecipeGenerator {

    public MovingElevatorsRecipeGenerator(ResourceCache cache){
        super("movingelevators", cache);
    }

    @Override
    public void generate(){
        this.shaped(MovingElevators.elevator_block.asItem())
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ADA")
            .input('A', "ingotIron")
            .input('B', "dustRedstone")
            .input('C', Item.getItemFromBlock(Blocks.OBSERVER))
            .input('D', Item.getItemFromBlock(Blocks.PISTON))
            .unlockedByOreDict("ingotIron");
        this.shaped(MovingElevators.display_block.asItem())
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ABA")
            .input('A', "ingotIron")
            .input('B', "dustRedstone")
            .input('C', "blockGlass")
            .unlockedBy(MovingElevators.elevator_block.asItem());
        this.shaped(MovingElevators.button_block.asItem())
            .pattern("ABA")
            .pattern("ACA")
            .pattern("AAA")
            .input('A', "ingotIron")
            .input('B', "dustRedstone")
            .input('C', Items.ENDER_PEARL)
            .unlockedBy(MovingElevators.elevator_block.asItem());
    }
}
