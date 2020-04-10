package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
public class METile extends TileEntity {

    public METile(){
        super();
    }

    private ItemStack camoStack = ItemStack.EMPTY;

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        return new SPacketUpdateTileEntity(this.pos, 0, this.getDataTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.handleDataTag(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("info", this.getDataTag());
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag){
        super.handleUpdateTag(tag);
        this.handleDataTag(tag.getCompoundTag("info"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        compound.setTag("info", this.getDataTag());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        this.handleDataTag(compound.getCompoundTag("info"));
    }

    protected NBTTagCompound getDataTag(){
        NBTTagCompound data = new NBTTagCompound();
        data.setTag("camo", this.camoStack.writeToNBT(new NBTTagCompound()));
        return data;
    }

    protected void handleDataTag(NBTTagCompound tag){
        if(tag.hasKey("camo"))
            this.camoStack = new ItemStack(tag.getCompoundTag("camo"));
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
        if(stack.isEmpty() || !(stack.getItem() instanceof ItemBlock))
            return false;
        Block block = ((ItemBlock)stack.getItem()).getBlock();
        return block != MovingElevators.elevator_block && block != MovingElevators.display_block && block.isFullCube(block.getDefaultState());
    }

    public IBlockState getCamoBlock(){
        if(this.camoStack == null || this.camoStack.isEmpty() || !(this.camoStack.getItem() instanceof ItemBlock))
            return null;
        return ((ItemBlock)this.camoStack.getItem()).getBlock().getStateForPlacement(this.world, this.pos, null, 0.5f, 0.5f, 0.5f, this.camoStack.getMetadata(), null);
    }

    public EnumFacing getFacing(){
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

    protected IBlockState getBlockState(){
        return this.world.getBlockState(this.pos);
    }

    @Override
    public boolean shouldRenderInPass(int pass){
        return super.shouldRenderInPass(pass);
    }
}
