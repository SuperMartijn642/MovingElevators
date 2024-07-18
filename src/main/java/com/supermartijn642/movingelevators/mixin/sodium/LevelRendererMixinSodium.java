package com.supermartijn642.movingelevators.mixin.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
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
@Mixin(value = LevelRenderer.class, priority = 1001)
public class LevelRendererMixinSodium {

    @Unique
    private static final PoseStack POSE_STACK = new PoseStack();

    @Shadow
    @Final
    private RenderBuffers renderBuffers;
    @Unique
    private Boolean isIrisLoaded;

    @Inject(
        method = "renderLevel",
        at = @At("HEAD")
    )
    public void renderLevelHead(float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f modelView, Matrix4f projection, CallbackInfo ci){
        // Apply the model-view matrix to the matrix stack
        POSE_STACK.pushPose();
        if(this.isIrisLoaded == null)
            this.isIrisLoaded = CommonUtils.isModLoaded("iris");
        if(!this.isIrisLoaded)
            POSE_STACK.mulPose(modelView);
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderBuffers;bufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;",
            shift = At.Shift.AFTER
        )
    )
    public void afterModelViewMatrix(float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f modelView, Matrix4f projection, CallbackInfo ci){
        // At some point the model-view matrix gets updated, so we can undo applying it to the matrix stack
        POSE_STACK.popPose();
    }

    @Inject(
        method = "renderSectionLayer",
        at = @At("HEAD")
    )
    public void renderChunkLayer(RenderType renderType, double cameraX, double cameraY, double cameraZ, Matrix4f modelView, Matrix4f projection, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlocks(POSE_STACK, renderType, this.renderBuffers.bufferSource());
    }
}
