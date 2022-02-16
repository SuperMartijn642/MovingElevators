package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class CamoBlock extends BaseBlock {

    private final Supplier<? extends CamoBlockEntity> tileSupplier;

    public CamoBlock(String registryName, Properties properties, Supplier<? extends CamoBlockEntity> tileSupplier){
        super(registryName, false, properties.dynamicShape());
        this.tileSupplier = tileSupplier;
    }

    @Override
    public boolean use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        TileEntity blockEntity = worldIn.getBlockEntity(pos);
        if(blockEntity instanceof CamoBlockEntity)
            this.onRightClick(state, worldIn, (CamoBlockEntity)blockEntity, pos, player, handIn, rayTraceResult);

        // Always return success to prevent accidentally placing blocks
        return true;
    }

    /**
     * @return whether the interaction has been handled
     */
    protected boolean onRightClick(BlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(player.isSneaking() && player.getItemInHand(handIn).isEmpty()){
            blockEntity.setCamoState(null);
            return true;
        }else if(!player.isSneaking() && blockEntity.canBeCamoStack(player.getItemInHand(handIn))){
            Item item = player.getItemInHand(handIn).getItem();
            if(item instanceof BlockItem){
                Block block = ((BlockItem)item).getBlock();
                BlockState state1 = block.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, handIn, rayTraceResult)));
                if(state1 == null)
                    state1 = block.defaultBlockState();
                blockEntity.setCamoState(state1);
            }
            return true;
        }
        return false;
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
    public PushReaction getPistonPushReaction(BlockState state){
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType){
        return false;
    }

    @Override
    public int getLightValue(BlockState state, IEnviromentBlockReader reader, BlockPos pos){
        TileEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getLightValue(reader, pos) : super.getLightValue(state, reader, pos);
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader reader, BlockPos pos){
        TileEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getShadeBrightness(reader, pos) : super.getShadeBrightness(state, reader, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos){
        TileEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().propagatesSkylightDown(reader, pos) : super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader reader, BlockPos pos){
        return BlockShape.empty().getUnderlying();
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader reader, BlockPos pos){
        TileEntity blockEntity = reader instanceof World ? ((World)reader).getChunkAt(pos).getBlockEntity(pos) : reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getLightBlock(reader, pos) : reader.getMaxLightLevel();
    }

    @Override
    public BlockRenderLayer getRenderLayer(){
        return BlockRenderLayer.TRANSLUCENT;
    }
}
