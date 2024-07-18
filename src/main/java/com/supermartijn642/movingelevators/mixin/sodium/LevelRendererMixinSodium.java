package com.supermartijn642.movingelevators.mixin.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.CommonUtils;
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
        method = "renderSectionLayer",
        at = @At("HEAD")
    )
    public void renderChunkLayer(RenderType renderType, double cameraX, double cameraY, double cameraZ, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci){
        if(this.isIrisLoaded == null)
            this.isIrisLoaded = CommonUtils.isModLoaded("iris");
        POSE_STACK.pushPose();
        if(!this.isIrisLoaded)
            POSE_STACK.mulPose(matrix4f);
        if(!ElevatorGroupRenderer.isIrisRenderingShadows)
            ElevatorGroupRenderer.renderBlocks(POSE_STACK, renderType, this.renderBuffers.bufferSource());
        POSE_STACK.popPose();
    }
}
