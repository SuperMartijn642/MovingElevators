package com.supermartijn642.movingelevators;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ButtonBlockItem extends BlockItem {

    public ButtonBlockItem(Block blockIn, Properties builder){
        super(blockIn, builder);
    }

    @Override
    public InteractionResult useOn(UseOnContext context){
        CompoundTag tag = context.getItemInHand().getTag();
        if(tag == null || !tag.contains("controllerDim")){
            Player player = context.getPlayer();
            if(player != null && !context.getPlayer().level.isClientSide)
                context.getPlayer().sendMessage(new TranslatableComponent("block.movingelevators.button_block.place").withStyle(ChatFormatting.RED), player.getUUID());
            return InteractionResult.FAIL;
        }
        if(!tag.getString("controllerDim").equals(context.getLevel().dimension().getRegistryName().toString())){
            Player player = context.getPlayer();
            if(player != null && !context.getPlayer().level.isClientSide)
                context.getPlayer().sendMessage(new TranslatableComponent("block.movingelevators.button_block.dimension").withStyle(ChatFormatting.RED), player.getUUID());
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }
}
