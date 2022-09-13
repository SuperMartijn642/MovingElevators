package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class ControllerBlock extends ElevatorInputBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ControllerBlock(BlockProperties properties){
        super(properties, ControllerBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(BlockState state, Level level, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(player != null && player.getItemInHand(hand).getItem() instanceof RemoteControllerBlockItem && blockEntity instanceof ControllerBlockEntity){
            if(!level.isClientSide){
                ItemStack stack = player.getItemInHand(hand);
                CompoundTag tag = stack.getOrCreateTag();
                tag.putString("controllerDim", level.dimension().location().toString());
                tag.putInt("controllerX", pos.getX());
                tag.putInt("controllerY", pos.getY());
                tag.putInt("controllerZ", pos.getZ());
                tag.putInt("controllerFacing", ((ControllerBlockEntity)blockEntity).getFacing().get2DDataValue());
                player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.bind").get(), true);
            }
            return true;
        }

        if(super.onRightClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation))
            return true;

        if(state.getValue(FACING) != hitSide){
            if(level.isClientSide)
                MovingElevatorsClient.openElevatorScreen(pos);
            return true;
        }
        return false;
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
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof ControllerBlockEntity
            && ((ControllerBlockEntity)entity).hasGroup()
            && ((ControllerBlockEntity)entity).getGroup().isCageAvailableAt((ControllerBlockEntity)entity)){
            return 15;
        }
        return 0;
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("movingelevators.elevator_controller.tooltip").color(ChatFormatting.AQUA).get());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean p_60519_){
        if(state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())){
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ControllerBlockEntity)
                ((ControllerBlockEntity)entity).onRemove();
        }
        super.onRemove(state, level, pos, newState, p_60519_);
    }
}
