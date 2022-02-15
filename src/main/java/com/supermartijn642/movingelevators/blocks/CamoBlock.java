package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class CamoBlock extends BaseBlock implements EntityBlock {

    private final BiFunction<BlockPos,BlockState,? extends CamoBlockEntity> tileSupplier;

    public CamoBlock(String registryName, Properties properties, BiFunction<BlockPos,BlockState,? extends CamoBlockEntity> tileSupplier){
        super(registryName, false, properties.dynamicShape());
        this.tileSupplier = tileSupplier;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if(blockEntity instanceof CamoBlockEntity)
            this.onRightClick(state, worldIn, (CamoBlockEntity)blockEntity, pos, player, handIn, rayTraceResult);

        // Always return success to prevent accidentally placing blocks
        return InteractionResult.sidedSuccess(worldIn.isClientSide);
    }

    /**
     * @return whether the interaction has been handled
     */
    protected boolean onRightClick(BlockState state, Level worldIn, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
        if(player.isShiftKeyDown() && player.getItemInHand(handIn).isEmpty()){
            blockEntity.setCamoState(null);
            return true;
        }else if(!player.isShiftKeyDown() && blockEntity.canBeCamoStack(player.getItemInHand(handIn))){
            Item item = player.getItemInHand(handIn).getItem();
            if(item instanceof BlockItem){
                Block block = ((BlockItem)item).getBlock();
                BlockState state1 = block.getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, handIn, rayTraceResult)));
                if(state1 == null)
                    state1 = block.defaultBlockState();
                blockEntity.setCamoState(state1);
            }
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return this.tileSupplier.apply(pos, state);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state){
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, @Nullable EntityType<?> entityType){
        return false;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context){
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getVisualShape(reader, pos, context) : BlockShape.fullCube().getUnderlying();
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter reader, BlockPos pos){
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getLightEmission(reader, pos) : super.getLightEmission(state, reader, pos);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter reader, BlockPos pos){
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getShadeBrightness(reader, pos) : super.getShadeBrightness(state, reader, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos){
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().propagatesSkylightDown(reader, pos) : super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos){
        return BlockShape.empty().getUnderlying();
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter reader, BlockPos pos){
        BlockEntity blockEntity = reader instanceof Level ? ((Level)reader).getChunkSource().getChunkForLighting(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())).getBlockEntity(pos) : reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getLightBlock(reader, pos) : reader.getMaxLightLevel();
    }
}
