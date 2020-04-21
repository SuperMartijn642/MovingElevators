package com.supermartijn642.movingelevators;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 4/8/2020 by SuperMartijn642
 */
public class DisplayBlock extends MEBlock {

    public static final int BUTTON_COUNT = 3;
    public static final int BUTTON_COUNT_BIG = 7;
    public static final float BUTTON_HEIGHT = 4 / 32f;

    public DisplayBlock(){
        super("display_block", METile::new);
    }

    @Override
    protected void onRightClick(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(!worldIn.isRemote){
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof METile){
                METile meTile = (METile)tile;
                if(meTile.getFacing() == facing){
                    int displayCat = meTile.getDisplayCategory();
                    double hitHorizontal = facing.getAxis() == EnumFacing.Axis.Z ? hitX : hitZ;
                    if(hitHorizontal > 2 / 32d && hitHorizontal < 30 / 32d){
                        int floorOffset = 0;
                        BlockPos elevatorPos = null;

                        if(displayCat == 1){ // single
                            if(hitY > 2 / 32d && hitY < 30 / 32d){
                                floorOffset = (int)Math.floor((hitY - 2 / 32d) / (28 / 32d / (BUTTON_COUNT * 2 + 1))) - BUTTON_COUNT;
                                elevatorPos = pos.down();
                            }
                        }else if(displayCat == 2){ // bottom
                            if(hitY > 2 / 32d){
                                floorOffset = (int)Math.floor((hitY - 2 / 32d) / (60 / 32d / (BUTTON_COUNT_BIG * 2 + 1))) - BUTTON_COUNT_BIG;
                                elevatorPos = pos.down();
                            }
                        }else if(displayCat == 3){ // top
                            if(hitY < 30 / 32d){
                                floorOffset = (int)Math.floor((hitY - 2 / 32d + 1) / (60 / 32d / (BUTTON_COUNT_BIG * 2 + 1))) - BUTTON_COUNT_BIG;
                                elevatorPos = pos.down(2);
                            }
                        }

                        if(elevatorPos == null)
                            return;

                        TileEntity elevatorTile = worldIn.getTileEntity(elevatorPos);
                        if(elevatorTile instanceof ElevatorBlockTile){
                            if(player == null || player.getHeldItem(handIn).isEmpty() || !(player.getHeldItem(handIn).getItem() instanceof ItemDye))
                                ((ElevatorBlockTile)elevatorTile).getGroup().onDisplayPress(elevatorPos.getY(), floorOffset);
                            else{
                                EnumDyeColor color = EnumDyeColor.byDyeDamage(player.getHeldItem(handIn).getMetadata());
                                ElevatorGroup group = ((ElevatorBlockTile)elevatorTile).getGroup();
                                int floor = group.getFloorNumber(elevatorPos.getY()) + floorOffset;
                                ElevatorBlockTile elevatorTile2 = group.getTileForFloor(floor);
                                if(elevatorTile2 != null)
                                    elevatorTile2.setDisplayLabelColor(color);
                            }
                        }
                    }
                }
            }
        }
    }
}
