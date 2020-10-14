package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
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
public class ElevatorBlockTile extends ElevatorInputTile implements ITickable {

    private ElevatorGroup group;
    private String name;
    private EnumDyeColor color = EnumDyeColor.GRAY;
    private EnumFacing facing;
    public boolean redstone;
    private boolean lastRedstone;

    public ElevatorBlockTile(){
    }

    @Override
    public void update(){
        if(this.facing == null){
            this.facing = this.getBlockState().getValue(ElevatorBlock.FACING);
            if(this.group != null && this.group.getLowest() == this.pos.getY())
                this.group.setFacing(this.facing);
            this.markDirty();
        }
        if(this.group != null){
            if(this.group.getLowest() == this.pos.getY())
                this.group.update(this);
            if(!this.world.isRemote && this.lastRedstone != this.redstone){
                if(this.redstone)
                    this.group.onButtonPress(false, false, this.pos.getY());
                this.lastRedstone = this.redstone;
                this.markDirty();
            }
        }else if(!this.world.isRemote){
            ArrayList<ElevatorBlockTile> tiles = new ArrayList<>(1);
            tiles.add(this);
            for(int y = Math.max(0, this.pos.getY() - 255); y <= Math.min(this.world.getHeight(), this.pos.getY() + 255); y++){
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
                BlockPos pos = new BlockPos(startX + x, this.pos.getY() - 1, startZ + z);
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

    @Override
    protected NBTTagCompound getChangedData(){
        return this.getAllData();
    }

    protected NBTTagCompound getAllData(){
        NBTTagCompound data = super.getAllData();
        if(this.name != null)
            data.setString("name", this.name);
        data.setInteger("color", this.color.getMetadata());
        if(this.facing != null)
            data.setInteger("facing", this.facing.getIndex());
        if(this.group != null && this.pos.getY() == this.group.getLowest())
            data.setTag("group", this.group.write());
        data.setBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected void handleData(NBTTagCompound tag){
        super.handleData(tag);
        if(tag.hasKey("moving") && tag.getBoolean("moving")){ // for older versions
            if(this.group == null)
                this.group = new ElevatorGroup(this.world, this.pos.getX(), this.pos.getZ(), this.facing);
            this.group.read(tag);
        }
        this.name = tag.hasKey("name") ? tag.getString("name") : null;
        if(tag.hasKey("color"))
            this.color = EnumDyeColor.byMetadata(tag.getInteger("color"));
        if(tag.hasKey("facing"))
            this.facing = EnumFacing.getFront(tag.getInteger("facing"));
        if(tag.hasKey("group")){
            if(this.group == null)
                this.group = new ElevatorGroup(this.world, this.pos.getX(), this.pos.getZ(), this.facing);
            this.group.read(tag.getCompoundTag("group"));
        }
        if(tag.hasKey("redstone")){
            this.redstone = tag.getBoolean("redstone");
            this.lastRedstone = this.redstone;
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
        return 255 * 255 * 4;
    }

    public String getDefaultFloorName(){
        if(this.world == null || !this.world.isRemote || this.group == null || this.pos == null)
            return null;
        return ClientProxy.translate("movingelevators.floorname").replace("$number$", Integer.toString(this.group.getFloorNumber(this.pos.getY())));
    }

    @Override
    public String getFloorName(){
        return this.name == null ? this.getDefaultFloorName() : this.name;
    }

    public void setFloorName(String name){
        this.name = name;
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
        this.markDirty();
    }

    public void setDisplayLabelColor(EnumDyeColor color){
        this.color = color;
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
        this.markDirty();
    }

    @Override
    public EnumDyeColor getDisplayLabelColor(){
        return this.color;
    }

    @Override
    public ElevatorGroup getGroup(){
        return this.group;
    }

    public void setGroup(ElevatorGroup group){
        this.group = group;
    }

    @Override
    public boolean hasGroup(){
        return this.group != null;
    }

    @Override
    public int getFloorLevel(){
        return this.pos.getY();
    }
}
