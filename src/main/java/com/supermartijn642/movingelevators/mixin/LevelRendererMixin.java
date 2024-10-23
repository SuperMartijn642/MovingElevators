package com.supermartijn642.movingelevators.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 22/04/2023 by SuperMartijn642
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Unique
    private static final PoseStack POSE_STACK = new PoseStack();

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
        method = "method_62214(Lnet/minecraft/client/renderer/FogParameters;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;Lnet/minecraft/util/profiling/ProfilerFiller;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;ZLnet/minecraft/client/renderer/culling/Frustum;Lcom/mojang/blaze3d/resource/ResourceHandle;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/Camera;F)V",
            shift = At.Shift.BEFORE
        )
    )
    private void renderLevelBlockEntities(CallbackInfo ci){
        ElevatorGroupRenderer.renderBlockEntities(POSE_STACK, ClientUtils.getPartialTicks(), this.renderBuffers.bufferSource());
    }

    @Inject(
        method = "renderSectionLayer",
        at = @At("HEAD")
    )
    private void renderChunkLayer(RenderType renderType, double cameraX, double cameraY, double cameraZ, Matrix4f modelView, Matrix4f projection, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlocks(POSE_STACK, renderType, this.renderBuffers.bufferSource());
    }
}
