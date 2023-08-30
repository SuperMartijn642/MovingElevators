package com.supermartijn642.movingelevators.mixin.iris;

import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.coderbot.iris.pipeline.ShadowRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 30/08/2023 by SuperMartijn642
 */
@Mixin(value = ShadowRenderer.class, remap = false)
public class ShadowRendererMixin {

    @Inject(
        method = "renderShadows",
        at = @At("HEAD")
    )
    private void renderShadowsHead(CallbackInfo ci){
        ElevatorGroupRenderer.isIrisRenderingShadows = true;
    }

    @Inject(
        method = "renderShadows",
        at = @At("TAIL")
    )
    private void renderShadowsTail(CallbackInfo ci){
        ElevatorGroupRenderer.isIrisRenderingShadows = false;
    }
}
