package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class CamoBakedModel implements BlockStateModel {

    public static final ModelProperty<BlockState> CAMO_PROPERTY = new ModelProperty<>();

    private final BlockStateModel originalModel;

    public CamoBakedModel(BlockStateModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts, ModelData modelData, @Nullable ChunkSectionLayer renderType){
        BlockState camouflage = modelData.get(CAMO_PROPERTY);

        if(camouflage == null || camouflage.getBlock() == Blocks.AIR){
            this.originalModel.collectParts(random, parts, modelData, renderType);
            return;
        }

        BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(camouflage);
        model.collectParts(random, parts, modelData, renderType);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts){
        this.originalModel.collectParts(random, parts);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData entityData){
        BlockEntity entity = level.getBlockEntity(pos);
        return entity == null ? ModelData.EMPTY : entity.getModelData();
    }

    @Override
    public Collection<ChunkSectionLayer> getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data){
        BlockState camouflage = data.get(CAMO_PROPERTY);
        if(camouflage == null || camouflage.getBlock() == Blocks.AIR)
            return this.originalModel.getRenderTypes(state, rand, data);
        return ClientUtils.getBlockRenderer().getBlockModel(camouflage).getRenderTypes(camouflage, rand, ModelData.EMPTY);
    }

    @Override
    public TextureAtlasSprite particleIcon(@NotNull ModelData data){
        BlockState camouflage = data.get(CAMO_PROPERTY);
        if(camouflage == null || camouflage.getBlock() == Blocks.AIR)
            return this.originalModel.particleIcon(data);
        return ClientUtils.getBlockRenderer().getBlockModel(camouflage).particleIcon(data);
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.originalModel.particleIcon();
    }
}
