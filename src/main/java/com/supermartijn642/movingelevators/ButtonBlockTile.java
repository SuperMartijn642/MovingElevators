package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ButtonBlockTile extends ElevatorInputTile {

    private EnumFacing facing = EnumFacing.NORTH;
    private EnumFacing lastFacing = facing;
    private BlockPos controllerPos = BlockPos.ORIGIN;

    public void setValues(EnumFacing facing, BlockPos controllerPos){
        this.facing = facing;
        this.controllerPos = controllerPos;
        this.dataChanged();
    }

    @Override
    protected NBTTagCompound getChangedData(){
        NBTTagCompound data = super.getChangedData();
        if(this.lastFacing != this.facing){
            data.setInteger("facing", this.facing.getIndex());
            this.lastFacing = this.facing;
        }
        return data;
    }

    @Override
    protected NBTTagCompound getAllData(){
        NBTTagCompound data = super.getAllData();
        data.setInteger("facing", this.facing.getIndex());
        data.setInteger("controllerX", this.controllerPos.getX());
        data.setInteger("controllerY", this.controllerPos.getY());
        data.setInteger("controllerZ", this.controllerPos.getZ());
        return data;
    }

    @Override
    protected void handleData(NBTTagCompound data){
        super.handleData(data);
        if(data.hasKey("facing"))
            this.facing = EnumFacing.getFront(data.getInteger("facing"));
        if(data.hasKey("controllerX"))
            this.controllerPos = new BlockPos(data.getInteger("controllerX"), data.getInteger("controllerY"), data.getInteger("controllerZ"));
    }

    @Override
    public EnumFacing getFacing(){
        return this.facing;
    }

    public ElevatorBlockTile getController(){
        if(this.world == null || this.controllerPos == null)
            return null;
        TileEntity tile = this.world.getTileEntity(this.controllerPos);
        return tile instanceof ElevatorBlockTile ? (ElevatorBlockTile)tile : null;
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
    public EnumDyeColor getDisplayLabelColor(){
        ElevatorBlockTile controller = this.getController();
        return controller == null ? null : controller.getDisplayLabelColor();
    }

    @Override
    public int getFloorLevel(){
        return this.controllerPos.getY();
    }
}
