package com.supermartijn642.movingelevators;

import com.google.gson.JsonParseException;
import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTile extends ElevatorInputTile {

    private boolean initialized = false;
    private Direction facing;
    private String name;
    private DyeColor color = DyeColor.GRAY;

    public ElevatorBlockTile(){
        super(MovingElevators.elevator_tile);
    }

    @Override
    public void tick(){
        super.tick();
        if(!this.initialized){
            this.level.getCapability(ElevatorGroupCapability.CAPABILITY).ifPresent(cap -> cap.add(this));
            this.getGroup().updateFloorData(this, this.name, this.color);
            this.initialized = true;
        }
    }

    public boolean hasPlatform(){
        int startX = this.worldPosition.getX() + this.getFacing().getStepX() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        int startZ = this.worldPosition.getZ() + this.getFacing().getStepZ() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        for(int x = 0; x < this.getGroup().getSize(); x++){
            for(int z = 0; z < this.getGroup().getSize(); z++){
                BlockPos pos = new BlockPos(startX + x, this.worldPosition.getY() - 1, startZ + z);
                if(this.level.isEmptyBlock(pos) || this.level.getBlockEntity(pos) != null)
                    return false;
                BlockState state = this.level.getBlockState(pos);
                if(state.getDestroySpeed(this.level, pos) < 0)
                    return false;
                if(!(state.getShape(this.level, pos).max(Direction.Axis.Y) == 1.0 &&
                    state.getShape(this.level, pos).min(Direction.Axis.X) == 0 && state.getShape(this.level, pos).max(Direction.Axis.X) == 1.0 &&
                    state.getShape(this.level, pos).min(Direction.Axis.Z) == 0 && state.getShape(this.level, pos).max(Direction.Axis.Z) == 1.0))
                    return false;
            }
        }
        return true;
    }

    public boolean hasSpaceForPlatform(){
        int startX = this.worldPosition.getX() + this.getFacing().getStepX() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        int startZ = this.worldPosition.getZ() + this.getFacing().getStepZ() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        for(int x = 0; x < this.getGroup().getSize(); x++){
            for(int z = 0; z < this.getGroup().getSize(); z++){
                BlockPos pos = new BlockPos(startX + x, this.worldPosition.getY() - 1, startZ + z);
                if(!this.level.isEmptyBlock(pos))
                    return false;
            }
        }
        return true;
    }

    @Override
    public Direction getFacing(){
        if(this.facing == null)
            this.facing = this.level.getBlockState(worldPosition).getValue(ElevatorBlock.FACING);
        return this.facing;
    }

    @Override
    protected CompoundNBT getChangedData(){
        CompoundNBT data = super.getChangedData();
        if(this.name != null)
            data.putString("name", ITextComponent.Serializer.toJson(new StringTextComponent(this.name)));
        data.putInt("color", this.color.getId());
        return data;
    }

    protected CompoundNBT getAllData(){
        CompoundNBT data = super.getAllData();
        if(this.name != null)
            data.putString("name", ITextComponent.Serializer.toJson(new StringTextComponent(this.name)));
        data.putInt("color", this.color.getId());
        return data;
    }

    protected void handleData(CompoundNBT data){
        super.handleData(data);
        if(data.contains("name")){
            try{
                this.name = ITextComponent.Serializer.fromJson(data.getString("name")).getString(Integer.MAX_VALUE);
            }catch(JsonParseException ignore){
                this.name = data.getString("name");
            }
        }else
            this.name = null;
        if(data.contains("color"))
            this.color = DyeColor.byId(data.getInt("color"));
    }

    public void onBreak(){
        this.level.getCapability(ElevatorGroupCapability.CAPABILITY).ifPresent(groups -> groups.remove(this));
    }

    @Override
    public double getViewDistance(){
        return 255 * 255 * 4;
    }

    @Override
    public String getFloorName(){
        return this.name;
    }

    public void setFloorName(String name){
        this.name = name;
        this.dataChanged();
        if(this.hasGroup())
            this.getGroup().updateFloorData(this, this.name, this.color);
    }

    public void setDisplayLabelColor(DyeColor color){
        this.color = color;
        this.dataChanged();
        if(this.hasGroup())
            this.getGroup().updateFloorData(this, this.name, this.color);
    }

    @Override
    public DyeColor getDisplayLabelColor(){
        return this.color;
    }

    @Override
    public ElevatorGroup getGroup(){
        return this.level.getCapability(ElevatorGroupCapability.CAPABILITY).map(groups -> groups.getGroup(this)).orElse(null);
    }

    @Override
    public boolean hasGroup(){
        return this.initialized;
    }

    @Override
    public int getFloorLevel(){
        return this.worldPosition.getY();
    }
}
