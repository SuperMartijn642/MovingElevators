package com.supermartijn642.movingelevators.base;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
    protected void onRightClick(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote)
            return;

        TileEntity tile = worldIn.getTileEntity(pos);
        if(!(tile instanceof ElevatorInputTile))
            return;

        ElevatorInputTile inputTile = (ElevatorInputTile)tile;
        if(inputTile.getFacing() != facing || !inputTile.hasGroup())
            return;

        inputTile.getGroup().onButtonPress(hitY > 2 / 3D, hitY < 1 / 3D, inputTile.getFloorLevel());
    }
}
