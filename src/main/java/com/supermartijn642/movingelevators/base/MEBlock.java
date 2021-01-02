package com.supermartijn642.movingelevators.base;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
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
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof METile){
            METile meTile = (METile)tile;
            if(meTile.getFacing() == null || meTile.getFacing() != rayTraceResult.getFace()){
                if(player.isSneaking() && player.getHeldItem(handIn).isEmpty()){
                    if(!worldIn.isRemote)
                        meTile.setCamoState(null);
                    return ActionResultType.SUCCESS;
                }else if(!player.isSneaking() && meTile.canBeCamoStack(player.getHeldItem(handIn))){
                    if(!worldIn.isRemote){
                        Item item = player.getHeldItem(handIn).getItem();
                        if(item instanceof BlockItem){
                            Block block = ((BlockItem)item).getBlock();
                            BlockState state1 = block.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, handIn, rayTraceResult)));
                            if(state1 == null)
                                state1 = block.getDefaultState();
                            meTile.setCamoState(state1);
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }
        this.onRightClick(state, worldIn, pos, player, handIn, rayTraceResult);
        return ActionResultType.SUCCESS;
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

    @Override
    public PushReaction getPushReaction(BlockState state){
        return PushReaction.BLOCK;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType){
        return false;
    }

    @Override
    public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context){
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos){
        return VoxelShapes.empty();
    }
}
