package com.supermartijn642.movingelevators.base;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.model.MEBlockModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 4/6/2020 by SuperMartijn642
 */
public abstract class METile extends BlockEntity {

    public METile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state){
        super(tileEntityTypeIn, pos, state);
    }

    private BlockState camoState = Blocks.AIR.defaultBlockState();

    private boolean dataChanged = false;

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        if(!this.dataChanged)
            return null;
        this.dataChanged = false;
        CompoundTag compound = this.getChangedData();
        return compound == null || compound.isEmpty() ? null : ClientboundBlockEntityDataPacket.create(this, entity -> compound);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
        this.handleData(pkt.getTag());
    }

    @Override
    public CompoundTag getUpdateTag(){
        CompoundTag tag = super.getUpdateTag();
        tag.put("info", this.getAllData());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag){
        super.handleUpdateTag(tag);
        this.handleData(tag.getCompound("info"));
    }

    @Override
    public void saveAdditional(CompoundTag compound){
        super.saveAdditional(compound);
        compound.put("info", this.getAllData());
    }

    @Override
    public void load(CompoundTag compound){
        super.load(compound);
        this.handleData(compound.getCompound("info"));
    }

    protected CompoundTag getChangedData(){
        CompoundTag data = new CompoundTag();
        data.putInt("camoState", Block.getId(this.camoState));
        return data;
    }

    protected CompoundTag getAllData(){
        CompoundTag data = new CompoundTag();
        data.putInt("camoState", Block.getId(this.camoState));
        return data;
    }

    protected void handleData(CompoundTag data){
        if(data.contains("camoState"))
            this.camoState = Block.stateById(data.getInt("camoState"));
        else if(data.contains("hasCamo")){ // Do this for older versions
            if(data.getBoolean("hasCamo"))
                this.camoState = Block.stateById(data.getInt("camo"));
            else
                this.camoState = Blocks.AIR.defaultBlockState();
        }else if(data.contains("camo")){ // Do this for older versions
            ItemStack camoStack = ItemStack.of(data.getCompound("camo"));
            Item item = camoStack.getItem();
            if(item instanceof BlockItem){
                Block block = ((BlockItem)item).getBlock();
                this.camoState = block.defaultBlockState();
            }
        }
    }

    public boolean setCamoState(BlockState state){
        this.camoState = state == null ? Blocks.AIR.defaultBlockState() : state;
        this.dataChanged();
        return true;
    }

    public boolean canBeCamoStack(ItemStack stack){
        if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem))
            return false;
        Block block = ((BlockItem)stack.getItem()).getBlock();
        return !MovingElevators.CAMOUFLAGE_MOD_BLACKLIST.contains(block.getRegistryName().getNamespace()) && this.isFullCube(block.defaultBlockState());
    }

    private boolean isFullCube(BlockState state){
        List<AABB> shapes = state.getCollisionShape(this.level, this.worldPosition).toAabbs();
        return shapes.size() == 1 && shapes.get(0).equals(new AABB(0, 0, 0, 1, 1, 1));
    }

    public BlockState getCamoBlock(){
        if(this.camoState.getBlock() == Blocks.AIR)
            return this.getBlockState();
        return this.camoState;
    }

    public abstract Direction getFacing();

    protected void dataChanged(){
        this.dataChanged = true;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        this.setChanged();
    }

    @Override
    public IModelData getModelData(){
        return new MEBlockModelData(this.camoState == null || this.camoState.isAir() ? null : this.camoState);
    }
}
