package com.supermartijn642.movingelevators.mixin.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.level.BlockDestructionProgress;
import org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

/**
 * Created 26/04/2023 by SuperMartijn642
 */
@Mixin(EmbeddiumWorldRenderer.class)
public class SodiumWorldRendererMixin {

    @Inject(
        method = "renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;Lnet/minecraft/client/Camera;F)V",
        at = @At("HEAD")
    )
    public void renderLevelBlockEntities(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, Camera camera, float tickDelta, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlockEntities(matrices, tickDelta, bufferBuilders.bufferSource());
    }
}
