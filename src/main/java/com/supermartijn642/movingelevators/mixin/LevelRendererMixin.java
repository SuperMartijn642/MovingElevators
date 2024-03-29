package com.supermartijn642.movingelevators.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 22/04/2023 by SuperMartijn642
 */
@Mixin(WorldRenderer.class)
public class LevelRendererMixin {

    private static boolean render = false;

    @Shadow
    @Final
    private RenderTypeBuffers renderBuffers;

    @Inject(
        method = "renderLevel",
        at = @At("HEAD")
    )
    public void renderLevelHead(MatrixStack poseStack, float f, long l, boolean bl, ActiveRenderInfo camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci){
        render = true;
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;getRenderableBlockEntities()Ljava/util/List;",
            shift = At.Shift.AFTER
        )
    )
    public void renderLevelBlockEntities(MatrixStack poseStack, float partialTicks, long l, boolean bl, ActiveRenderInfo camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci){
        if(render){
            render = false;
            ElevatorGroupRenderer.renderBlockEntities(poseStack, partialTicks, this.renderBuffers.bufferSource());
        }
    }

    @Inject(
        method = "renderChunkLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/vertex/VertexBuffer;unbind()V",
            shift = At.Shift.AFTER
        )
    )
    public void renderChunkLayer(RenderType renderType, MatrixStack poseStack, double cameraX, double cameraY, double cameraZ, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlocks(poseStack, renderType, this.renderBuffers.bufferSource());
    }
}
