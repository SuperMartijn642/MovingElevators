package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public abstract class ElevatorInputBlockEntity extends CamoBlockEntity implements TickableBlockEntity {

    public boolean redstone;
    private boolean lastRedstone;

    public ElevatorInputBlockEntity(BaseBlockEntityType<?> blockEntityType, BlockPos pos, BlockState state){
        super(blockEntityType, pos, state);
    }

    @Override
    public void update(){
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
    protected CompoundTag writeData(){
        CompoundTag compound = super.writeData();
        compound.putBoolean("redstone", this.lastRedstone);
        return compound;
    }

    @Override
    protected void readData(CompoundTag compound){
        super.readData(compound);
        this.redstone = compound.getBoolean("redstone");
        this.lastRedstone = this.redstone;
    }
}
