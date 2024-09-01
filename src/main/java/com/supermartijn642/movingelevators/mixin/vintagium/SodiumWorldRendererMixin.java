package com.supermartijn642.movingelevators.mixin.vintagium;

import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.renderer.DestroyBlockProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Created 01/09/2024 by SuperMartijn642
 */
@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer")
public class SodiumWorldRendererMixin {

    @Inject(
        method = "renderTileEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;preDrawBatch()V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    public void renderTileEntities(float partialTicks, Map<Integer,DestroyBlockProgress> damagedBlocks, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlockEntities(partialTicks);
    }
}
