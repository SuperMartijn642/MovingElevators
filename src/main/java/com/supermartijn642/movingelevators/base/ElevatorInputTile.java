package com.supermartijn642.movingelevators.base;

import com.supermartijn642.movingelevators.ElevatorGroup;
import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public abstract class ElevatorInputTile extends METile {

    public boolean redstone;
    private boolean lastRedstone;

    public ElevatorInputTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state){
        super(tileEntityTypeIn, pos, state);
    }

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
    protected CompoundTag getChangedData(){
        CompoundTag data = super.getChangedData();
        data.putBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected CompoundTag getAllData(){
        CompoundTag data = super.getAllData();
        data.putBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected void handleData(CompoundTag data){
        super.handleData(data);
        if(data.contains("redstone")){
            this.redstone = data.getBoolean("redstone");
            this.lastRedstone = this.redstone;
        }
    }

    @Override
    public AABB getRenderBoundingBox(){
        return new AABB(this.worldPosition, this.worldPosition.above().above().east().south());
    }
}
