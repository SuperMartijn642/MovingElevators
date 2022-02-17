package com.supermartijn642.movingelevators.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
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
    protected boolean onRightClick(IBlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(blockEntity instanceof ElevatorInputBlockEntity){
            ElevatorInputBlockEntity inputTile = (ElevatorInputBlockEntity)blockEntity;
            if(inputTile.getFacing() == facing && inputTile.hasGroup()){
                if(!worldIn.isRemote)
                    inputTile.getGroup().onButtonPress((double)hitY > 2 / 3D, (double)hitY < 1 / 3D, inputTile.getFloorLevel());
                return true;
            }
        }
        return super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, facing, hitX, hitY, hitZ);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos){
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof ElevatorInputBlockEntity)
            ((ElevatorInputBlockEntity)tile).redstone = world.isBlockPowered(pos) || world.isBlockPowered(pos.up());
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side){
        return true;
    }
}
