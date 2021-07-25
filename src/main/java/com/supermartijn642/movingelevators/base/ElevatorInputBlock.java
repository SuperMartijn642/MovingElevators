package com.supermartijn642.movingelevators.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.BiFunction;

/**
 * Created 5/6/2020 by SuperMartijn642
 */
public class ElevatorInputBlock extends MEBlock {

    public ElevatorInputBlock(String registry_name, BiFunction<BlockPos,BlockState,? extends ElevatorInputTile> tileSupplier){
        super(registry_name, tileSupplier);
    }

    @Override
    protected void onRightClick(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
        if(worldIn.isClientSide)
            return;

        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(!(tile instanceof ElevatorInputTile))
            return;

        ElevatorInputTile inputTile = (ElevatorInputTile)tile;
        if(inputTile.getFacing() != rayTraceResult.getDirection() || !inputTile.hasGroup())
            return;

        double y = rayTraceResult.getLocation().y - pos.getY();
        inputTile.getGroup().onButtonPress(y > 2 / 3D, y < 1 / 3D, inputTile.getFloorLevel());
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof ElevatorInputTile)
            ((ElevatorInputTile)tile).redstone = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
    }
}
