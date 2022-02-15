package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public abstract class ElevatorInputBlockEntity extends CamoBlockEntity implements ITickableTileEntity {

    public boolean redstone;
    private boolean lastRedstone;

    public ElevatorInputBlockEntity(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
    }

    @Override
    public void tick(){
        if(!this.level.isClientSide && this.lastRedstone != this.redstone){
            if(this.redstone)
                this.getGroup().onButtonPress(false, false, this.getFloorLevel());
            this.lastRedstone = this.redstone;
            this.setChanged();
        }
    }

    public abstract boolean hasGroup();

    public abstract ElevatorGroup getGroup();

    public abstract String getFloorName();

    public abstract DyeColor getDisplayLabelColor();

    /**
     * @return the y level of the floor of this controller
     */
    public abstract int getFloorLevel();

    public abstract Direction getFacing();

    @Override
    protected CompoundNBT writeData(){
        CompoundNBT compound = super.writeData();
        compound.putBoolean("redstone", this.lastRedstone);
        return compound;
    }

    @Override
    protected void readData(CompoundNBT compound){
        super.readData(compound);
        this.redstone = compound.getBoolean("redstone");
        this.lastRedstone = this.redstone;
    }
}
