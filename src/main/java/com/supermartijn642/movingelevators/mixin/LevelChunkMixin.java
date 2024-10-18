package com.supermartijn642.movingelevators.mixin;

import com.supermartijn642.movingelevators.extensions.MovingElevatorsLevelChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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

    @Redirect(
        method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;onRemove(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
        )
    )
    public void suppressOnRemove(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean fromPiston){
        if(!this.suppressBlockUpdates)
            state.onRemove(world, pos, oldState, fromPiston);
    }

    @Redirect(
        method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
        )
    )
    public void suppressOnPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean fromPiston){
        if(!this.suppressBlockUpdates)
            state.onPlace(world, pos, oldState, fromPiston);
    }
}
