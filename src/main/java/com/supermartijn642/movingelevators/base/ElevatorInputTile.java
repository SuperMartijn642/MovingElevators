package com.supermartijn642.movingelevators.base;

import com.supermartijn642.movingelevators.ElevatorGroup;
import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public abstract class ElevatorInputTile extends METile implements ITickable {

    public boolean redstone;
    private boolean lastRedstone;

    @Override
    public void update(){
        if(!this.world.isRemote && this.lastRedstone != this.redstone){
            System.out.println("Redstone changed!");
            if(this.redstone)
                this.getGroup().onButtonPress(false, false, this.pos.getY());
            this.lastRedstone = this.redstone;
            this.markDirty();
        }
    }

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

    /**
     * @return the y level of the floor of this controller
     */
    public abstract int getFloorLevel();

    @Override
    protected NBTTagCompound getChangedData(){
        NBTTagCompound data = super.getChangedData();
        data.setBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected NBTTagCompound getAllData(){
        NBTTagCompound data = super.getAllData();
        data.setBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected void handleData(NBTTagCompound data){
        super.handleData(data);
        if(data.hasKey("redstone")){
            this.redstone = data.getBoolean("redstone");
            this.lastRedstone = this.redstone;
        }
    }
}
