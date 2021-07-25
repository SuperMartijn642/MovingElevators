package com.supermartijn642.movingelevators.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class MEBlockBakedModel implements IDynamicBakedModel {

    private final BakedModel originalModel;

    public MEBlockBakedModel(BakedModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData){
        BlockState camouflage = extraData instanceof MEBlockModelData ? ((MEBlockModelData)extraData).camouflage : null;

        if(camouflage == null)
            return this.originalModel.getQuads(state, side, rand, extraData);

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(camouflage);
        return model.getQuads(camouflage, side, rand, EmptyModelData.INSTANCE);
    }

    @Override
    public IModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, IModelData tileData){
        BlockEntity tile = world.getBlockEntity(pos);
        return tile == null ? EmptyModelData.INSTANCE : tile.getModelData();
    }

    @Override
    public boolean useAmbientOcclusion(){
        return false;
    }

    @Override
    public boolean isGui3d(){
        return false;
    }

    @Override
    public boolean usesBlockLight(){
        return true;
    }

    @Override
    public boolean isCustomRenderer(){
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(){
        return this.originalModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides(){
        return ItemOverrides.EMPTY;
    }
}
