package com.supermartijn642.movingelevators.mixin.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 22/04/2023 by SuperMartijn642
 */
@Mixin(value = LevelRenderer.class, priority = 1001)
public class LevelRendererMixinSodium {

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
        method = "renderChunkLayer",
        at = @At("HEAD")
    )
    public void renderChunkLayer(RenderType renderType, PoseStack poseStack, double cameraX, double cameraY, double cameraZ, Matrix4f matrix4f, CallbackInfo ci){
        if(!ElevatorGroupRenderer.isIrisRenderingShadows)
            ElevatorGroupRenderer.renderBlocks(poseStack, renderType, this.renderBuffers.bufferSource());
    }
}
