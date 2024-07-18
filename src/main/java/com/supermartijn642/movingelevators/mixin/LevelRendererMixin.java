package com.supermartijn642.movingelevators.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
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
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Unique
    private static final PoseStack POSE_STACK = new PoseStack();
    @Unique
    private static boolean render = false;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
        method = "renderLevel",
        at = @At("HEAD")
    )
    public void renderLevelHead(float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci){
        render = true;
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$CompiledSection;getRenderableBlockEntities()Ljava/util/List;",
            shift = At.Shift.AFTER
        )
    )
    public void renderLevelBlockEntities(float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci){
        if(render){
            render = false;
            ElevatorGroupRenderer.renderBlockEntities(POSE_STACK, partialTicks, this.renderBuffers.bufferSource());
            if(!POSE_STACK.clear())
                throw new IllegalStateException("Pose stack not empty");
        }
    }

    @Inject(
        method = "renderSectionLayer",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;unbind()V",
            shift = At.Shift.BEFORE
        )
    )
    public void renderChunkLayer(RenderType renderType, double cameraX, double cameraY, double cameraZ, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci){
        POSE_STACK.pushPose();
        POSE_STACK.mulPose(matrix4f);
        if(!ElevatorGroupRenderer.isIrisRenderingShadows)
            ElevatorGroupRenderer.renderBlocks(POSE_STACK, renderType, this.renderBuffers.bufferSource());
        POSE_STACK.popPose();
    }
}
