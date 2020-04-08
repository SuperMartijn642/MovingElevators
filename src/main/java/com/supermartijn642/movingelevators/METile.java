package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

/**
 * Created 4/6/2020 by SuperMartijn642
 */
public class METile extends TileEntity {

    public METile(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
    }

    private ItemStack camoStack = ItemStack.EMPTY;

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        return new SUpdateTileEntityPacket(this.pos, 0, this.getDataTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.handleDataTag(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT tag = super.getUpdateTag();
        tag.put("info", this.getDataTag());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag){
        super.handleUpdateTag(tag);
        this.handleDataTag(tag.getCompound("info"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound){
        super.write(compound);
        compound.put("info", this.getDataTag());
        return compound;
    }

    @Override
    public void read(CompoundNBT compound){
        super.read(compound);
        this.handleDataTag(compound.getCompound("info"));
    }

    protected CompoundNBT getDataTag(){
        CompoundNBT data = new CompoundNBT();
        data.put("camo", this.camoStack.write(new CompoundNBT()));
        return data;
    }

    protected void handleDataTag(CompoundNBT tag){
        if(tag.contains("camo"))
            this.camoStack = ItemStack.read(tag.getCompound("camo"));
    }

    public boolean setCamoStack(ItemStack stack){
        if(stack == null)
            this.camoStack = ItemStack.EMPTY;
        else
            this.camoStack = stack.copy();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
        return true;
    }

    public boolean canBeCamoStack(ItemStack stack){
        if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem))
            return false;
        Block block = ((BlockItem)stack.getItem()).getBlock();
        return block != MovingElevators.elevator_block && block != MovingElevators.display_block && !block.func_220074_n(block.getDefaultState()) && block.isNormalCube(block.getDefaultState(), this.world, this.pos);
    }

    public BlockState getCamoBlock(){
        if(this.camoStack == null || this.camoStack.isEmpty() || !(this.camoStack.getItem() instanceof BlockItem))
            return null;
        return ((BlockItem)this.camoStack.getItem()).getBlock().getDefaultState();
    }

    public Direction getFacing(){
        TileEntity tile = this.world.getTileEntity(this.pos.down());
        if(tile instanceof METile)
            return ((METile)tile).getFacing();
        return null;
    }

    public int getDisplayCategory(){
        TileEntity tile = this.world.getTileEntity(this.pos.down());
        if(tile instanceof ElevatorBlockTile){
            tile = this.world.getTileEntity(this.pos.up());
            if(tile instanceof METile && !(tile instanceof ElevatorBlockTile))
                return 2;
            return 1;
        }
        if(tile instanceof METile){
            tile = this.world.getTileEntity(this.pos.down(2));
            if(tile instanceof ElevatorBlockTile)
                return 3;
            return 0;
        }
        return 0;
    }
}
