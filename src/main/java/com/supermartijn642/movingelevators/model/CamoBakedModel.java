package com.supermartijn642.movingelevators.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class CamoBakedModel implements IDynamicBakedModel {

    public static final ModelProperty<BlockState> CAMO_PROPERTY = new ModelProperty<>();

    private final BakedModel originalModel;
    private List<BakedQuad> originalModelQuads;

    public CamoBakedModel(BakedModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random random, IModelData modelData){
        BlockState camouflage = modelData.getData(CAMO_PROPERTY);

        if(camouflage == null || camouflage.getBlock() == Blocks.AIR){
            if(this.originalModelQuads == null)
                this.originalModelQuads = getAllQuads(this.originalModel, state, random);
            return this.originalModelQuads;
        }

        BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(camouflage);
        return getAllQuads(model, camouflage, random);
    }

    private static List<BakedQuad> getAllQuads(BakedModel model, BlockState state, Random random){
        List<BakedQuad> quads = new ArrayList<>();
        for(Direction direction : Direction.values())
            quads.addAll(model.getQuads(state, direction, random, EmptyModelData.INSTANCE));
        quads.addAll(model.getQuads(state, null, random, EmptyModelData.INSTANCE));
        return quads;
    }

    @Override
    public IModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, IModelData entityData){
        BlockEntity entity = level.getBlockEntity(pos);
        return entity == null ? EmptyModelData.INSTANCE : entity.getModelData();
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

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType transformType, PoseStack poseStack){
        return this.originalModel.handlePerspective(transformType, poseStack);
    }
}
