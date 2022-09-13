package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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
    protected boolean onRightClick(IBlockState state, World level, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        if(blockEntity instanceof DisplayBlockEntity){
            DisplayBlockEntity displayEntity = (DisplayBlockEntity)blockEntity;
            if(displayEntity.getFacing() == hitSide){
                if(!level.isRemote){
                    int displayCat = displayEntity.getDisplayCategory();

                    Vec3d hitVec = hitLocation.subtract(pos.getX(), pos.getY(), pos.getZ());
                    double hitHorizontal = hitSide.getAxis() == EnumFacing.Axis.Z ? hitVec.x : hitVec.z;
                    double hitY = hitVec.y;

                    if(hitHorizontal > 2 / 32d && hitHorizontal < 30 / 32d){
                        BlockPos inputEntityPos = null;
                        int button_count = -1;
                        int height = -1;

                        if(displayCat == 1){ // single
                            if(hitY > 2 / 32d && hitY < 30 / 32d){
                                inputEntityPos = pos.down();
                                button_count = BUTTON_COUNT;
                                height = 1;
                            }
                        }else if(displayCat == 2){ // bottom
                            if(hitY > 2 / 32d){
                                inputEntityPos = pos.down();
                                button_count = BUTTON_COUNT_BIG;
                                height = 2;
                            }
                        }else if(displayCat == 3){ // top
                            if(hitY < 30 / 32d){
                                inputEntityPos = pos.down(2);
                                button_count = BUTTON_COUNT_BIG;
                                height = 2;
                                hitY++;
                            }
                        }

                        if(inputEntityPos != null){
                            TileEntity blockEntity2 = level.getTileEntity(inputEntityPos);
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

                                if(player == null || player.getHeldItem(hand).isEmpty() || !(player.getHeldItem(hand).getItem() instanceof ItemDye))
                                    inputEntity.getGroup().onDisplayPress(inputEntity.getFloorLevel(), floorOffset);
                                else{
                                    EnumDyeColor color = EnumDyeColor.byDyeDamage(player.getHeldItem(hand).getMetadata());
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
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("movingelevators.elevator_display.tooltip").color(TextFormatting.AQUA).get());
    }
}
