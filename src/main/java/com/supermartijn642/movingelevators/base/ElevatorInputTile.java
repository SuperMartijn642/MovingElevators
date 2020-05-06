package com.supermartijn642.movingelevators.base;

import com.supermartijn642.movingelevators.ElevatorGroup;
import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.item.EnumDyeColor;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public abstract class ElevatorInputTile extends METile {

    public abstract boolean hasGroup();

    public abstract ElevatorGroup getGroup();

    public abstract String getFloorName();

    public int getDisplayHeight(){
        if(this.world.getBlockState(this.pos.up()).getBlock() == MovingElevators.display_block){
            if(this.world.getBlockState(this.pos.up(2)).getBlock() == MovingElevators.display_block)
                return 2;
            return 1;
        }
        return 0;
    }

    public abstract EnumDyeColor getDisplayLabelColor();

    public abstract int getFloorLevel();
}
