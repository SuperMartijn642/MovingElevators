package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import com.supermartijn642.movingelevators.base.METile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class DisplayBlockTile extends METile {

    public DisplayBlockTile(){
        super(MovingElevators.display_tile);
    }

    @Override
    public Direction getFacing(){
        TileEntity tile = this.level.getBlockEntity(this.worldPosition.below());
        if(tile instanceof METile)
            return ((METile)tile).getFacing();
        return null;
    }

    public int getDisplayCategory(){
        TileEntity tile = this.level.getBlockEntity(this.worldPosition.below());
        if(tile instanceof ElevatorInputTile){
            tile = this.level.getBlockEntity(this.worldPosition.above());
            if(tile instanceof DisplayBlockTile)
                return 2;
            return 1;
        }
        if(tile instanceof DisplayBlockTile){
            tile = this.level.getBlockEntity(this.worldPosition.below(2));
            if(tile instanceof ElevatorInputTile)
                return 3;
            return 0;
        }
        return 0;
    }

}
