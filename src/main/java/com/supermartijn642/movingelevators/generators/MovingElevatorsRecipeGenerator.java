package com.supermartijn642.movingelevators.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

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
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.DUSTS_REDSTONE)
            .input('C', Blocks.OBSERVER)
            .input('D', Blocks.PISTON)
            .unlockedBy(Tags.Items.INGOTS_IRON);
        this.shaped(MovingElevators.display_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ABA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.DUSTS_REDSTONE)
            .input('C', Tags.Items.GLASS)
            .unlockedBy(MovingElevators.elevator_block);
        this.shaped(MovingElevators.button_block)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("AAA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.DUSTS_REDSTONE)
            .input('C', Tags.Items.ENDER_PEARLS)
            .unlockedBy(MovingElevators.elevator_block);
    }
}
