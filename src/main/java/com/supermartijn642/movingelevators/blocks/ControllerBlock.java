package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class ControllerBlock extends ElevatorInputBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ControllerBlock(String registryName, Properties properties){
        super(registryName, properties, ControllerBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(BlockState state, Level worldIn, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
        if(player != null && player.getItemInHand(handIn).getItem() instanceof RemoteControllerBlockItem && blockEntity instanceof ControllerBlockEntity){
            if(!worldIn.isClientSide){
                ItemStack stack = player.getItemInHand(handIn);
                CompoundTag tag = stack.getOrCreateTag();
                tag.putString("controllerDim", worldIn.dimension().location().toString());
                tag.putInt("controllerX", pos.getX());
                tag.putInt("controllerY", pos.getY());
                tag.putInt("controllerZ", pos.getZ());
                tag.putInt("controllerFacing", ((ControllerBlockEntity)blockEntity).getFacing().get2DDataValue());
                player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.bind").get(), true);
            }
            return true;
        }

        if(super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, rayTraceResult))
            return true;

        if(state.getValue(FACING) != rayTraceResult.getDirection()){
            if(worldIn.isClientSide)
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
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos){
        BlockEntity entity = world.getBlockEntity(pos);
        if(entity instanceof ControllerBlockEntity
            && ((ControllerBlockEntity)entity).hasGroup()
            && ((ControllerBlockEntity)entity).getGroup().isCageAvailableAt((ControllerBlockEntity)entity)){
            return 15;
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter reader, List<Component> tooltips, TooltipFlag advanced){
        super.appendHoverText(stack, reader, tooltips, advanced);
        tooltips.add(TextComponents.translation("movingelevators.elevator_controller.tooltip").color(ChatFormatting.AQUA).get());
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
