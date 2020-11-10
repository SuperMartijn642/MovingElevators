package com.supermartijn642.movingelevators;

import com.google.gson.JsonParseException;
import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTile extends ElevatorInputTile implements ITickableTileEntity {

    private boolean initialized = false;
    private Direction facing;
    private String name;
    private DyeColor color = DyeColor.GRAY;
    public boolean redstone;
    private boolean lastRedstone;

    public ElevatorBlockTile(){
        super(MovingElevators.elevator_tile);
    }

    @Override
    public void tick(){
        if(!this.initialized){
            this.world.getCapability(ElevatorGroupCapability.CAPABILITY).ifPresent(cap -> cap.add(this));
            this.getGroup().updateFloorData(this, this.name, this.color);
            this.initialized = true;
        }
        if(!this.world.isRemote && this.lastRedstone != this.redstone){
            if(this.redstone)
                this.getGroup().onButtonPress(false, false, this.pos.getY());
            this.lastRedstone = this.redstone;
            this.markDirty();
        }
    }

    public boolean hasPlatform(){
        int startX = this.pos.getX() + this.getFacing().getXOffset() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        int startZ = this.pos.getZ() + this.getFacing().getZOffset() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        for(int x = 0; x < this.getGroup().getSize(); x++){
            for(int z = 0; z < this.getGroup().getSize(); z++){
                BlockPos pos = new BlockPos(startX + x, this.pos.getY() - 1, startZ + z);
                if(this.world.isAirBlock(pos) || this.world.getTileEntity(pos) != null)
                    return false;
                BlockState state = this.world.getBlockState(pos);
                if(state.getBlockHardness(this.world, pos) < 0)
                    return false;
                if(!(state.getShape(this.world, pos).getEnd(Direction.Axis.Y) == 1.0 &&
                    state.getShape(this.world, pos).getStart(Direction.Axis.X) == 0 && state.getShape(this.world, pos).getEnd(Direction.Axis.X) == 1.0 &&
                    state.getShape(this.world, pos).getStart(Direction.Axis.Z) == 0 && state.getShape(this.world, pos).getEnd(Direction.Axis.Z) == 1.0))
                    return false;
            }
        }
        return true;
    }

    public boolean hasSpaceForPlatform(){
        int startX = this.pos.getX() + this.getFacing().getXOffset() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        int startZ = this.pos.getZ() + this.getFacing().getZOffset() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        for(int x = 0; x < this.getGroup().getSize(); x++){
            for(int z = 0; z < this.getGroup().getSize(); z++){
                BlockPos pos = new BlockPos(startX + x, this.pos.getY() - 1, startZ + z);
                if(!this.world.isAirBlock(pos))
                    return false;
            }
        }
        return true;
    }

    @Override
    public Direction getFacing(){
        if(this.facing == null)
            this.facing = this.world.getBlockState(pos).get(ElevatorBlock.FACING);
        return this.facing;
    }

    @Override
    protected CompoundNBT getChangedData(){
        CompoundNBT data = super.getChangedData();
        if(this.name != null)
            data.putString("name", ITextComponent.Serializer.toJson(new StringTextComponent(this.name)));
        data.putInt("color", this.color.getId());
        data.putBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected CompoundNBT getAllData(){
        CompoundNBT data = super.getAllData();
        if(this.name != null)
            data.putString("name", ITextComponent.Serializer.toJson(new StringTextComponent(this.name)));
        data.putInt("color", this.color.getId());
        data.putBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected void handleData(CompoundNBT data){
        super.handleData(data);
        if(data.contains("name")){
            try{
                this.name = ITextComponent.Serializer.func_240643_a_(data.getString("name")).getStringTruncated(Integer.MAX_VALUE);
            }catch(JsonParseException ignore){
                this.name = data.getString("name");
            }
        }else
            this.name = null;
        if(data.contains("color"))
            this.color = DyeColor.byId(data.getInt("color"));
        if(data.contains("redstone")){
            this.redstone = data.getBoolean("redstone");
            this.lastRedstone = this.redstone;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    public void onBreak(){
        this.world.getCapability(ElevatorGroupCapability.CAPABILITY).ifPresent(groups -> groups.remove(this));
    }

    @Override
    public double getMaxRenderDistanceSquared(){
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
        return this.world.getCapability(ElevatorGroupCapability.CAPABILITY).map(groups -> groups.getGroup(this)).orElse(null);
    }

    @Override
    public boolean hasGroup(){
        return this.initialized;
    }

    @Override
    public int getFloorLevel(){
        return this.pos.getY();
    }
}
