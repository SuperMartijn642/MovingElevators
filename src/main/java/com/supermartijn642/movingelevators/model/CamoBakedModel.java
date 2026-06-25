package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class CamoBakedModel implements BlockStateModel {

    private final BlockStateModel originalModel;

    public CamoBakedModel(BlockStateModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest){
        BlockState camoState = getCamoState(level, state, pos);

        if(camoState == null){
            this.originalModel.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        model.emitQuads(emitter, level, pos, camoState, random, cullTest);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        BlockState camoState = getCamoState(level, state, pos);

        if(camoState == null)
            return this.originalModel.createGeometryKey(level, pos, state, random);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        return model.createGeometryKey(level, pos, camoState, random);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state){
        BlockState camoState = getCamoState(level, state, pos);

        if(camoState == null)
            return this.originalModel.particleMaterial(level, pos, state);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        return model.particleMaterial(level, pos, camoState);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        BlockState camoState = getCamoState(level, state, pos);

        if(camoState == null)
            return this.originalModel.materialFlags(level, pos, state, random);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        return model.materialFlags(level, pos, camoState, random);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag){
        BlockState camoState = getCamoState(level, state, pos);

        if(camoState == null)
            return this.originalModel.hasMaterialFlag(level, pos, state, random, flag);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(camoState);
        return model.hasMaterialFlag(level, pos, camoState, random, flag);
    }

    private static BlockState getCamoState(BlockAndTintGetter blockView, BlockState state, BlockPos pos){
        if(state != null && pos != null && blockView.getBlockState(pos) == state){
            BlockEntity entity = blockView.getBlockEntity(pos);
            if(entity instanceof CamoBlockEntity && ((CamoBlockEntity)entity).hasCamoState())
                return ((CamoBlockEntity)entity).getCamoState();
        }
        return null;
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
