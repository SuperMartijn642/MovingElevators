package com.supermartijn642.movingelevators.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.Camera;
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
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunks:Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            shift = At.Shift.BEFORE
        )
    )
    public void renderLevelBlockEntities(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlockEntities(poseStack, partialTicks, this.renderBuffers.bufferSource());
    }

    @Inject(
        method = "renderChunkLayer",
        at = @At("HEAD")
    )
    public void renderChunkLayer(RenderType renderType, PoseStack poseStack, double cameraX, double cameraY, double cameraZ, Matrix4f matrix4f, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlocks(poseStack, renderType, this.renderBuffers.bufferSource());
    }
}
