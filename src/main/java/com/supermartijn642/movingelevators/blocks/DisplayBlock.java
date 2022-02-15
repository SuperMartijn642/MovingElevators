package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
    protected boolean onRightClick(BlockState state, Level worldIn, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
        if(blockEntity instanceof DisplayBlockEntity){
            DisplayBlockEntity displayTile = (DisplayBlockEntity)blockEntity;
            if(displayTile.getFacing() == rayTraceResult.getDirection()){
                if(!worldIn.isClientSide){
                    int displayCat = displayTile.getDisplayCategory();

                    Vec3 hitVec = rayTraceResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
                    double hitHorizontal = rayTraceResult.getDirection().getAxis() == Direction.Axis.Z ? hitVec.x : hitVec.z;
                    double hitY = hitVec.y;

                    if(hitHorizontal > 2 / 32d && hitHorizontal < 30 / 32d){
                        BlockPos inputTilePos = null;
                        int button_count = -1;
                        int height = -1;

                        if(displayCat == 1){ // single
                            if(hitY > 2 / 32d && hitY < 30 / 32d){
                                inputTilePos = pos.below();
                                button_count = BUTTON_COUNT;
                                height = 1;
                            }
                        }else if(displayCat == 2){ // bottom
                            if(hitY > 2 / 32d){
                                inputTilePos = pos.below();
                                button_count = BUTTON_COUNT_BIG;
                                height = 2;
                            }
                        }else if(displayCat == 3){ // top
                            if(hitY < 30 / 32d){
                                inputTilePos = pos.below(2);
                                button_count = BUTTON_COUNT_BIG;
                                height = 2;
                                hitY++;
                            }
                        }

                        BlockEntity blockEntity2 = worldIn.getBlockEntity(inputTilePos);
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

                            if(player == null || player.getItemInHand(handIn).isEmpty() || !(player.getItemInHand(handIn).getItem() instanceof DyeItem))
                                inputTile.getGroup().onDisplayPress(inputTile.getFloorLevel(), floorOffset);
                            else{
                                DyeColor color = ((DyeItem)player.getItemInHand(handIn).getItem()).getDyeColor();
                                int floor = group.getFloorNumber(inputTile.getFloorLevel()) + floorOffset;
                                ControllerBlockEntity elevatorTile = group.getTileForFloor(floor);
                                if(elevatorTile != null)
                                    elevatorTile.setDisplayLabelColor(color);
                            }
                        }
                    }
                }
                return true;
            }
        }

        return super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, rayTraceResult);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter reader, List<Component> tooltips, TooltipFlag advanced){
        super.appendHoverText(stack, reader, tooltips, advanced);
        tooltips.add(TextComponents.translation("movingelevators.elevator_display.tooltip").color(ChatFormatting.AQUA).get());
    }
}
