package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ButtonBlockTile extends ElevatorInputTile {

    private Direction facing = Direction.NORTH;
    private Direction lastFacing = facing;
    private BlockPos controllerPos = BlockPos.ZERO;

    public ButtonBlockTile(){
        super(MovingElevators.button_tile);
    }

    public void setValues(Direction facing, BlockPos controllerPos){
        this.facing = facing;
        this.controllerPos = controllerPos;
        this.world.notifyBlockUpdate(this.pos,this.getBlockState(),this.getBlockState(),2);
        this.markDirty();
    }

    @Override
    protected CompoundNBT getChangedData(){
        CompoundNBT data = super.getChangedData();
        if(this.lastFacing != this.facing){
            data.putInt("facing",this.facing.getIndex());
            this.lastFacing = this.facing;
        }
        return data;
    }

    @Override
    protected CompoundNBT getAllData(){
        CompoundNBT data = super.getAllData();
        data.putInt("facing", this.facing.getIndex());
        data.putInt("controllerX", this.controllerPos.getX());
        data.putInt("controllerY", this.controllerPos.getY());
        data.putInt("controllerZ", this.controllerPos.getZ());
        return data;
    }

    @Override
    protected void handleData(CompoundNBT data){
        super.handleData(data);
        if(data.contains("facing"))
            this.facing = Direction.byIndex(data.getInt("facing"));
        if(data.contains("controllerX"))
            this.controllerPos = new BlockPos(data.getInt("controllerX"),data.getInt("controllerY"),data.getInt("controllerZ"));
    }

    @Override
    public Direction getFacing(){
        return this.facing;
    }

    public ElevatorBlockTile getController(){
        if(this.world == null || this.controllerPos == null)
            return null;
        TileEntity tile = this.world.getTileEntity(this.controllerPos);
        return tile instanceof ElevatorBlockTile ? (ElevatorBlockTile) tile : null;
    }

    @Override
    public boolean hasGroup(){
        ElevatorBlockTile controller = this.getController();
        return controller != null && controller.hasGroup();
    }

    @Override
    public ElevatorGroup getGroup(){
        ElevatorBlockTile controller = this.getController();
        return controller == null ? null : controller.getGroup();
    }

    @Override
    public String getFloorName(){
        ElevatorBlockTile controller = this.getController();
        return controller == null ? null : controller.getFloorName();
    }

    @Override
    public DyeColor getDisplayLabelColor(){
        ElevatorBlockTile controller = this.getController();
        return controller == null ? null : controller.getDisplayLabelColor();
    }

    @Override
    public int getFloorLevel(){
        return this.controllerPos.getY();
    }
}
