package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.Random;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class METileRenderer<T extends METile> extends TileEntityRenderer<T> {

    protected T tile;
    protected float partialTicks;
    protected MatrixStack matrixStack;
    protected IRenderTypeBuffer buffer;
    protected int combinedLight;
    protected int combinedOverlay;

    public METileRenderer(TileEntityRendererDispatcher rendererDispatcherIn){
        super(rendererDispatcherIn);
    }

    @Override
    public void render(T tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        if(tile == null || tile.getWorld() == null)
            return;

        this.tile = tile;
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;
        this.buffer = buffer;
        this.combinedLight = combinedLight;
        this.combinedOverlay = combinedOverlay;

        // render camouflage
        this.renderCamouflage();

        this.render();
    }

    protected void render(){
    }

    private void renderCamouflage(){
        if(tile.getCamoBlock() == null)
            return;
        matrixStack.push();

        matrixStack.translate(-0.001, -0.001, -0.001);
        matrixStack.scale(1.002f, 1.002f, 1.002f);

        BlockState state = tile.getCamoBlock();
        IModelData data = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state).getModelData(tile.getWorld(), tile.getPos(), state, EmptyModelData.INSTANCE);
        for(RenderType type : RenderType.getBlockRenderTypes()){
            if(RenderTypeLookup.canRenderInLayer(state, type))
                Minecraft.getInstance().getBlockRendererDispatcher().renderModel(state, tile.getPos(), tile.getWorld(), matrixStack, buffer.getBuffer(type), true, new Random(), data);
        }

        matrixStack.pop();
    }
}
