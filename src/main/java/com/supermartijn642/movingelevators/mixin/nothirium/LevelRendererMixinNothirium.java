package com.supermartijn642.movingelevators.mixin.nothirium;

import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 22/04/2023 by SuperMartijn642
 */
@Mixin(value = RenderGlobal.class, priority = 999)
public class LevelRendererMixinNothirium {

    @Inject(
        method = "renderEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;preDrawBatch()V",
            shift = At.Shift.AFTER
        )
    )
    public void renderLevelBlockEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci){
        ElevatorGroupRenderer.renderBlockEntities(partialTicks);
    }

    @Inject(
        method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I",
        at = @At("HEAD")
    )
    public void renderChunkLayer(BlockRenderLayer renderType, double d, int i, Entity e, CallbackInfoReturnable<Integer> ci){
        ElevatorGroupRenderer.renderBlocks(renderType);
    }
}
