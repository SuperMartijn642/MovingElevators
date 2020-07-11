package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import com.supermartijn642.movingelevators.base.MEBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Created 4/8/2020 by SuperMartijn642
 */
public class DisplayBlock extends MEBlock {

    public static final int BUTTON_COUNT = 3;
    public static final int BUTTON_COUNT_BIG = 7;
    public static final float BUTTON_HEIGHT = 4 / 32f;

    public DisplayBlock(){
        super("display_block", DisplayBlockTile::new);
    }

    @Override
    protected void onRightClick(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(!worldIn.isRemote){
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof DisplayBlockTile){
                DisplayBlockTile displayTile = (DisplayBlockTile)tile;
                if(displayTile.getFacing() == rayTraceResult.getFace()){
                    int displayCat = displayTile.getDisplayCategory();
                    Vector3d hitVec = rayTraceResult.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
                    double hitHorizontal = rayTraceResult.getFace().getAxis() == Direction.Axis.Z ? hitVec.x : hitVec.z;
                    double hitY = hitVec.y;
                    if(hitHorizontal > 2 / 32d && hitHorizontal < 30 / 32d){
                        int floorOffset = 0;
                        BlockPos inputTilePos = null;

                        if(displayCat == 1){ // single
                            if(hitY > 2 / 32d && hitY < 30 / 32d){
                                floorOffset = (int)Math.floor((hitY - 2 / 32d) / (28 / 32d / (BUTTON_COUNT * 2 + 1))) - BUTTON_COUNT;
                                inputTilePos = pos.down();
                            }
                        }else if(displayCat == 2){ // bottom
                            if(hitY > 2 / 32d){
                                floorOffset = (int)Math.floor((hitY - 2 / 32d) / (60 / 32d / (BUTTON_COUNT_BIG * 2 + 1))) - BUTTON_COUNT_BIG;
                                inputTilePos = pos.down();
                            }
                        }else if(displayCat == 3){ // top
                            if(hitY < 30 / 32d){
                                floorOffset = (int)Math.floor((hitY - 2 / 32d + 1) / (60 / 32d / (BUTTON_COUNT_BIG * 2 + 1))) - BUTTON_COUNT_BIG;
                                inputTilePos = pos.down(2);
                            }
                        }

                        if(inputTilePos == null)
                            return;

                        TileEntity tile2 = worldIn.getTileEntity(inputTilePos);
                        if(tile2 instanceof ElevatorInputTile && ((ElevatorInputTile)tile2).hasGroup()){
                            ElevatorInputTile inputTile = (ElevatorInputTile)tile2;
                            if(player == null || player.getHeldItem(handIn).isEmpty() || !(player.getHeldItem(handIn).getItem() instanceof DyeItem))
                                inputTile.getGroup().onDisplayPress(inputTile.getFloorLevel(), floorOffset);
                            else{
                                DyeColor color = ((DyeItem)player.getHeldItem(handIn).getItem()).getDyeColor();
                                ElevatorGroup group = inputTile.getGroup();
                                int floor = group.getFloorNumber(inputTile.getFloorLevel()) + floorOffset;
                                ElevatorBlockTile elevatorTile = group.getTileForFloor(floor);
                                if(elevatorTile != null)
                                    elevatorTile.setDisplayLabelColor(color);
                            }
                        }
                    }
                }
            }
        }
    }
}
