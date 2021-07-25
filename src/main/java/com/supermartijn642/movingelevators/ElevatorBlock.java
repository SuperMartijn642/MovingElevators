package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputBlock;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class ElevatorBlock extends ElevatorInputBlock {

    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    public ElevatorBlock(){
        super("elevator_block", ElevatorBlockTile::new);
    }

    @Override
    protected void onRightClick(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(player != null && player.getItemInHand(handIn).getItem() instanceof ButtonBlockItem){
            if(!worldIn.isClientSide){
                ItemStack stack = player.getItemInHand(handIn);
                CompoundNBT tag = stack.getOrCreateTag();
                tag.putString("controllerDim", worldIn.dimension().getRegistryName().toString());
                tag.putInt("controllerX", pos.getX());
                tag.putInt("controllerY", pos.getY());
                tag.putInt("controllerZ", pos.getZ());
                player.sendMessage(new TranslationTextComponent("block.movingelevators.button_block.bind").withStyle(TextFormatting.YELLOW), player.getUUID());
            }
        }else if(state.getValue(FACING) != rayTraceResult.getDirection()){
            if(worldIn.isClientSide)
                ClientProxy.openElevatorScreen(pos);
        }else
            super.onRightClick(state, worldIn, pos, player, handIn, rayTraceResult);
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
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        if(state.getBlock() != newState.getBlock()){
            TileEntity tile = worldIn.getBlockEntity(pos);
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
    public int getAnalogOutputSignal(BlockState state, World worldIn, BlockPos pos){
        if(!state.hasProperty(FACING))
            return 0;
        return worldIn.isEmptyBlock(pos.relative(state.getValue(FACING)).below()) ? 0 : 15;
    }
}
