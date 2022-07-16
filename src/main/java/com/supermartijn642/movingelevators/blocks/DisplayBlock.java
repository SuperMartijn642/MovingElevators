package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 4/8/2020 by SuperMartijn642
 */
public class DisplayBlock extends CamoBlock {

    public static final int BUTTON_COUNT = 3;
    public static final int BUTTON_COUNT_BIG = 7;
    public static final float BUTTON_HEIGHT = 4 / 32f;

    public DisplayBlock(String registryName, Properties properties){
        super(registryName, properties, DisplayBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(IBlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(blockEntity instanceof DisplayBlockEntity){
            DisplayBlockEntity displayTile = (DisplayBlockEntity)blockEntity;
            if(displayTile.getFacing() == facing){
                if(!worldIn.isRemote){
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

                        if(inputTilePos != null){
                            TileEntity blockEntity2 = worldIn.getTileEntity(inputTilePos);
                            if(blockEntity2 instanceof ElevatorInputBlockEntity && ((ElevatorInputBlockEntity)blockEntity2).hasGroup()){
                                ElevatorInputBlockEntity inputTile = (ElevatorInputBlockEntity)blockEntity2;

                                ElevatorGroup group = inputTile.getGroup();
                                int index = inputTile.getGroup().getFloorNumber(inputTile.getFloorLevel());
                                int below = index;
                                int above = group.getFloorCount() - index - 1;
                                if(below < above){
                                    below = Math.min(below, button_count);
                                    above = Math.min(above, button_count * 2 - below);
                                }else{
                                    above = Math.min(above, button_count);
                                    below = Math.min(below, button_count * 2 - above);
                                }
                                int startIndex = index - below;
                                int total = below + 1 + above;

                                int floorOffset = (int)Math.floor((hitY - (height - total * BUTTON_HEIGHT) / 2d) / BUTTON_HEIGHT) + startIndex - index;

                                if(player == null || player.getHeldItem(handIn).isEmpty() || !(player.getHeldItem(handIn).getItem() instanceof ItemDye))
                                    inputTile.getGroup().onDisplayPress(inputTile.getFloorLevel(), floorOffset);
                                else{
                                    EnumDyeColor color = EnumDyeColor.byDyeDamage(player.getHeldItem(handIn).getMetadata());
                                    int floor = group.getFloorNumber(inputTile.getFloorLevel()) + floorOffset;
                                    ControllerBlockEntity elevatorTile = group.getTileForFloor(floor);
                                    if(elevatorTile != null)
                                        elevatorTile.setDisplayLabelColor(color);
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }

        return super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, facing, hitX, hitY, hitZ);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World reader, List<String> tooltips, ITooltipFlag advanced){
        super.addInformation(stack, reader, tooltips, advanced);
        tooltips.add(TextComponents.translation("movingelevators.elevator_display.tooltip").color(TextFormatting.AQUA).format());
    }
}
