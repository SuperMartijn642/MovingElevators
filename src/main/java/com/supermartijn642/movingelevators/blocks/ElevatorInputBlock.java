package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
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
    protected boolean onRightClick(IBlockState state, World level, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        if(blockEntity instanceof ElevatorInputBlockEntity){
            ElevatorInputBlockEntity inputEntity = (ElevatorInputBlockEntity)blockEntity;
            if(inputEntity.getFacing() == hitSide && inputEntity.hasGroup()){
                if(!level.isRemote){
                    double y = hitLocation.y - pos.getY();
                    inputEntity.getGroup().onButtonPress(y > 2 / 3D, y < 1 / 3D, inputEntity.getFloorLevel());
                }
                return true;
            }
        }
        return super.onRightClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation);
    }

    @Override
    public void neighborChanged(IBlockState state, World level, BlockPos pos, Block block, BlockPos fromPos){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof ElevatorInputBlockEntity)
            ((ElevatorInputBlockEntity)entity).redstone = level.isBlockPowered(pos) || level.isBlockPowered(pos.up());
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess level, BlockPos pos, @Nullable EnumFacing side){
        return true;
    }
}
