package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlockItem extends ItemBlock {

    public RemoteControllerBlockItem(Block blockIn){
        super(blockIn);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
        ItemStack stack = player.getHeldItem(hand);
        if(stack.getItem() == this){
            if(player.isSneaking()){
                if(stack.hasTagCompound() && stack.getTagCompound().hasKey("controllerDim")){
                    if(!world.isRemote){
                        stack.removeSubCompound("controllerDim");
                        stack.removeSubCompound("controllerX");
                        stack.removeSubCompound("controllerY");
                        stack.removeSubCompound("controllerZ");
                        player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.clear").get(), true);
                    }
                    return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
                }
            }else{
                if(!world.isRemote){
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
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        NBTTagCompound tag = player.getHeldItem(hand).getTagCompound();
        if(tag == null || !tag.hasKey("controllerDim")){
            if(player != null && !world.isRemote)
                player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.not_bound").color(TextFormatting.RED).get(), true);
            return EnumActionResult.FAIL;
        }
        if(tag.getInteger("controllerDim") != world.provider.getDimensionType().getId()){
            if(player != null && !world.isRemote)
                player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.wrong_dimension").color(TextFormatting.RED).get(), true);
            return EnumActionResult.FAIL;
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
}
