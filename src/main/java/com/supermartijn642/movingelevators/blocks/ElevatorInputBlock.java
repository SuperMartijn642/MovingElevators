package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created 5/6/2020 by SuperMartijn642
 */
public class ElevatorInputBlock extends CamoBlock {

    public ElevatorInputBlock(BlockProperties properties, Supplier<? extends ElevatorInputBlockEntity> entitySupplier){
        super(properties, entitySupplier);
    }

    @Override
    protected boolean onRightClick(BlockState state, World level, CamoBlockEntity blockEntity, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vec3d hitLocation){
        if(blockEntity instanceof ElevatorInputBlockEntity){
            ElevatorInputBlockEntity inputEntity = (ElevatorInputBlockEntity)blockEntity;
            if(inputEntity.getFacing() == hitSide && inputEntity.hasGroup()){
                if(!level.isClientSide){
                    double y = hitLocation.y - pos.getY();
                    inputEntity.getGroup().onButtonPress(y > 2 / 3D, y < 1 / 3D, inputEntity.getFloorLevel());
                }
                return true;
            }
        }
        return super.onRightClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation);
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof ElevatorInputBlockEntity)
            ((ElevatorInputBlockEntity)entity).redstone = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader level, BlockPos pos, @Nullable Direction side){
        return true;
    }
}
