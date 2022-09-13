package com.supermartijn642.movingelevators.generators;

import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.movingelevators.MovingElevators;

/**
 * Created 14/02/2022 by SuperMartijn642
 */
public class MovingElevatorsLootTableGenerator extends LootTableGenerator {

    public MovingElevatorsLootTableGenerator(ResourceCache cache){
        super("movingelevators", cache);
    }

    @Override
    public void generate(){
        this.dropSelf(MovingElevators.elevator_block);
        this.dropSelf(MovingElevators.display_block);
        this.dropSelf(MovingElevators.button_block);
    }
}
