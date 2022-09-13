package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BlockProperties;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 4/8/2020 by SuperMartijn642
 */
public class DisplayBlock extends CamoBlock {

    public static final int BUTTON_COUNT = 3;
    public static final int BUTTON_COUNT_BIG = 7;
    public static final float BUTTON_HEIGHT = 4 / 32f;

    public DisplayBlock(BlockProperties properties){
        super(properties, DisplayBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(BlockState state, Level level, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(blockEntity instanceof DisplayBlockEntity){
            DisplayBlockEntity displayEntity = (DisplayBlockEntity)blockEntity;
            if(displayEntity.getFacing() == hitSide){
                if(!level.isClientSide){
                    int displayCat = displayEntity.getDisplayCategory();

                    Vec3 hitVec = hitLocation.subtract(pos.getX(), pos.getY(), pos.getZ());
                    double hitHorizontal = hitSide.getAxis() == Direction.Axis.Z ? hitVec.x : hitVec.z;
                    double hitY = hitVec.y;

                    if(hitHorizontal > 2 / 32d && hitHorizontal < 30 / 32d){
                        BlockPos inputEntityPos = null;
                        int button_count = -1;
                        int height = -1;

                        if(displayCat == 1){ // single
                            if(hitY > 2 / 32d && hitY < 30 / 32d){
                                inputEntityPos = pos.below();
                                button_count = BUTTON_COUNT;
                                height = 1;
                            }
                        }else if(displayCat == 2){ // bottom
                            if(hitY > 2 / 32d){
                                inputEntityPos = pos.below();
                                button_count = BUTTON_COUNT_BIG;
                                height = 2;
                            }
                        }else if(displayCat == 3){ // top
                            if(hitY < 30 / 32d){
                                inputEntityPos = pos.below(2);
                                button_count = BUTTON_COUNT_BIG;
                                height = 2;
                                hitY++;
                            }
                        }

                        if(inputEntityPos != null){
                            BlockEntity blockEntity2 = level.getBlockEntity(inputEntityPos);
                            if(blockEntity2 instanceof ElevatorInputBlockEntity && ((ElevatorInputBlockEntity)blockEntity2).hasGroup()){
                                ElevatorInputBlockEntity inputEntity = (ElevatorInputBlockEntity)blockEntity2;

                                ElevatorGroup group = inputEntity.getGroup();
                                int index = inputEntity.getGroup().getFloorNumber(inputEntity.getFloorLevel());
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

                                if(player == null || player.getItemInHand(hand).isEmpty() || !(player.getItemInHand(hand).getItem() instanceof DyeItem))
                                    inputEntity.getGroup().onDisplayPress(inputEntity.getFloorLevel(), floorOffset);
                                else{
                                    DyeColor color = ((DyeItem)player.getItemInHand(hand).getItem()).getDyeColor();
                                    int floor = group.getFloorNumber(inputEntity.getFloorLevel()) + floorOffset;
                                    ControllerBlockEntity elevatorEntity = group.getEntityForFloor(floor);
                                    if(elevatorEntity != null)
                                        elevatorEntity.setDisplayLabelColor(color);
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }

        return super.onRightClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("movingelevators.elevator_display.tooltip").color(ChatFormatting.AQUA).get());
    }
}
