package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ButtonBlockItem extends BlockItem {

    public ButtonBlockItem(Block blockIn, Properties builder){
        super(blockIn, builder);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context){
        CompoundNBT tag = context.getItem().getTag();
        if(tag == null || !tag.contains("controllerDim")){
            PlayerEntity player = context.getPlayer();
            if(player != null && !context.getPlayer().world.isRemote)
                context.getPlayer().sendMessage(new TranslationTextComponent("block.movingelevators.button_block.place").mergeStyle(TextFormatting.RED), player.getUniqueID());
            return ActionResultType.FAIL;
        }
        if(!tag.getString("controllerDim").equals(context.getWorld().func_234923_W_().func_240901_a_().toString())){
            PlayerEntity player = context.getPlayer();
            if(player != null && !context.getPlayer().world.isRemote)
                context.getPlayer().sendMessage(new TranslationTextComponent("block.movingelevators.button_block.dimension").mergeStyle(TextFormatting.RED), player.getUniqueID());
            return ActionResultType.FAIL;
        }
        return super.onItemUse(context);
    }
}
