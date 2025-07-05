package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
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
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest){
        BlockState camoState = getCamoState(blockView, state, pos);
        BlockStateModel model = camoState == null ? this.originalModel : ClientUtils.getBlockRenderer().getBlockModel(camoState);
        model.emitQuads(emitter, blockView, pos, state, random, cullTest);
    }

    @Override
    public TextureAtlasSprite particleSprite(BlockAndTintGetter blockView, BlockPos pos, BlockState state){
        BlockState camoState = getCamoState(blockView, state, pos);
        BlockStateModel model = camoState == null ? this.originalModel : ClientUtils.getBlockRenderer().getBlockModel(camoState);
        return model.particleSprite(blockView, pos, state);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random){
        return Pair.of(this, getCamoState(blockView, state, pos));
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
    public List<BlockModelPart> collectParts(RandomSource random){
        return this.originalModel.collectParts(random);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts){
        this.originalModel.collectParts(random, parts);
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.originalModel.particleIcon();
    }
}
