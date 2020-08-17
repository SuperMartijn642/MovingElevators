package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import com.supermartijn642.movingelevators.base.MEBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

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
    protected void onRightClick(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote)
            return;

        TileEntity tile = worldIn.getTileEntity(pos);
        if(!(tile instanceof DisplayBlockTile))
            return;
        DisplayBlockTile displayTile = (DisplayBlockTile)tile;

        if(displayTile.getFacing() == facing){
            int displayCat = displayTile.getDisplayCategory();

            double hitHorizontal = facing.getAxis() == EnumFacing.Axis.Z ? hitX : hitZ;

            if(hitHorizontal > 2 / 32d && hitHorizontal < 30 / 32d){
                BlockPos inputTilePos = null;
                int button_count = -1;
                int height = -1;

                if(displayCat == 1){ // single
                    if(hitY > 2 / 32d && hitY < 30 / 32d){
                        inputTilePos = pos.down();
                        button_count = BUTTON_COUNT;
                        height = 1;
                    }
                }else if(displayCat == 2){ // bottom
                    if(hitY > 2 / 32d){
                        inputTilePos = pos.down();
                        button_count = BUTTON_COUNT_BIG;
                        height = 2;
                    }
                }else if(displayCat == 3){ // top
                    if(hitY < 30 / 32d){
                        inputTilePos = pos.down(2);
                        button_count = BUTTON_COUNT_BIG;
                        height = 2;
                        hitY++;
                    }
                }

                if(inputTilePos == null)
                    return;

                tile = worldIn.getTileEntity(inputTilePos);
                if(tile instanceof ElevatorInputTile && ((ElevatorInputTile)tile).hasGroup()){
                    ElevatorInputTile inputTile = (ElevatorInputTile)tile;

                    List<ElevatorBlockTile> allTiles = inputTile.getGroup().getTiles();
                    int index = inputTile.getGroup().getFloorNumber(inputTile.getFloorLevel());
                    int below = Math.min(index, button_count);
                    int above = Math.min(allTiles.size() - index - 1, button_count + (button_count - below));
                    below = Math.min(below, button_count + (button_count - above));
                    int total = below + 1 + above;

                    int floorOffset = (int)Math.floor((hitY - (height - total * BUTTON_HEIGHT) / 2d) / BUTTON_HEIGHT) - index;

                    if(player == null || player.getHeldItem(handIn).isEmpty() || !(player.getHeldItem(handIn).getItem() instanceof ItemDye))
                        inputTile.getGroup().onDisplayPress(inputTile.getFloorLevel(), floorOffset);
                    else{
                        EnumDyeColor color = EnumDyeColor.byDyeDamage(player.getHeldItem(handIn).getMetadata());
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
