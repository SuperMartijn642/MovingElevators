package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class CamoBakedModel implements BakedModel, FabricBakedModel {

    private final BakedModel originalModel;

    public CamoBakedModel(BakedModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public boolean isVanillaAdapter(){
        return false;
    }

    @Override
    public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest){
        BlockState camoState = null;

        // This is stupid, but oh well ¯\(o_o)/¯
        if(state != null && pos != null && blockView.getBlockState(pos) == state){
            BlockEntity entity = blockView.getBlockEntity(pos);
            if(entity instanceof CamoBlockEntity && ((CamoBlockEntity)entity).hasCamoState())
                camoState = ((CamoBlockEntity)entity).getCamoState();
        }

        if(camoState == null)
            this.originalModel.emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
        else{
            BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(camoState);
            model.emitBlockQuads(emitter, blockView, camoState, pos, randomSupplier, cullTest);
        }
    }

    @Override
    public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier){
        this.originalModel.emitItemQuads(emitter, randomSupplier);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource random){
        return this.originalModel.getQuads(blockState, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion(){
        return this.originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d(){
        return this.originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight(){
        return this.originalModel.usesBlockLight();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(){
        return this.originalModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms(){
        return this.originalModel.getTransforms();
    }
}
