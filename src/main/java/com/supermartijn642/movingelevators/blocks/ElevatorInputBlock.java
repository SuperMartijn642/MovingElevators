package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Created 5/6/2020 by SuperMartijn642
 */
public class ElevatorInputBlock extends CamoBlock {

    public ElevatorInputBlock(BlockProperties properties, BiFunction<BlockPos,BlockState,? extends ElevatorInputBlockEntity> entitySupplier){
        super(properties, entitySupplier);
    }

    @Override
    protected boolean onRightClick(BlockState state, Level level, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof ElevatorInputBlockEntity)
            ((ElevatorInputBlockEntity)entity).redstone = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction side){
        return true;
    }
}
