package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ButtonBlockItem extends ItemBlock {

    public ButtonBlockItem(Block block){
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        NBTTagCompound tag = player.getHeldItem(hand).getTagCompound();
        if(tag == null || !tag.hasKey("controllerDim")){
            if(player != null && !player.world.isRemote)
                player.sendMessage(new TextComponentTranslation("block.movingelevators.button_block.place").setStyle(new Style().setColor(TextFormatting.RED)));
            return EnumActionResult.FAIL;
        }
        if(tag.getInteger("controllerDim") != worldIn.provider.getDimensionType().getId()){
            if(player != null && !player.world.isRemote)
                player.sendMessage(new TextComponentTranslation("block.movingelevators.button_block.dimension").setStyle(new Style().setColor(TextFormatting.RED)));
            return EnumActionResult.FAIL;
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }
}
