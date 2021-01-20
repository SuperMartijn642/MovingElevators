package com.supermartijn642.movingelevators.base;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public abstract class METileRenderer<T extends METile> extends TileEntityRenderer<T> {

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

        this.render();
    }

    protected abstract void render();
}
