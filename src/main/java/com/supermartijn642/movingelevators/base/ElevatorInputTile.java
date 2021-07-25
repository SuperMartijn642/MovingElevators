package com.supermartijn642.movingelevators.base;

import com.supermartijn642.movingelevators.ElevatorGroup;
import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public abstract class ElevatorInputTile extends METile implements ITickableTileEntity {

    public boolean redstone;
    private boolean lastRedstone;

    public ElevatorInputTile(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
    }

    @Override
    public void tick(){
        if(!this.level.isClientSide && this.lastRedstone != this.redstone){
            if(this.redstone)
                this.getGroup().onButtonPress(false, false, this.worldPosition.getY());
            this.lastRedstone = this.redstone;
            this.setChanged();
        }
    }

    public abstract boolean hasGroup();

    public abstract ElevatorGroup getGroup();

    public abstract String getFloorName();

    public int getDisplayHeight(){
        if(this.level.getBlockState(this.worldPosition.above()).getBlock() == MovingElevators.display_block){
            if(this.level.getBlockState(this.worldPosition.above(2)).getBlock() == MovingElevators.display_block)
                return 2;
            return 1;
        }
        return 0;
    }

    public abstract DyeColor getDisplayLabelColor();

    /**
     * @return the y level of the floor of this controller
     */
    public abstract int getFloorLevel();

    @Override
    protected CompoundNBT getChangedData(){
        CompoundNBT data = super.getChangedData();
        data.putBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected CompoundNBT getAllData(){
        CompoundNBT data = super.getAllData();
        data.putBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected void handleData(CompoundNBT data){
        super.handleData(data);
        if(data.contains("redstone")){
            this.redstone = data.getBoolean("redstone");
            this.lastRedstone = this.redstone;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return new AxisAlignedBB(this.worldPosition, this.worldPosition.above().above().east().south());
    }
}
