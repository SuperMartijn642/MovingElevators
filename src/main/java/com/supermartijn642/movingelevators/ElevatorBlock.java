package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class ElevatorBlock extends ElevatorInputBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ElevatorBlock(){
        super("elevator_block", ElevatorBlockTile::new);
    }

    @Override
    protected void onRightClick(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
        if(player != null && player.getItemInHand(handIn).getItem() instanceof ButtonBlockItem){
            if(!worldIn.isClientSide){
                ItemStack stack = player.getItemInHand(handIn);
                CompoundTag tag = stack.getOrCreateTag();
                tag.putString("controllerDim", worldIn.dimension().getRegistryName().toString());
                tag.putInt("controllerX", pos.getX());
                tag.putInt("controllerY", pos.getY());
                tag.putInt("controllerZ", pos.getZ());
                player.sendMessage(new TranslatableComponent("block.movingelevators.button_block.bind").withStyle(ChatFormatting.YELLOW), player.getUUID());
            }
        }else if(state.getValue(FACING) != rayTraceResult.getDirection()){
            if(worldIn.isClientSide)
                ClientProxy.openElevatorScreen(pos);
        }else
            super.onRightClick(state, worldIn, pos, player, handIn, rayTraceResult);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(FACING);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        if(state.getBlock() != newState.getBlock()){
            BlockEntity tile = worldIn.getBlockEntity(pos);
            if(tile instanceof ElevatorBlockTile)
                ((ElevatorBlockTile)tile).onBreak();
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level worldIn, BlockPos pos){
        if(!state.hasProperty(FACING))
            return 0;
        return worldIn.isEmptyBlock(pos.relative(state.getValue(FACING)).below()) ? 0 : 15;
    }
}
