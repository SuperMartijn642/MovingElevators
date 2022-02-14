package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseTileEntity;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.Constants;

import java.util.List;

/**
 * Created 4/6/2020 by SuperMartijn642
 */
public abstract class CamoBlockEntity extends BaseTileEntity {

    private BlockState camoState = Blocks.AIR.defaultBlockState();

    public CamoBlockEntity(TileEntityType<?> blockEntityType){
        super(blockEntityType);
    }

    public boolean setCamoState(BlockState state){
        this.camoState = state == null ? Blocks.AIR.defaultBlockState() : state;
        this.dataChanged();
        this.level.getLightEngine().checkBlock(this.worldPosition);
        this.requestModelDataUpdate();
        return true;
    }

    public BlockState getCamoState(){
        return this.camoState;
    }

    public boolean hasCamoState(){
        return this.camoState != null && this.camoState.getBlock() != Blocks.AIR;
    }

    public boolean canBeCamoStack(ItemStack stack){
        if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem))
            return false;
        Block block = ((BlockItem)stack.getItem()).getBlock();
        return !MovingElevators.CAMOUFLAGE_MOD_BLACKLIST.contains(block.getRegistryName().getNamespace()) && this.isFullCube(block.defaultBlockState());
    }

    private boolean isFullCube(BlockState state){
        List<AxisAlignedBB> shapes = state.getCollisionShape(this.level, this.worldPosition).toAabbs();
        return shapes.size() == 1 && shapes.get(0).equals(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
    }

    @Override
    public IModelData getModelData(){
        return new ModelDataMap.Builder().withInitial(CamoBakedModel.CAMO_PROPERTY, this.hasCamoState() ? this.camoState : null).build();
    }

    @Override
    protected CompoundNBT writeData(){
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("camoState", Block.getId(this.camoState));
        return compound;
    }

    @Override
    protected void readData(CompoundNBT compound){
        if(compound.contains("camoState"))
            this.camoState = Block.stateById(compound.getInt("camoState"));
        else if(compound.contains("hasCamo")){ // Do this for older versions
            if(compound.getBoolean("hasCamo"))
                this.camoState = Block.stateById(compound.getInt("camo"));
            else
                this.camoState = Blocks.AIR.defaultBlockState();
        }else if(compound.contains("camo")){ // Do this for older versions
            ItemStack camoStack = ItemStack.of(compound.getCompound("camo"));
            Item item = camoStack.getItem();
            if(item instanceof BlockItem){
                Block block = ((BlockItem)item).getBlock();
                this.camoState = block.defaultBlockState();
            }
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt){
        if(nbt.contains("info", Constants.NBT.TAG_COMPOUND)){
            // Do this for older versions
            nbt.put("data", nbt.getCompound("info"));
            nbt.remove("info");
        }
        super.load(state, nbt);
    }
}
