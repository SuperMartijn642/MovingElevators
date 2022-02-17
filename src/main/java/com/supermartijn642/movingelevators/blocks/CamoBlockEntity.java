package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseTileEntity;
import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

/**
 * Created 4/6/2020 by SuperMartijn642
 */
public abstract class CamoBlockEntity extends BaseTileEntity {

    private IBlockState camoState = Blocks.AIR.getDefaultState();

    public CamoBlockEntity(){
        super();
    }

    public boolean setCamoState(IBlockState state){
        this.camoState = state == null ? Blocks.AIR.getDefaultState() : state;
        this.dataChanged();
        this.world.checkLight(this.pos);
        return true;
    }

    public IBlockState getCamoState(){
        return this.camoState;
    }

    public boolean hasCamoState(){
        return this.camoState != null && this.camoState.getBlock() != Blocks.AIR && !(this.camoState.getBlock() instanceof CamoBlock);
    }

    public boolean canBeCamoStack(ItemStack stack){
        if(stack.isEmpty() || !(stack.getItem() instanceof ItemBlock))
            return false;
        Block block = ((ItemBlock)stack.getItem()).getBlock();
        return !MovingElevators.CAMOUFLAGE_MOD_BLACKLIST.contains(block.getRegistryName().getResourceDomain()) && this.isFullCube(block.getDefaultState());
    }

    private boolean isFullCube(IBlockState state){
        for(EnumFacing side : EnumFacing.values())
            if(state.getBlockFaceShape(this.world, this.pos, side) != BlockFaceShape.SOLID)
                return false;
        return true;
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("camoState", Block.getStateId(this.camoState));
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        if(compound.hasKey("camoState"))
            this.camoState = Block.getStateById(compound.getInteger("camoState"));
        else if(compound.hasKey("hasCamo")){ // Do this for older versions
            if(compound.getBoolean("hasCamo"))
                this.camoState = Block.getStateById(compound.getInteger("camo"));
            else
                this.camoState = Blocks.AIR.getDefaultState();
        }else if(compound.hasKey("camo")){ // Do this for older versions
            ItemStack camoStack = new ItemStack(compound.getCompoundTag("camo"));
            Item item = camoStack.getItem();
            if(item instanceof ItemBlock){
                Block block = ((ItemBlock)item).getBlock();
                this.camoState = block.getDefaultState();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt){
        if(nbt.hasKey("info", Constants.NBT.TAG_COMPOUND)){
            // Do this for older versions
            nbt.setTag("data", nbt.getCompoundTag("info"));
            nbt.removeTag("info");
        }
        super.readFromNBT(nbt);
    }
}
