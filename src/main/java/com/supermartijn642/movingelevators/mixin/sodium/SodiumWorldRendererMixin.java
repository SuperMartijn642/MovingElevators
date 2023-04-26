package com.supermartijn642.movingelevators.mixin.sodium;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderTypeBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

/**
 * Created 26/04/2023 by SuperMartijn642
 */
@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererMixin {

    @Inject(
        method = "renderTileEntities",
        at = @At("HEAD"),
        remap = false
    )
    public void renderLevelBlockEntities(MatrixStack matrices, RenderTypeBuffers bufferBuilders, Long2ObjectMap<SortedSet<DestroyBlockProgress>> blockBreakingProgressions, ActiveRenderInfo camera, float tickDelta, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlockEntities(matrices, tickDelta, bufferBuilders.bufferSource());
    }
}
