package com.supermartijn642.movingelevators.base;

import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
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
                        meTile.setCamoState(null);
                    return true;
                }else if(!player.isSneaking() && meTile.canBeCamoStack(player.getHeldItem(handIn))){
                    if(!worldIn.isRemote){
                        Item item = player.getHeldItem(handIn).getItem();
                        if(item instanceof ItemBlock){
                            Block block = ((ItemBlock)item).getBlock();
                            IBlockState state1 = block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, player.getHeldItem(handIn).getMetadata(), player, handIn);
                            meTile.setCamoState(state1);
                        }
                    }
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
    public EnumPushReaction getMobilityFlag(IBlockState state){
        return EnumPushReaction.BLOCK;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state){
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type){
        return false;
    }
}
