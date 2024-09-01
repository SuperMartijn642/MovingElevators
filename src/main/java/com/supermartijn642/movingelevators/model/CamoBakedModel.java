package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.movingelevators.blocks.CamoBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class CamoBakedModel implements IBakedModel {

    private final IBakedModel originalModel;

    public CamoBakedModel(IBakedModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long random){
        IBlockState camouflage = state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(CamoBlock.CAMO_PROPERTY) : null;

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if(camouflage == null || camouflage.getBlock() == Blocks.AIR){
            if(state != null && layer != state.getBlock().getBlockLayer())
                return Collections.emptyList();
            return this.originalModel.getQuads(state, side, random);
        }

        if(!camouflage.getBlock().canRenderInLayer(camouflage, layer))
            return Collections.emptyList();
        IBakedModel model = ClientUtils.getBlockRenderer().getModelForState(camouflage);
        return model.getQuads(camouflage, side, random);
    }

    @Override
    public boolean isAmbientOcclusion(){
        return this.originalModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d(){
        return this.originalModel.isGui3d();
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

    @Override
    public Pair<? extends IBakedModel,Matrix4f> handlePerspective(ItemCameraTransforms.TransformType transformType){
        return this.originalModel.handlePerspective(transformType);
    }
}
