package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public abstract class ElevatorInputBlockEntity extends CamoBlockEntity implements TickableBlockEntity {

    public boolean redstone;
    private boolean lastRedstone;

    public ElevatorInputBlockEntity(BaseBlockEntityType<?> blockEntityType){
        super(blockEntityType);
    }

    @Override
    public void update(){
        if(!this.world.isRemote && this.lastRedstone != this.redstone){
            if(this.redstone)
                this.getGroup().onButtonPress(false, false, this.getFloorLevel());
            this.lastRedstone = this.redstone;
            this.markDirty();
        }
    }

    public abstract boolean hasGroup();

    public abstract ElevatorGroup getGroup();

    public abstract String getFloorName();

    public abstract EnumDyeColor getDisplayLabelColor();

    /**
     * @return the y level of the floor of this controller
     */
    public abstract int getFloorLevel();

    public abstract EnumFacing getFacing();

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = super.writeData();
        compound.setBoolean("redstone", this.lastRedstone);
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        super.readData(compound);
        this.redstone = compound.getBoolean("redstone");
        this.lastRedstone = this.redstone;
    }
}
