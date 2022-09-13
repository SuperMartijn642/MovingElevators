package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.ItemProperties;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlockItem extends BaseBlockItem {

    public RemoteControllerBlockItem(Block block, ItemProperties properties){
        super(block, properties);
    }

    @Override
    public ItemUseResult interact(ItemStack stack, PlayerEntity player, Hand hand, World level){
        if(player.isSneaking()){
            if(stack.hasTag() && stack.getTag().contains("controllerDim")){
                if(!level.isClientSide){
                    stack.removeTagKey("controllerDim");
                    stack.removeTagKey("controllerX");
                    stack.removeTagKey("controllerY");
                    stack.removeTagKey("controllerZ");
                    player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.clear").get(), true);
                }
                return ItemUseResult.success(stack);
            }
        }else{
            if(!level.isClientSide){
                if(stack.hasTag() && stack.getTag().contains("controllerDim")){
                    CompoundNBT compound = stack.getTag();
                    ITextComponent x = TextComponents.number(compound.getInt("controllerX")).color(TextFormatting.GOLD).get();
                    ITextComponent y = TextComponents.number(compound.getInt("controllerY")).color(TextFormatting.GOLD).get();
                    ITextComponent z = TextComponents.number(compound.getInt("controllerZ")).color(TextFormatting.GOLD).get();
                    ITextComponent dimension = TextComponents.dimension(DimensionType.getById(compound.getInt("controllerDim"))).color(TextFormatting.GOLD).get();
                    player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).get(), true);
                }else
                    player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.tooltip").get(), true);
            }
            return ItemUseResult.success(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, PlayerEntity player, Hand hand, World level, BlockPos hitPos, Direction hitSide, Vec3d hitLocation){
        CompoundNBT tag = stack.getTag();
        if(tag == null || !tag.contains("controllerDim")){
            if(player != null && !level.isClientSide)
                player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.not_bound").color(TextFormatting.RED).get(), true);
            return InteractionFeedback.CONSUME;
        }
        if(tag.getInt("controllerDim") != level.dimension.getType().getId()){
            if(player != null && !level.isClientSide)
                player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.wrong_dimension").color(TextFormatting.RED).get(), true);
            return InteractionFeedback.CONSUME;
        }
        return super.interactWithBlock(stack, player, hand, level, hitPos, hitSide, hitLocation);
    }
}
