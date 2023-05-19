package com.supermartijn642.movingelevators.model;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context){
        BlockState camoState = null;

        // This is stupid, but oh well ¯\(o_o)/¯
        if(state != null && pos != null && blockView.getBlockState(pos) == state){
            BlockEntity entity = blockView.getBlockEntity(pos);
            if(entity instanceof CamoBlockEntity && ((CamoBlockEntity)entity).hasCamoState())
                camoState = ((CamoBlockEntity)entity).getCamoState();
        }

        if(camoState == null)
            context.bakedModelConsumer().accept(this.originalModel, state);
        else{
            BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(camoState);
            if(((FabricBakedModel)model).isVanillaAdapter())
                context.bakedModelConsumer().accept(model, camoState);
            else
                ((FabricBakedModel)model).emitBlockQuads(blockView, camoState, pos, randomSupplier, context);
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context){
        context.fallbackConsumer().accept(this.originalModel);
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
    public ItemTransforms getTransforms(){
        return this.originalModel.getTransforms();
    }
}
