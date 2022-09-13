package com.supermartijn642.movingelevators.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.movingelevators.MovingElevators;

/**
 * Created 05/01/2022 by SuperMartijn642
 */
public class MovingElevatorsTagGenerator extends TagGenerator {

    public MovingElevatorsTagGenerator(ResourceCache cache){
        super("movingelevators", cache);
    }

    @Override
    public void generate(){
        this.blockMineableWithPickaxe().add(MovingElevators.elevator_block).add(MovingElevators.display_block).add(MovingElevators.button_block);
    }
}
