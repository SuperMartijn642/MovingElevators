package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.ItemProperties;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlockItem extends BaseBlockItem {

    public RemoteControllerBlockItem(Block block, ItemProperties properties){
        super(block, properties);
    }

    @Override
    public ItemUseResult interact(ItemStack stack, EntityPlayer player, EnumHand hand, World level){
        if(player.isSneaking()){
            if(stack.hasTagCompound() && stack.getTagCompound().hasKey("controllerDim")){
                if(!level.isRemote){
                    NBTTagCompound tag = stack.getTagCompound();
                    tag.removeTag("controllerDim");
                    tag.removeTag("controllerX");
                    tag.removeTag("controllerY");
                    tag.removeTag("controllerZ");
                    player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.clear").get(), true);
                }
                return ItemUseResult.success(stack);
            }
        }else{
            if(!level.isRemote){
                if(stack.hasTagCompound() && stack.getTagCompound().hasKey("controllerDim")){
                    NBTTagCompound compound = stack.getTagCompound();
                    ITextComponent x = TextComponents.number(compound.getInteger("controllerX")).color(TextFormatting.GOLD).get();
                    ITextComponent y = TextComponents.number(compound.getInteger("controllerY")).color(TextFormatting.GOLD).get();
                    ITextComponent z = TextComponents.number(compound.getInteger("controllerZ")).color(TextFormatting.GOLD).get();
                    ITextComponent dimension = TextComponents.dimension(DimensionType.getById(compound.getInteger("controllerDim"))).color(TextFormatting.GOLD).get();
                    player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).get(), true);
                }else
                    player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.tooltip").get(), true);
            }
            return ItemUseResult.success(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, EntityPlayer player, EnumHand hand, World level, BlockPos hitPos, EnumFacing hitSide, Vec3d hitLocation){
        NBTTagCompound tag = player.getHeldItem(hand).getTagCompound();
        if(tag == null || !tag.hasKey("controllerDim")){
            if(player != null && !level.isRemote)
                player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.not_bound").color(TextFormatting.RED).get(), true);
            return InteractionFeedback.CONSUME;
        }
        if(tag.getInteger("controllerDim") != level.provider.getDimensionType().getId()){
            if(player != null && !level.isRemote)
                player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.wrong_dimension").color(TextFormatting.RED).get(), true);
            return InteractionFeedback.CONSUME;
        }
        return super.interactWithBlock(stack, player, hand, level, hitPos, hitSide, hitLocation);
    }
}
