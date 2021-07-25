package com.supermartijn642.movingelevators.base;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public abstract class METileRenderer<T extends METile> implements BlockEntityRenderer<T> {

    protected T tile;
    protected float partialTicks;
    protected PoseStack matrixStack;
    protected MultiBufferSource buffer;
    protected int combinedLight;
    protected int combinedOverlay;

    @Override
    public void render(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
        if(tile == null || tile.getLevel() == null)
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
