package com.supermartijn642.movingelevators.base;

import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

/**
 * Created 4/6/2020 by SuperMartijn642
 */
public abstract class METile extends TileEntity {

    public METile(){
        super();
    }

    private IBlockState camoState = Blocks.AIR.getDefaultState();

    private boolean dataChanged = false;

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        if(!this.dataChanged)
            return null;
        this.dataChanged = false;
        NBTTagCompound compound = this.getChangedData();
        return compound == null || compound.hasNoTags() ? null : new SPacketUpdateTileEntity(this.pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.handleData(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("info", this.getAllData());
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag){
        super.handleUpdateTag(tag);
        this.handleData(tag.getCompoundTag("info"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        compound.setTag("info", this.getAllData());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        this.handleData(compound.getCompoundTag("info"));
    }

    protected NBTTagCompound getChangedData(){
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("camoState", Block.getStateId(this.camoState));
        return data;
    }

    protected NBTTagCompound getAllData(){
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("camoState", Block.getStateId(this.camoState));
        return data;
    }

    protected void handleData(NBTTagCompound data){
        if(data.hasKey("camoState"))
            this.camoState = Block.getStateById(data.getInteger("camoState"));
        else if(data.hasKey("hasCamo")){ // Do this for older versions
            if(data.getBoolean("hasCamo"))
                this.camoState = Block.getStateById(data.getInteger("camo"));
            else
                this.camoState = Blocks.AIR.getDefaultState();
        }else if(data.hasKey("camo")){ // Do this for older versions
            ItemStack camoStack = new ItemStack(data.getCompoundTag("camo"));
            Item item = camoStack.getItem();
            if(item instanceof ItemBlock){
                Block block = ((ItemBlock)item).getBlock();
                this.camoState = block.getDefaultState();
            }
        }
    }

    public boolean setCamoState(IBlockState state){
        this.camoState = state == null ? Blocks.AIR.getDefaultState() : state;
        this.dataChanged();
        return true;
    }

    public boolean canBeCamoStack(ItemStack stack){
        if(stack.isEmpty() || !(stack.getItem() instanceof ItemBlock))
            return false;
        Block block = ((ItemBlock)stack.getItem()).getBlock();
        return block != MovingElevators.elevator_block && block != MovingElevators.display_block && block != MovingElevators.button_block &&
            this.isFullCube(block.getDefaultState());
    }

    private boolean isFullCube(IBlockState state){
        for(EnumFacing side : EnumFacing.values())
            if(state.getBlockFaceShape(this.world, this.pos, side) != BlockFaceShape.SOLID)
                return false;
        return true;
    }

    public IBlockState getCamoBlock(){
        if(this.camoState.getBlock() == Blocks.AIR)
            return this.getBlockState();
        return this.camoState;
    }

    public abstract EnumFacing getFacing();

    protected IBlockState getBlockState(){
        return this.world.getBlockState(this.pos);
    }

    protected void dataChanged(){
        this.dataChanged = true;
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
        this.markDirty();
    }

}
