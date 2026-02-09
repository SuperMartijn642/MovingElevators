package com.supermartijn642.movingelevators.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Created 09/02/2026 by SuperMartijn642
 */
public class BufferSourceWrapper implements MultiBufferSource {

    private MultiBufferSource source;

    public void set(MultiBufferSource source) {
        this.source = source;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType){
        return this.source.getBuffer(RenderLayerHelper.getEntityBlockLayer(renderType));
    }
}
