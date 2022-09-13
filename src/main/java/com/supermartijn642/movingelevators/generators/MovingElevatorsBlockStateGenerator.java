package com.supermartijn642.movingelevators.generators;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.movingelevators.MovingElevators;

/**
 * Created 12/09/2022 by SuperMartijn642
 */
public class MovingElevatorsBlockStateGenerator extends BlockStateGenerator {

    public MovingElevatorsBlockStateGenerator(ResourceCache cache){
        super("movingelevators", cache);
    }

    @Override
    public void generate(){
        this.blockState(MovingElevators.elevator_block).variantsForAll((state, builder) -> builder.model("block/elevator_block"));
        this.blockState(MovingElevators.display_block).emptyVariant(builder -> builder.model("block/display_block"));
        this.blockState(MovingElevators.button_block).emptyVariant(builder -> builder.model("block/button_block"));
    }
}
