package com.supermartijn642.movingelevators.mixin.sodium;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 22/04/2023 by SuperMartijn642
 */
@Mixin(value = WorldRenderer.class, priority = 1001)
public class LevelRendererMixinSodium {

    @Shadow
    @Final
    private RenderTypeBuffers renderBuffers;

    @Inject(
        method = "renderChunkLayer",
        at = @At("HEAD")
    )
    public void renderChunkLayer(RenderType renderType, MatrixStack poseStack, double cameraX, double cameraY, double cameraZ, CallbackInfo ci){
        if(!ElevatorGroupRenderer.isIrisRenderingShadows)
            ElevatorGroupRenderer.renderBlocks(poseStack, renderType, this.renderBuffers.bufferSource());
    }
}
