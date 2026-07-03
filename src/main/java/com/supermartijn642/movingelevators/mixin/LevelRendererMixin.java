package com.supermartijn642.movingelevators.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.LevelRenderState;
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
@Mixin(value = LevelRenderer.class, priority = 900)
public class LevelRendererMixin {

    @Unique
    private static final PoseStack POSE_STACK = new PoseStack();

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
        method = "extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/culling/Frustum;)V",
        at = @At("HEAD")
    )
    private void extractVisibleBlockEntities(Camera camera, float partialTicks, LevelRenderState levelRenderState, Frustum frustum, CallbackInfo ci){
        ElevatorGroupRenderer.extractRenderState();
    }

    @Inject(
        method = "submitBlockEntities",
        at = @At("HEAD")
    )
    private void submitBlockEntities(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeStorage submitNodeStorage, CallbackInfo ci){
        ElevatorGroupRenderer.submit(POSE_STACK, ClientUtils.getPartialTicks(), levelRenderState.cameraRenderState, submitNodeStorage);
    }
}
