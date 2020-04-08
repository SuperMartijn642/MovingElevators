package com.supermartijn642.movingelevators;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Created 4/8/2020 by SuperMartijn642
 */
public class DisplayBlock extends MEBlock {

    public static final int BUTTON_COUNT = 3;
    public static final int BUTTON_COUNT_BIG = 7;
    public static final float BUTTON_HEIGHT = 4 / 32f;

    public DisplayBlock(){
        super("display_block", () -> new METile(MovingElevators.display_tile));
    }

    @Override
    protected void onRightClick(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(!worldIn.isRemote){
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof METile){
                METile meTile = (METile)tile;
                if(meTile.getFacing() == rayTraceResult.getFace()){
                    int displayCat = meTile.getDisplayCategory();
                    Vec3d hitVec = rayTraceResult.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
                    double hitHorizontal = rayTraceResult.getFace().getAxis() == Direction.Axis.Z ? hitVec.x : hitVec.z;
                    double hitY = hitVec.y;
                    if(hitHorizontal > 2 / 32d && hitHorizontal < 30 / 32d){
                        if(displayCat == 1){
                            if(hitY > 2 / 32d && hitY < 30 / 32d){
                                int floorOffset = (int)Math.floor((hitY - 2 / 32d) / (28 / 32d / (BUTTON_COUNT * 2 + 1))) - BUTTON_COUNT;
                                TileEntity tile2 = worldIn.getTileEntity(pos.down());
                                if(tile2 instanceof ElevatorBlockTile)
                                    ((ElevatorBlockTile)tile2).getGroup().onDisplayPress(pos.getY() - 1, floorOffset);
                            }
                        }else if(displayCat == 2){
                            if(hitY > 2 / 32d){
                                int floorOffset = (int)Math.floor((hitY - 2 / 32d) / (60 / 32d / (BUTTON_COUNT_BIG * 2 + 1))) - BUTTON_COUNT_BIG;
                                TileEntity tile2 = worldIn.getTileEntity(pos.down());
                                if(tile2 instanceof ElevatorBlockTile)
                                    ((ElevatorBlockTile)tile2).getGroup().onDisplayPress(pos.getY() - 1, floorOffset);
                            }
                        }else if(displayCat == 3){
                            if(hitY < 30 / 32d){
                                int floorOffset = (int)Math.floor((hitY - 2 / 32d + 1) / (60 / 32d / (BUTTON_COUNT_BIG * 2 + 1))) - BUTTON_COUNT_BIG;
                                TileEntity tile2 = worldIn.getTileEntity(pos.down(2));
                                if(tile2 instanceof ElevatorBlockTile)
                                    ((ElevatorBlockTile)tile2).getGroup().onDisplayPress(pos.getY() - 2, floorOffset);
                            }
                        }
                    }
                }
            }
        }
    }
}
