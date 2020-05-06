package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import com.supermartijn642.movingelevators.base.METile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class DisplayBlockTile extends METile {

    @Override
    public EnumFacing getFacing(){
        TileEntity tile = this.world.getTileEntity(this.pos.down());
        if(tile instanceof METile)
            return ((METile)tile).getFacing();
        return null;
    }

    public int getDisplayCategory(){
        TileEntity tile = this.world.getTileEntity(this.pos.down());
        if(tile instanceof ElevatorInputTile){
            tile = this.world.getTileEntity(this.pos.up());
            if(tile instanceof DisplayBlockTile)
                return 2;
            return 1;
        }
        if(tile instanceof DisplayBlockTile){
            tile = this.world.getTileEntity(this.pos.down(2));
            if(tile instanceof ElevatorInputTile)
                return 3;
            return 0;
        }
        return 0;
    }

}
