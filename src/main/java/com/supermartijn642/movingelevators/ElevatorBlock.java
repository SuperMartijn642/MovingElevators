package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlock extends MEBlock {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public ElevatorBlock(){
        super("elevator_block", ElevatorBlockTile::new);
    }

    @Override
    protected void onRightClick(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(!(tile instanceof ElevatorBlockTile))
            return;
        ElevatorBlockTile elevator = (ElevatorBlockTile)tile;
        if(!elevator.hasGroup())
            return;
        if(worldIn.isRemote && state.getValue(FACING) != facing)
            ClientProxy.openElevatorScreen(pos);
        else if(!worldIn.isRemote && state.getValue(FACING) == facing){
            ((ElevatorBlockTile)tile).getGroup().onButtonPress(hitY > 2 / 3D, hitY < 1 / 3D, pos.getY());
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof ElevatorBlockTile)
            ((ElevatorBlockTile)tile).onBreak();
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        EnumFacing facing = EnumFacing.getFront(meta);
        if(facing.getAxis() == EnumFacing.Axis.Y)
            facing = EnumFacing.NORTH;
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state){
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World worldIn, BlockPos pos){
        EnumFacing facing = state.getValue(FACING);
        if(facing == null)
            return 0;
        return worldIn.isAirBlock(pos.offset(facing).down()) ? 0 : 15;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos){
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof ElevatorBlockTile)
            ((ElevatorBlockTile)tile).redstone = world.isBlockPowered(pos) || world.isBlockPowered(pos.up());
    }
}
