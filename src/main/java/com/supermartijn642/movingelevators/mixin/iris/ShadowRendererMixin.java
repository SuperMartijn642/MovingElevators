package com.supermartijn642.movingelevators.mixin.iris;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 30/08/2023 by SuperMartijn642
 */
@Mixin(value = ShadowRenderer.class, remap = false)
public class ShadowRendererMixin {

    @Shadow public static Matrix4f MODELVIEW;

    @Shadow @Final private RenderBuffers buffers;

    @Inject(
        method = "renderShadows",
        at = @At("HEAD")
    )
    private void renderShadowsHead(CallbackInfo ci){
        ElevatorGroupRenderer.isIrisRenderingShadows = true;
    }

    @Inject(method = "renderShadows", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/ShadowRenderingState;renderBlockEntities(Lnet/irisshaders/iris/shadows/ShadowRenderer;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;DDDFZZ)I"))
    private void renderBEShadow(LevelRendererAccessor levelRenderer, Camera playerCamera, CallbackInfo ci) {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.mulPoseMatrix(MODELVIEW);
        ElevatorGroupRenderer.renderBlockEntities(poseStack, CapturedRenderingState.INSTANCE.getTickDelta(), buffers.bufferSource());
        poseStack.popPose();
    }

    @Inject(
        method = "renderShadows",
        at = @At("TAIL")
    )
    private void renderShadowsTail(CallbackInfo ci){
        ElevatorGroupRenderer.isIrisRenderingShadows = false;
    }
}
