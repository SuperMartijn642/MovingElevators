package com.supermartijn642.movingelevators.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Created 5/6/2020 by SuperMartijn642
 */
public class ElevatorInputBlock extends CamoBlock {

    public ElevatorInputBlock(String registry_name, Properties properties, BiFunction<BlockPos,BlockState,? extends ElevatorInputBlockEntity> tileSupplier){
        super(registry_name, properties, tileSupplier);
    }

    @Override
    protected boolean onRightClick(BlockState state, Level worldIn, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType){
        return (world2, pos, state2, entity) -> {
            if(entity instanceof ElevatorInputBlockEntity)
                ((ElevatorInputBlockEntity)entity).tick();
        };
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof ElevatorInputBlockEntity)
            ((ElevatorInputBlockEntity)tile).redstone = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side){
        return true;
    }
}
