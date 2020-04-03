package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class ElevatorBlock extends Block {

    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    public ElevatorBlock(){
        super(Properties.create(Material.ROCK, MaterialColor.GRAY).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).hardnessAndResistance(1.5F, 6.0F));
        this.setRegistryName("elevator_block");
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(worldIn.isRemote && state.get(FACING) != rayTraceResult.getFace()){
            Minecraft.getInstance().displayGuiScreen(new ElevatorScreen(pos));
        }
        else if(!worldIn.isRemote && state.get(FACING) == rayTraceResult.getFace()) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof ElevatorBlockTile){
                ElevatorBlockTile elevator = (ElevatorBlockTile)tile;
                if(elevator.getFacing() == rayTraceResult.getFace()){
                    double y = rayTraceResult.getHitVec().y - pos.getY();
                    ((ElevatorBlockTile)tile).onButtonPress(y > 2 / 3D, y < 1 / 3D, pos.getY());
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return new ElevatorBlockTile();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(FACING);
    }

    @Override
    public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state){
        super.onPlayerDestroy(worldIn, pos, state);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        if(state.getBlock() != newState.getBlock()){
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof ElevatorBlockTile)
                ((ElevatorBlockTile)tile).onBreak(state.get(FACING));
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
}
