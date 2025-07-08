package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class CamoBakedModel implements DynamicBlockStateModel {

    public static final ModelProperty<BlockState> CAMO_PROPERTY = new ModelProperty<>();

    private final BlockStateModel originalModel;

    public CamoBakedModel(BlockStateModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts){
        BlockState camouflage = level.getModelData(pos).get(CAMO_PROPERTY);

        if(camouflage == null || camouflage.getBlock() == Blocks.AIR){
            this.originalModel.collectParts(level, pos, state, random, parts);
            return;
        }

        BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(camouflage);
        model.collectParts(level, pos, camouflage, random, parts);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        BlockState camouflage = level.getModelData(pos).get(CAMO_PROPERTY);

        if(camouflage == null || camouflage.getBlock() == Blocks.AIR)
            return this.originalModel.createGeometryKey(level, pos, state, random);

        BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(camouflage);
        return model.createGeometryKey(level, pos, camouflage, random);
    }

    @Override
    public TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state){
        BlockState camouflage = level.getModelData(pos).get(CAMO_PROPERTY);

        if(camouflage == null || camouflage.getBlock() == Blocks.AIR)
            return this.originalModel.particleIcon(level, pos, state);

        BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(camouflage);
        return model.particleIcon(level, pos, camouflage);
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.originalModel.particleIcon();
    }
}
