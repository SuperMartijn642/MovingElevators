package com.supermartijn642.movingelevators.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.supermartijn642.movingelevators.extensions.MovingElevatorsLevelChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 18/10/2024 by SuperMartijn642
 */
@Mixin(LevelChunk.class)
public class LevelChunkMixin implements MovingElevatorsLevelChunk {

    @Unique
    private boolean suppressBlockUpdates;

    @Override
    public boolean movingElevatorsSuppressBlockUpdates(boolean suppress){
        return this.suppressBlockUpdates = suppress;
    }

    @WrapWithCondition(
        method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
        )
    )
    public boolean setBlockState(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean fromPiston) {
        return !this.suppressBlockUpdates;
    }
}
