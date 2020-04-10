package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class MEBlock extends Block {

    private final Supplier<? extends METile> tileSupplier;

    public MEBlock(String registry_name, Supplier<? extends METile> tileSupplier){
        super(Block.Properties.create(Material.ROCK, MaterialColor.GRAY).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).hardnessAndResistance(1.5F, 6.0F));
        this.tileSupplier = tileSupplier;
        this.setRegistryName(registry_name);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof METile){
            METile meTile = (METile)tile;
            if(meTile.getFacing() == null || meTile.getFacing() != rayTraceResult.getFace()){
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
        this.onRightClick(state, worldIn, pos, player, handIn, rayTraceResult);
        return true;
    }

    protected void onRightClick(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){

    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return this.tileSupplier.get();
    }
}
