package com.supermartijn642.movingelevators.mixin;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.movingelevators.extensions.MovingElevatorsLevelChunk;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;breakBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V"
        )
    )
    public void suppressOnRemove(Block oldBlock, World world, BlockPos pos, IBlockState newState){
        CommonUtils.getLogger("movingelevators").error("TRIGGERED!!!!");
        if(!this.suppressBlockUpdates)
            oldBlock.breakBlock(world, pos, newState);
    }

    @Redirect(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;onBlockAdded(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V"
        )
    )
    public void suppressOnPlace(Block block, World world, BlockPos pos, IBlockState oldState){
        if(!this.suppressBlockUpdates)
            block.onBlockAdded(world, pos, oldState);
    }
}
