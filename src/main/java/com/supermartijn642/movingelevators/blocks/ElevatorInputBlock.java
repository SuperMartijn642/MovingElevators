package com.supermartijn642.movingelevators.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created 5/6/2020 by SuperMartijn642
 */
public class ElevatorInputBlock extends CamoBlock {

    public ElevatorInputBlock(String registry_name, Properties properties, Supplier<? extends ElevatorInputBlockEntity> tileSupplier){
        super(registry_name, properties, tileSupplier);
    }

    @Override
    protected boolean onRightClick(BlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(blockEntity instanceof ElevatorInputBlockEntity){
            ElevatorInputBlockEntity inputTile = (ElevatorInputBlockEntity)blockEntity;
            if(inputTile.getFacing() == rayTraceResult.getDirection() && inputTile.hasGroup()){
                if(!worldIn.isClientSide){
                    double y = rayTraceResult.getLocation().y - pos.getY();
                    inputTile.getGroup().onButtonPress(y > 2 / 3D, y < 1 / 3D, inputTile.getFloorLevel());
                }
                return true;
            }
        }
        return super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, rayTraceResult);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
        TileEntity tile = world.getBlockEntity(pos);
        if(tile instanceof ElevatorInputBlockEntity)
            ((ElevatorInputBlockEntity)tile).redstone = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side){
        return true;
    }
}
