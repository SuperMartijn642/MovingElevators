package com.supermartijn642.movingelevators.base;

import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
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
public abstract class METile extends TileEntity {

    public METile(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
    }

    private BlockState camoState = Blocks.AIR.getDefaultState();
    private BlockState lastCamoState = this.camoState;

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        CompoundNBT compound = this.getChangedData();
        return compound.isEmpty() ? null : new SUpdateTileEntityPacket(this.pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.handleData(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT tag = super.getUpdateTag();
        tag.put("info", this.getAllData());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag){
        super.handleUpdateTag(tag);
        this.handleData(tag.getCompound("info"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound){
        super.write(compound);
        compound.put("info", this.getAllData());
        return compound;
    }

    @Override
    public void read(CompoundNBT compound){
        super.read(compound);
        this.handleData(compound.getCompound("info"));
    }

    protected CompoundNBT getChangedData(){
        CompoundNBT data = new CompoundNBT();
        if(this.lastCamoState != this.camoState){
            data.putInt("camoState", Block.getStateId(this.camoState));
            this.lastCamoState = this.camoState;
        }
        return data;
    }

    protected CompoundNBT getAllData(){
        CompoundNBT data = new CompoundNBT();
        data.putInt("camoState", Block.getStateId(this.camoState));
        return data;
    }

    protected void handleData(CompoundNBT data){
        if(data.contains("camoState"))
            this.camoState = Block.getStateById(data.getInt("camoState"));
        else if(data.contains("hasCamo")){ // Do this for older versions
            if(data.getBoolean("hasCamo"))
                this.camoState = Block.getStateById(data.getInt("camo"));
            else
                this.camoState = Blocks.AIR.getDefaultState();
        }else if(data.contains("camo")){ // Do this for older versions
            ItemStack camoStack = ItemStack.read(data.getCompound("camo"));
            Item item = camoStack.getItem();
            if(item instanceof BlockItem){
                Block block = ((BlockItem)item).getBlock();
                this.camoState = block.getDefaultState();
            }
        }
    }

    public boolean setCamoState(BlockState state){
        this.camoState = state == null ? Blocks.AIR.getDefaultState() : state;
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
        this.markDirty();
        return true;
    }

    public boolean canBeCamoStack(ItemStack stack){
        if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem))
            return false;
        Block block = ((BlockItem)stack.getItem()).getBlock();
        return block != MovingElevators.elevator_block && block != MovingElevators.display_block && block != MovingElevators.button_block && !block.isTransparent(block.getDefaultState()) && block.isNormalCube(block.getDefaultState(), this.world, this.pos);
    }

    public BlockState getCamoBlock(){
        if(this.camoState.getBlock() == Blocks.AIR)
            return this.getBlockState();
        return this.camoState;
    }

    public abstract Direction getFacing();

}
