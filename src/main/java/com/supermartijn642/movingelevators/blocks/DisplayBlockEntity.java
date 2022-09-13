package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class DisplayBlockEntity extends CamoBlockEntity {

    public DisplayBlockEntity(){
        super(MovingElevators.display_tile);
    }

    public Direction getFacing(){
        ElevatorInputBlockEntity entity = this.getInputBlockEntity();
        return entity == null ? null : entity.getFacing();
    }

    public boolean isBottomDisplay(){
        return this.level.getBlockEntity(this.worldPosition.below()) instanceof ElevatorInputBlockEntity;
    }

    public boolean hasDisplayOnTop(){
        return this.level.getBlockEntity(this.worldPosition.above()) instanceof DisplayBlockEntity;
    }

    public int getDisplayCategory(){
        TileEntity entity = this.level.getBlockEntity(this.worldPosition.below());
        if(entity instanceof ElevatorInputBlockEntity){
            entity = this.level.getBlockEntity(this.worldPosition.above());
            if(entity instanceof DisplayBlockEntity)
                return 2;
            return 1;
        }
        if(entity instanceof DisplayBlockEntity){
            entity = this.level.getBlockEntity(this.worldPosition.below(2));
            if(entity instanceof ElevatorInputBlockEntity)
                return 3;
            return 0;
        }
        return 0;
    }

    public ElevatorInputBlockEntity getInputBlockEntity(){
        TileEntity entity = this.level.getBlockEntity(this.worldPosition.below());
        if(entity instanceof ElevatorInputBlockEntity)
            return (ElevatorInputBlockEntity)entity;
        else if(entity instanceof DisplayBlockEntity && (entity = this.level.getBlockEntity(this.worldPosition.below(2))) instanceof ElevatorInputBlockEntity)
            return (ElevatorInputBlockEntity)entity;
        return null;
    }

    public ElevatorGroup getElevatorGroup(){
        ElevatorInputBlockEntity inputEntity = this.getInputBlockEntity();
        return inputEntity == null ? null : inputEntity.getGroup();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return new AxisAlignedBB(this.worldPosition, this.worldPosition.offset(1,2,1));
    }
}
