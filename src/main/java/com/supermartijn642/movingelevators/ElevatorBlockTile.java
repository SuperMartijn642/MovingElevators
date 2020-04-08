package com.supermartijn642.movingelevators;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlockTile extends METile implements ITickable {

//    private static final int MAX_NAME_CHARACTER_COUNT = 11;

    private ElevatorGroup group;
    private String name;
    private EnumFacing facing;

    public ElevatorBlockTile(){
    }

    @Override
    public void update(){
        if(this.facing == null){
            this.facing = this.getBlockState().getValue(ElevatorBlock.FACING);
            if(this.group != null && this.group.getLowest() == this.pos.getY())
                this.group.setFacing(this.facing);
        }
        if(this.group != null){
            if(this.group.getLowest() == this.pos.getY())
                this.group.update(this);
        }else if(!this.world.isRemote){
            ArrayList<ElevatorBlockTile> tiles = new ArrayList<>(1);
            tiles.add(this);
            for(int y = 0; y <= this.world.getHeight(); y++){
                if(y == this.pos.getY())
                    continue;
                TileEntity tile = this.world.getTileEntity(new BlockPos(this.pos.getX(), y, this.pos.getZ()));
                if(tile instanceof ElevatorBlockTile){
                    ElevatorBlockTile elevator = (ElevatorBlockTile)tile;
                    if(elevator.getFacing() == this.getFacing()){
                        if(elevator.group == null)
                            tiles.add(elevator);
                        else{
                            for(ElevatorBlockTile elevator2 : tiles)
                                elevator.group.add(elevator2);
                            return;
                        }
                    }
                }
            }
            this.group = new ElevatorGroup(this.world, this.pos.getX(), this.pos.getZ(), this.getFacing());
            for(ElevatorBlockTile tile : tiles)
                this.group.add(tile);
        }
    }

    public boolean hasPlatform(){
        int startX = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(this.group.getSize() / 2f) - this.group.getSize() / 2;
        int startZ = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(this.group.getSize() / 2f) - this.group.getSize() / 2;
        for(int x = 0; x < this.group.getSize(); x++){
            for(int z = 0; z < this.group.getSize(); z++){
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
        int startX = this.pos.getX() + this.facing.getFrontOffsetX() * (int)Math.ceil(this.group.getSize() / 2f) - this.group.getSize() / 2;
        int startZ = this.pos.getZ() + this.facing.getFrontOffsetZ() * (int)Math.ceil(this.group.getSize() / 2f) - this.group.getSize() / 2;
        for(int x = 0; x < this.group.getSize(); x++){
            for(int z = 0; z < this.group.getSize(); z++){
                BlockPos pos = new BlockPos(startX + x, this.pos.getY(), startZ + z);
                if(!this.world.isAirBlock(pos))
                    return false;
            }
        }
        return true;
    }

    @Override
    public EnumFacing getFacing(){
        return this.facing;
    }

    protected NBTTagCompound getDataTag(){
        NBTTagCompound data = super.getDataTag();
        if(this.name != null)
            data.setString("name", this.name);
        if(this.facing != null)
            data.setInteger("facing", this.facing.getIndex());
        if(this.group != null && this.pos.getY() == this.group.getLowest())
            data.setTag("group", this.group.write());
        return data;
    }

    protected void handleDataTag(NBTTagCompound tag){
        super.handleDataTag(tag);
        if(tag.hasKey("moving") && tag.getBoolean("moving")){ // for older versions
            if(this.group == null)
                this.group = new ElevatorGroup(this.world, this.pos.getX(), this.pos.getZ(), this.facing);
            this.group.read(tag);
        }
        this.name = tag.hasKey("name") ? tag.getString("name") : null;
        if(tag.hasKey("facing"))
            this.facing = EnumFacing.getFront(tag.getInteger("facing"));
        if(tag.hasKey("group")){
            if(this.group == null)
                this.group = new ElevatorGroup(this.world, this.pos.getX(), this.pos.getZ(), this.facing);
            this.group.read(tag.getCompoundTag("group"));
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    public void onBreak(){
        this.group.remove(this.pos.getY());
    }

    @Override
    public double getMaxRenderDistanceSquared(){
        return this.world.getHeight() * this.world.getHeight() * 4;
    }

    public int getDisplayHeight(){
        if(this.world.getBlockState(this.pos.up()).getBlock() == MovingElevators.display_block){
            if(this.world.getBlockState(this.pos.up(2)).getBlock() == MovingElevators.display_block)
                return 2;
            return 1;
        }
        return 0;
    }

    public String getName(){
        return this.name == null ? I18n.format("movingelevators.floorname").replace("$number$", Integer.toString(this.group.getFloorNumber(this.pos.getY()))) : this.name;
    }

    public void setName(String name){
        this.name = name;
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
    }

    public ElevatorGroup getGroup(){
        return this.group;
    }

    public void setGroup(ElevatorGroup group){
        this.group = group;
    }

    public boolean hasGroup(){
        return this.group != null;
    }
}
