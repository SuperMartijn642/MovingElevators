package com.supermartijn642.movingelevators.model;

import com.supermartijn642.movingelevators.base.MEBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.List;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class MEBlockBakedModel implements IBakedModel {

    private final IBakedModel originalModel;

    public MEBlockBakedModel(IBakedModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand){
        MEBlockModelData extraData = state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(MEBlock.MODEL_DATA) : null;
        IBlockState camouflage = extraData == null ? null : extraData.camouflage;

        if(camouflage == null)
            return this.originalModel.getQuads(state, side, rand);

        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(camouflage);
        return model.getQuads(camouflage, side, rand);
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
    public boolean isBuiltInRenderer(){
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture(){
        return this.originalModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides(){
        return ItemOverrideList.NONE;
    }
}
