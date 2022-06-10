package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseTileEntity;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Created 4/6/2020 by SuperMartijn642
 */
public abstract class CamoBlockEntity extends BaseTileEntity {

    private BlockState camoState = Blocks.AIR.defaultBlockState();

    public CamoBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state){
        super(blockEntityType, pos, state);
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
        return !MovingElevators.CAMOUFLAGE_MOD_BLACKLIST.contains(ForgeRegistries.BLOCKS.getKey(block).getNamespace()) && this.isFullCube(block.defaultBlockState());
    }

    private boolean isFullCube(BlockState state){
        List<AABB> shapes = state.getCollisionShape(this.level, this.worldPosition).toAabbs();
        return shapes.size() == 1 && shapes.get(0).equals(new AABB(0, 0, 0, 1, 1, 1));
    }

    @Override
    public IModelData getModelData(){
        return new ModelDataMap.Builder().withInitial(CamoBakedModel.CAMO_PROPERTY, this.hasCamoState() ? this.camoState : null).build();
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag compound = new CompoundTag();
        compound.putInt("camoState", Block.getId(this.camoState));
        return compound;
    }

    @Override
    protected void readData(CompoundTag compound){
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
    public void load(CompoundTag nbt){
        if(nbt.contains("info", Tag.TAG_COMPOUND)){
            // Do this for older versions
            nbt.put("data", nbt.getCompound("info"));
            nbt.remove("info");
        }
        super.load(nbt);
    }
}
