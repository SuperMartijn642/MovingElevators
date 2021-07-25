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
    public ActionResultType useOn(ItemUseContext context){
        CompoundNBT tag = context.getItemInHand().getTag();
        if(tag == null || !tag.contains("controllerDim")){
            PlayerEntity player = context.getPlayer();
            if(player != null && !context.getPlayer().level.isClientSide)
                context.getPlayer().sendMessage(new TranslationTextComponent("block.movingelevators.button_block.place").withStyle(TextFormatting.RED), player.getUUID());
            return ActionResultType.FAIL;
        }
        if(!tag.getString("controllerDim").equals(context.getLevel().dimension().getRegistryName().toString())){
            PlayerEntity player = context.getPlayer();
            if(player != null && !context.getPlayer().level.isClientSide)
                context.getPlayer().sendMessage(new TranslationTextComponent("block.movingelevators.button_block.dimension").withStyle(TextFormatting.RED), player.getUUID());
            return ActionResultType.FAIL;
        }
        return super.useOn(context);
    }
}
