package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class DisplayBlockEntity extends CamoBlockEntity {

    public DisplayBlockEntity(){
        super();
    }

    public EnumFacing getFacing(){
        ElevatorInputBlockEntity entity = this.getInputBlockEntity();
        return entity == null ? null : entity.getFacing();
    }

    public boolean isBottomDisplay(){
        return this.world.getTileEntity(this.pos.down()) instanceof ElevatorInputBlockEntity;
    }

    public boolean hasDisplayOnTop(){
        return this.world.getTileEntity(this.pos.up()) instanceof DisplayBlockEntity;
    }

    public int getDisplayCategory(){
        TileEntity tile = this.world.getTileEntity(this.pos.down());
        if(tile instanceof ElevatorInputBlockEntity){
            tile = this.world.getTileEntity(this.pos.up());
            if(tile instanceof DisplayBlockEntity)
                return 2;
            return 1;
        }
        if(tile instanceof DisplayBlockEntity){
            tile = this.world.getTileEntity(this.pos.down(2));
            if(tile instanceof ElevatorInputBlockEntity)
                return 3;
            return 0;
        }
        return 0;
    }

    public ElevatorInputBlockEntity getInputBlockEntity(){
        TileEntity entity = this.world.getTileEntity(this.pos.down());
        if(entity instanceof ElevatorInputBlockEntity)
            return (ElevatorInputBlockEntity)entity;
        else if(entity instanceof DisplayBlockEntity && (entity = this.world.getTileEntity(this.pos.down(2))) instanceof ElevatorInputBlockEntity)
            return (ElevatorInputBlockEntity)entity;
        return null;
    }

    public ElevatorGroup getElevatorGroup(){
        ElevatorInputBlockEntity inputEntity = this.getInputBlockEntity();
        return inputEntity == null ? null : inputEntity.getGroup();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return new AxisAlignedBB(this.pos, this.pos.add(1, 2, 1));
    }
}
