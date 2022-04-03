package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class ControllerBlock extends ElevatorInputBlock {

    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    public ControllerBlock(String registryName, Properties properties){
        super(registryName, properties, ControllerBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(BlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(player != null && player.getItemInHand(handIn).getItem() instanceof RemoteControllerBlockItem){
            if(!worldIn.isClientSide){
                ItemStack stack = player.getItemInHand(handIn);
                CompoundNBT tag = stack.getOrCreateTag();
                tag.putInt("controllerDim", worldIn.dimension.getType().getId());
                tag.putInt("controllerX", pos.getX());
                tag.putInt("controllerY", pos.getY());
                tag.putInt("controllerZ", pos.getZ());
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
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos){
        TileEntity entity = world.getBlockEntity(pos);
        if(entity instanceof ControllerBlockEntity
            && ((ControllerBlockEntity)entity).hasGroup()
            && ((ControllerBlockEntity)entity).getGroup().isCageAvailableAt((ControllerBlockEntity)entity)){
            return 15;
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader reader, List<ITextComponent> tooltips, ITooltipFlag advanced){
        super.appendHoverText(stack, reader, tooltips, advanced);
        tooltips.add(TextComponents.translation("movingelevators.elevator_controller.tooltip").color(TextFormatting.AQUA).get());
    }

    @Override
    public void onRemove(BlockState state, World level, BlockPos pos, BlockState newState, boolean p_196243_5_){
        if(state.hasTileEntity() && (state.getBlock() != newState.getBlock() || !newState.hasTileEntity())){
            TileEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ControllerBlockEntity)
                ((ControllerBlockEntity)entity).onRemove();
        }
        super.onRemove(state, level, pos, newState, p_196243_5_);
    }
}
