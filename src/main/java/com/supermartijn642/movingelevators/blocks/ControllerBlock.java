package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class ControllerBlock extends ElevatorInputBlock {

    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    public ControllerBlock(BlockProperties properties){
        super(properties, ControllerBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(BlockState state, World level, CamoBlockEntity blockEntity, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vector3d hitLocation){
        if(player != null && player.getItemInHand(hand).getItem() instanceof RemoteControllerBlockItem && blockEntity instanceof ControllerBlockEntity){
            if(!level.isClientSide){
                ItemStack stack = player.getItemInHand(hand);
                CompoundNBT tag = stack.getOrCreateTag();
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
    public BlockState getStateForPlacement(BlockItemUseContext context){
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(FACING);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World level, BlockPos pos){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof ControllerBlockEntity
            && ((ControllerBlockEntity)entity).hasGroup()
            && ((ControllerBlockEntity)entity).getGroup().isCageAvailableAt((ControllerBlockEntity)entity)){
            return 15;
        }
        return 0;
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("movingelevators.elevator_controller.tooltip").color(TextFormatting.AQUA).get());
    }

    @Override
    public void onRemove(BlockState state, World level, BlockPos pos, BlockState newState, boolean p_196243_5_){
        if(state.hasTileEntity() && (!state.is(newState.getBlock()) || !newState.hasTileEntity())){
            TileEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ControllerBlockEntity)
                ((ControllerBlockEntity)entity).onRemove();
        }
        super.onRemove(state, level, pos, newState, p_196243_5_);
    }
}
