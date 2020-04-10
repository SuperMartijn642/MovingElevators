package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
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
 * Created 4/7/2020 by SuperMartijn642
 */
public class MEBlock extends Block {

    private final Supplier<? extends METile> tileSupplier;

    public MEBlock(String registry_name, Supplier<? extends METile> tileSupplier){
        super(Material.ROCK, MapColor.GRAY);
        this.tileSupplier = tileSupplier;
        this.setRegistryName(registry_name);
        this.setUnlocalizedName(MovingElevators.MODID + ":" + registry_name);

        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
        this.setHardness(1.5f);
        this.setResistance(6.0f);
        this.setCreativeTab(CreativeTabs.SEARCH);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof METile){
            METile meTile = (METile)tile;
            if(meTile.getFacing() == null || meTile.getFacing() != facing){
                if(player.isSneaking() && player.getHeldItem(handIn).isEmpty()){
                    if(!worldIn.isRemote)
                        meTile.setCamoStack(null);
                    return true;
                }else if(!player.isSneaking() && meTile.canBeCamoStack(player.getHeldItem(handIn))){
                    if(!worldIn.isRemote)
                        meTile.setCamoStack(player.getHeldItem(handIn));
                    return true;
                }
            }
        }
        this.onRightClick(worldIn, pos, state, player, handIn, facing, hitX, hitY, hitZ);
        return true;
    }

    protected void onRightClick(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){

    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return this.tileSupplier.get();
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side){
        TileEntity tile = blockAccess.getTileEntity(pos);
        if(tile instanceof METile)
            return ((METile)tile).getCamoBlock() == null && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state){
        return EnumPushReaction.BLOCK;
    }
}
