package com.supermartijn642.movingelevators.mixin;

import com.supermartijn642.movingelevators.extensions.MovingElevatorsLevelChunk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created 18/10/2024 by SuperMartijn642
 */
@Mixin(Chunk.class)
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
            target = "Lnet/minecraft/block/BlockState;onRemove(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"
        )
    )
    public void suppressOnRemove(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean fromPiston){
        if(!this.suppressBlockUpdates)
            oldState.onRemove(world, pos, newState, fromPiston);
    }

    @Redirect(
        method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onPlace(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"
        )
    )
    public void suppressOnPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean fromPiston){
        if(!this.suppressBlockUpdates)
            state.onPlace(world, pos, oldState, fromPiston);
    }
}
