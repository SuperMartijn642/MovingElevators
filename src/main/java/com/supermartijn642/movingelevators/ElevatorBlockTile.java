package com.supermartijn642.movingelevators;

import com.google.gson.JsonParseException;
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

import java.util.ArrayList;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTile extends METile implements ITickableTileEntity {

    private ElevatorGroup group;
    private String name;
    private DyeColor color = DyeColor.GRAY;
    private Direction facing;
    public boolean redstone;
    private boolean lastRedstone;

    public ElevatorBlockTile(){
        super(MovingElevators.elevator_tile);
    }

    @Override
    public void tick(){
        if(this.facing == null){
            this.facing = this.getBlockState().get(ElevatorBlock.FACING);
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
            for(int y = 0; y <= this.world.getMaxHeight(); y++){
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
        int startX = this.pos.getX() + this.getFacing().getXOffset() * (int)Math.ceil(this.group.getSize() / 2f) - this.group.getSize() / 2;
        int startZ = this.pos.getZ() + this.getFacing().getZOffset() * (int)Math.ceil(this.group.getSize() / 2f) - this.group.getSize() / 2;
        for(int x = 0; x < this.group.getSize(); x++){
            for(int z = 0; z < this.group.getSize(); z++){
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
        int startX = this.pos.getX() + this.facing.getXOffset() * (int)Math.ceil(this.group.getSize() / 2f) - this.group.getSize() / 2;
        int startZ = this.pos.getZ() + this.facing.getZOffset() * (int)Math.ceil(this.group.getSize() / 2f) - this.group.getSize() / 2;
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
    public Direction getFacing(){
        return this.facing;
    }

    protected CompoundNBT getDataTag(){
        CompoundNBT data = super.getDataTag();
        if(this.name != null)
            data.putString("name", ITextComponent.Serializer.toJson(new StringTextComponent(this.name)));
        data.putInt("color", this.color.getId());
        if(this.facing != null)
            data.putInt("facing", this.facing.getIndex());
        if(this.group != null && this.pos.getY() == this.group.getLowest())
            data.put("group", this.group.write());
        data.putBoolean("redstone", this.lastRedstone);
        return data;
    }

    protected void handleDataTag(CompoundNBT tag){
        super.handleDataTag(tag);
        if(tag.contains("moving") && tag.getBoolean("moving")){ // for older versions
            if(this.group == null)
                this.group = new ElevatorGroup(this.world, this.pos.getX(), this.pos.getZ(), this.facing);
            this.group.read(tag);
        }
        if(tag.contains("name")){
            try{
                this.name = ITextComponent.Serializer.fromJson(tag.getString("name")).getFormattedText();
            }catch(JsonParseException ignore){
                this.name = tag.getString("name");
            }
        }else
            this.name = null;
        if(tag.contains("color"))
            this.color = DyeColor.byId(tag.getInt("color"));
        if(tag.contains("facing"))
            this.facing = Direction.byIndex(tag.getInt("facing"));
        if(tag.contains("group")){
            if(this.group == null)
                this.group = new ElevatorGroup(this.world, this.pos.getX(), this.pos.getZ(), this.facing);
            this.group.read(tag.getCompound("group"));
        }
        if(tag.contains("redstone")){
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
        return this.world.getMaxHeight() * this.world.getMaxHeight() * 4;
    }

    public int getDisplayHeight(){
        if(this.world.getBlockState(this.pos.up()).getBlock() == MovingElevators.display_block){
            if(this.world.getBlockState(this.pos.up(2)).getBlock() == MovingElevators.display_block)
                return 2;
            return 1;
        }
        return 0;
    }

    public String getDefaultName(){
        if(this.world == null || !this.world.isRemote || this.group == null || this.pos == null)
            return null;
        return ClientProxy.translate("movingelevators.floorname").replace("$number$", Integer.toString(this.group.getFloorNumber(this.pos.getY())));
    }

    public String getName(){
        return this.name == null ? this.getDefaultName() : this.name;
    }

    public void setName(String name){
        this.name = name;
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
        this.markDirty();
    }

    public void setDisplayLabelColor(DyeColor color){
        this.color = color;
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
        this.markDirty();
    }

    public DyeColor getDisplayLabelColor(){
        return this.color;
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
