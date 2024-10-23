package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class CamoBlock extends BaseBlock implements EntityHoldingBlock {

    private static final IntegerProperty OPACITY = IntegerProperty.create("opacity", 0, 15);

    private final BiFunction<BlockPos,BlockState,? extends CamoBlockEntity> entitySupplier;

    public CamoBlock(BlockProperties properties, BiFunction<BlockPos,BlockState,? extends CamoBlockEntity> entitySupplier){
        super(false, properties.toUnderlying().pushReaction(PushReaction.BLOCK).dynamicShape());
        this.entitySupplier = entitySupplier;
        this.registerDefaultState(this.defaultBlockState().setValue(OPACITY, 15));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof CamoBlockEntity)
            this.onRightClick(state, level, (CamoBlockEntity)blockEntity, pos, player, hand, hitSide, hitLocation);

        // Always return success to prevent accidentally placing blocks
        return InteractionFeedback.SUCCESS;
    }

    /**
     * @return whether the interaction has been handled
     */
    protected boolean onRightClick(BlockState state, Level level, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()){
            blockEntity.setCamoState(null);
            if(state.getValue(OPACITY) != 15)
                level.setBlock(pos, state.setValue(OPACITY, 15), Block.UPDATE_ALL);
            return true;
        }else if(!player.isShiftKeyDown() && blockEntity.canBeCamoStack(player.getItemInHand(hand))){
            Item item = player.getItemInHand(hand).getItem();
            if(item instanceof BlockItem){
                Block block = ((BlockItem)item).getBlock();
                BlockState camoState = block.getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, hand, new BlockHitResult(hitLocation, hitSide, pos, false))));
                if(camoState == null)
                    camoState = block.defaultBlockState();
                blockEntity.setCamoState(camoState);
                int opacity = Math.max(0, Math.min(15, camoState.getLightBlock()));
                if(opacity != state.getValue(OPACITY))
                    level.setBlock(pos, state.setValue(OPACITY, opacity), Block.UPDATE_ALL);
            }
            return true;
        }
        return false;
    }

    @Override
    public BlockEntity createNewBlockEntity(BlockPos pos, BlockState state){
        return this.entitySupplier.apply(pos, state);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context){
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getVisualShape(reader, pos, context) : BlockShape.fullCube().getUnderlying();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter reader, BlockPos pos){
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getShadeBrightness(reader, pos) : super.getShadeBrightness(state, reader, pos);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state){
        return state.getValue(OPACITY) == 0;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state){
        return BlockShape.empty().getUnderlying();
    }

    @Override
    public int getLightBlock(BlockState state){
        return state.getValue(OPACITY);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        super.createBlockStateDefinition(builder);
        builder.add(OPACITY);
    }
}
