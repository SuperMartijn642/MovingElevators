package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

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
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts){
        BlockState camoState = level.getModelData(pos).get(CAMO_PROPERTY);

        if(camoState == null){
            this.originalModel.collectParts(level, pos, state, random, parts);
            return;
        }

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        model.collectParts(level, pos, camoState, random, parts);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        BlockState camoState = level.getModelData(pos).get(CAMO_PROPERTY);

        if(camoState == null)
            return this.originalModel.createGeometryKey(level, pos, state, random);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        return model.createGeometryKey(level, pos, camoState, random);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state){
        BlockState camoState = level.getModelData(pos).get(CAMO_PROPERTY);

        if(camoState == null)
            return this.originalModel.particleMaterial(level, pos, state);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        return model.particleMaterial(level, pos, camoState);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state){
        BlockState camoState = level.getModelData(pos).get(CAMO_PROPERTY);

        if(camoState == null)
            return this.originalModel.materialFlags(level, pos, state);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        return model.materialFlags(level, pos, camoState);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, @BakedQuad.MaterialFlags int flag){
        BlockState camoState = level.getModelData(pos).get(CAMO_PROPERTY);

        if(camoState == null)
            return this.originalModel.hasMaterialFlag(level, pos, state, flag);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        return model.hasMaterialFlag(level, pos, camoState, flag);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts){
        this.originalModel.collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial(){
        return this.originalModel.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(){
        return this.originalModel.materialFlags();
    }

    @Override
    public boolean hasMaterialFlag(@BakedQuad.MaterialFlags int flag){
        return this.originalModel.hasMaterialFlag(flag);
    }
}
