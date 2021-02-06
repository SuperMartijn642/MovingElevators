package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.METile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class PresenceBlockTile extends METile {
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

    public ElevatorBlockTile getController() {
        if(this.world == null || this.controllerPos == null)
            return null;
        TileEntity tile = this.world.getTileEntity(this.controllerPos);
        return tile instanceof ElevatorBlockTile ? (ElevatorBlockTile)tile : null;
    }
}
