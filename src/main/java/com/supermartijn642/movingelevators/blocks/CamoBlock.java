package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.block.EntityHoldingBlock;
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
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class CamoBlock extends BaseBlock implements EntityHoldingBlock {

    private final Supplier<? extends CamoBlockEntity> entitySupplier;

    public CamoBlock(BlockProperties properties, Supplier<? extends CamoBlockEntity> entitySupplier){
        super(false, properties.dynamicShape());
        this.entitySupplier = entitySupplier;
    }

    @Override
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vec3d hitLocation){
        TileEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof CamoBlockEntity)
            this.onRightClick(state, level, (CamoBlockEntity)blockEntity, pos, player, hand, hitSide, hitLocation);

        // Always return success to prevent accidentally placing blocks
        return InteractionFeedback.SUCCESS;
    }

    /**
     * @return whether the interaction has been handled
     */
    protected boolean onRightClick(BlockState state, World level, CamoBlockEntity blockEntity, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vec3d hitLocation){
        if(player.isSneaking() && player.getItemInHand(hand).isEmpty()){
            blockEntity.setCamoState(null);
            return true;
        }else if(!player.isSneaking() && blockEntity.canBeCamoStack(player.getItemInHand(hand))){
            Item item = player.getItemInHand(hand).getItem();
            if(item instanceof BlockItem){
                Block block = ((BlockItem)item).getBlock();
                BlockState state1 = block.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, hand, new BlockRayTraceResult(hitLocation, hitSide, pos, false))));
                if(state1 == null)
                    state1 = block.defaultBlockState();
                blockEntity.setCamoState(state1);
            }
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return this.entitySupplier.get();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state){
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader level, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType){
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
        TileEntity entity;
        if(reader instanceof World){
            Chunk chunk = ((World)reader).getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
            entity = chunk == null ? reader.getBlockEntity(pos) : chunk.getBlockEntity(pos);
        }else
            entity = reader.getBlockEntity(pos);
        return entity instanceof CamoBlockEntity && ((CamoBlockEntity)entity).hasCamoState() ? ((CamoBlockEntity)entity).getCamoState().getLightBlock(reader, pos) : reader.getMaxLightLevel();
    }
}
