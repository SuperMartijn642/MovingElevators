package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlock extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public ElevatorBlock(){
        super(Material.ROCK, MapColor.GRAY);
        this.setRegistryName("elevator_block");
        this.setUnlocalizedName(MovingElevators.MODID + ":elevator_block");
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
        this.setHardness(1.5f);
        this.setResistance(6.0f);
        this.setCreativeTab(CreativeTabs.SEARCH);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote && state.getValue(FACING) != facing){
            ClientProxy.openElevatorScreen(pos);
        }else if(!worldIn.isRemote && state.getValue(FACING) == facing){
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof ElevatorBlockTile){
                ElevatorBlockTile elevator = (ElevatorBlockTile)tile;
                if(elevator.getFacing() == facing){
                    ((ElevatorBlockTile)tile).onButtonPress(hitY > 2 / 3D, hitY < 1 / 3D, pos.getY());
                }
            }
        }
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return new ElevatorBlockTile();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof ElevatorBlockTile)
            ((ElevatorBlockTile)tile).onBreak(state.getValue(FACING));
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
}
