package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlockTile extends ElevatorInputTile {

    private boolean initialized = false;
    private EnumFacing facing;
    private String name;
    private EnumDyeColor color = EnumDyeColor.GRAY;

    public ElevatorBlockTile(){
    }

    @Override
    public void update(){
        if(!this.initialized){
            ElevatorGroupCapability groups = this.world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
            if(groups != null){
                groups.add(this);
                this.getGroup().updateFloorData(this, this.name, this.color);
                this.initialized = true;
            }
        }
    }

    public boolean hasPlatform(){
        int startX = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        int startZ = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        for(int x = 0; x < this.getGroup().getSize(); x++){
            for(int z = 0; z < this.getGroup().getSize(); z++){
                BlockPos pos = new BlockPos(startX + x, this.pos.getY() - 1, startZ + z);
                if(this.world.isAirBlock(pos) || this.world.getTileEntity(pos) != null)
                    return false;
                IBlockState state = this.world.getBlockState(pos);
                if(state.getBlockHardness(this.world, pos) < 0)
                    return false;
                AxisAlignedBB collisionBox = state.getCollisionBoundingBox(this.world, pos);
                if(collisionBox == null || !(collisionBox.maxY == 1.0 &&
                    collisionBox.minX == 0 && collisionBox.maxX == 1.0 &&
                    collisionBox.minZ == 0 && collisionBox.maxZ == 1.0))
                    return false;
            }
        }
        return true;
    }

    public boolean hasSpaceForPlatform(){
        int startX = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
        int startZ = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(this.getGroup().getSize() / 2f) - this.getGroup().getSize() / 2;
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
    public EnumFacing getFacing(){
        if(this.facing == null)
            this.facing = this.world.getBlockState(pos).getValue(ElevatorBlock.FACING);
        return this.facing;
    }

    @Override
    protected NBTTagCompound getChangedData(){
        NBTTagCompound data = super.getChangedData();
        if(this.name != null)
            data.setString("name", this.name);
        data.setInteger("color", this.color.getMetadata());
        return data;
    }

    protected NBTTagCompound getAllData(){
        NBTTagCompound data = super.getAllData();
        if(this.name != null)
            data.setString("name", this.name);
        data.setInteger("color", this.color.getMetadata());
        return data;
    }

    protected void handleData(NBTTagCompound tag){
        super.handleData(tag);
        this.name = tag.hasKey("name") ? tag.getString("name") : null;
        if(tag.hasKey("color"))
            this.color = EnumDyeColor.byMetadata(tag.getInteger("color"));
    }

    public void onBreak(){
        ElevatorGroupCapability groups = this.world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
        if(groups != null)
            groups.remove(this);
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

    public void setDisplayLabelColor(EnumDyeColor color){
        this.color = color;
        this.dataChanged();
        if(this.hasGroup())
            this.getGroup().updateFloorData(this, this.name, this.color);
    }

    @Override
    public EnumDyeColor getDisplayLabelColor(){
        return this.color;
    }

    @Override
    public ElevatorGroup getGroup(){
        ElevatorGroupCapability groups = this.world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
        return groups == null ? null : groups.getGroup(this);
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
