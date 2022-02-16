package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
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

    private final IBakedModel originalModel;
    private List<BakedQuad> originalModelQuads;

    public CamoBakedModel(IBakedModel originalModel){
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

        IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(camouflage);
        return getAllQuads(model, camouflage, random);
    }

    private static List<BakedQuad> getAllQuads(IBakedModel model, BlockState state, Random random){
        List<BakedQuad> quads = new ArrayList<>();
        for(Direction direction : Direction.values())
            quads.addAll(model.getQuads(state, direction, random, EmptyModelData.INSTANCE));
        quads.addAll(model.getQuads(state, null, random, EmptyModelData.INSTANCE));
        return quads;
    }

    @Override
    public IModelData getModelData(IEnviromentBlockReader world, BlockPos pos, BlockState state, IModelData tileData){
        TileEntity tile = world.getBlockEntity(pos);
        return tile == null ? EmptyModelData.INSTANCE : tile.getModelData();
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
    public boolean isCustomRenderer(){
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(){
        return this.originalModel.getParticleIcon();
    }

    @Override
    public ItemOverrideList getOverrides(){
        return ItemOverrideList.EMPTY;
    }
}
