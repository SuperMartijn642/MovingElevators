package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;

/**
 * Created 4/6/2020 by SuperMartijn642
 */
public abstract class CamoBlockEntity extends BaseBlockEntity {

    private BlockState camoState = Blocks.AIR.defaultBlockState();

    public CamoBlockEntity(BaseBlockEntityType<?> blockEntityType, BlockPos pos, BlockState state){
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
        return !MovingElevators.CAMOUFLAGE_MOD_BLACKLIST.contains(Registries.BLOCKS.getIdentifier(block).getNamespace()) && this.isFullCube(block.defaultBlockState());
    }

    private boolean isFullCube(BlockState state){
        List<AABB> shapes = state.getCollisionShape(this.level, this.worldPosition).toAabbs();
        return shapes.size() == 1 && shapes.get(0).equals(new AABB(0, 0, 0, 1, 1, 1));
    }

    @Override
    public ModelData getModelData(){
        return ModelData.builder().with(CamoBakedModel.CAMO_PROPERTY, this.hasCamoState() ? this.camoState : null).build();
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag compound = new CompoundTag();
        compound.putInt("camoState", Block.getId(this.camoState));
        return compound;
    }

    @Override
    protected void readData(CompoundTag compound){
        this.camoState = Block.stateById(compound.getInt("camoState"));
    }
}
