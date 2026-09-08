package com.supermartijn642.movingelevators.mixin;

import com.supermartijn642.movingelevators.blocks.ElevatorInputBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 08/03/2023 by SuperMartijn642
 */
@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {

    @Inject(
        method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void shouldConnectTo(BlockState blockState, BlockGetter level, BlockPos pos, @Nullable Direction direction, CallbackInfoReturnable<Boolean> ci){
        if(blockState.getBlock() instanceof ElevatorInputBlock)
            ci.setReturnValue(true);
    }
}
