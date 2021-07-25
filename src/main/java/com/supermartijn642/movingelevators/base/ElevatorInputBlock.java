package com.supermartijn642.movingelevators.base;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.function.Supplier;

/**
 * Created 5/6/2020 by SuperMartijn642
 */
public class ElevatorInputBlock extends MEBlock {

    public ElevatorInputBlock(String registry_name, Supplier<? extends ElevatorInputTile> tileSupplier){
        super(registry_name, tileSupplier);
    }

    @Override
    protected void onRightClick(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(worldIn.isClientSide)
            return;

        TileEntity tile = worldIn.getBlockEntity(pos);
        if(!(tile instanceof ElevatorInputTile))
            return;

        ElevatorInputTile inputTile = (ElevatorInputTile)tile;
        if(inputTile.getFacing() != rayTraceResult.getDirection() || !inputTile.hasGroup())
            return;

        double y = rayTraceResult.getLocation().y - pos.getY();
        inputTile.getGroup().onButtonPress(y > 2 / 3D, y < 1 / 3D, inputTile.getFloorLevel());
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
        TileEntity tile = world.getBlockEntity(pos);
        if(tile instanceof ElevatorInputTile)
            ((ElevatorInputTile)tile).redstone = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
    }
}
