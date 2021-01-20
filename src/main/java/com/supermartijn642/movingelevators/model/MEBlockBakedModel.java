package com.supermartijn642.movingelevators.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class MEBlockBakedModel implements IDynamicBakedModel {

    private final IBakedModel originalModel;

    public MEBlockBakedModel(IBakedModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData){
        BlockState camouflage = extraData instanceof MEBlockModelData ? ((MEBlockModelData)extraData).camouflage : null;

        if(camouflage == null)
            return this.originalModel.getQuads(state, side, rand, extraData);

        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(camouflage);
        return model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Override
    public IModelData getModelData(IBlockDisplayReader world, BlockPos pos, BlockState state, IModelData tileData){
        TileEntity tile = world.getTileEntity(pos);
        return tile == null ? EmptyModelData.INSTANCE : tile.getModelData();
    }

    @Override
    public boolean isAmbientOcclusion(){
        return false;
    }

    @Override
    public boolean isGui3d(){
        return false;
    }

    @Override
    public boolean func_230044_c_(){
        return this.originalModel.func_230044_c_();
    }

    @Override
    public boolean isBuiltInRenderer(){
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture(){
        return this.originalModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides(){
        return ItemOverrideList.EMPTY;
    }
}
