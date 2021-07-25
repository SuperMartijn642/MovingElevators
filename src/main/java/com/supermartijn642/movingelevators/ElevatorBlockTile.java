package com.supermartijn642.movingelevators;

import com.google.gson.JsonParseException;
import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTile extends ElevatorInputTile {

    private boolean initialized = false;
    private Direction facing;
    private String name;
    private DyeColor color = DyeColor.GRAY;

    public ElevatorBlockTile(BlockPos pos, BlockState state){
        super(MovingElevators.elevator_tile, pos, state);
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
            this.facing = this.level.getBlockState(this.worldPosition).getValue(ElevatorBlock.FACING);
        return this.facing;
    }

    @Override
    protected CompoundTag getChangedData(){
        CompoundTag data = super.getChangedData();
        if(this.name != null)
            data.putString("name", Component.Serializer.toJson(new TextComponent(this.name)));
        data.putInt("color", this.color.getId());
        return data;
    }

    protected CompoundTag getAllData(){
        CompoundTag data = super.getAllData();
        if(this.name != null)
            data.putString("name", Component.Serializer.toJson(new TextComponent(this.name)));
        data.putInt("color", this.color.getId());
        return data;
    }

    protected void handleData(CompoundTag data){
        super.handleData(data);
        if(data.contains("name")){
            try{
                this.name = Component.Serializer.fromJson(data.getString("name")).getString(Integer.MAX_VALUE);
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
