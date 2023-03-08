package com.supermartijn642.movingelevators.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.movingelevators.MovingElevators;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Created 14/02/2022 by SuperMartijn642
 */
public class MovingElevatorsRecipeGenerator extends RecipeGenerator {

    public MovingElevatorsRecipeGenerator(ResourceCache cache){
        super("movingelevators", cache);
    }

    @Override
    public void generate(){
        this.shaped(MovingElevators.elevator_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ADA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.REDSTONE_DUSTS)
            .input('C', Blocks.OBSERVER)
            .input('D', Blocks.PISTON)
            .unlockedBy(ConventionalItemTags.IRON_INGOTS);
        this.shaped(MovingElevators.display_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ABA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.REDSTONE_DUSTS)
            .input('C', ConventionalItemTags.GLASS_BLOCKS)
            .unlockedBy(MovingElevators.elevator_block);
        this.shaped(MovingElevators.button_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("AAA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.REDSTONE_DUSTS)
            .input('C', Items.ENDER_PEARL)
            .unlockedBy(MovingElevators.elevator_block);
    }
}
