package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlockItem extends BlockItem {

    public RemoteControllerBlockItem(Block blockIn, Properties builder){
        super(blockIn, builder);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand){
        ItemStack stack = player.getItemInHand(hand);
        if(stack.getItem() == this){
            if(player.isShiftKeyDown()){
                if(stack.hasTag() && stack.getTag().contains("controllerDim")){
                    if(!world.isClientSide){
                        stack.removeTagKey("controllerDim");
                        stack.removeTagKey("controllerX");
                        stack.removeTagKey("controllerY");
                        stack.removeTagKey("controllerZ");
                        player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.clear").get(), true);
                    }
                    return ActionResult.sidedSuccess(stack, world.isClientSide);
                }
            }else{
                if(!world.isClientSide){
                    if(stack.hasTag() && stack.getTag().contains("controllerDim")){
                        CompoundNBT compound = stack.getTag();
                        ITextComponent x = TextComponents.number(compound.getInt("controllerX")).color(TextFormatting.GOLD).get();
                        ITextComponent y = TextComponents.number(compound.getInt("controllerY")).color(TextFormatting.GOLD).get();
                        ITextComponent z = TextComponents.number(compound.getInt("controllerZ")).color(TextFormatting.GOLD).get();
                        ITextComponent dimension = TextComponents.dimension(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("controllerDim")))).color(TextFormatting.GOLD).get();
                        player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).get(), true);
                    }else
                        player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.tooltip").get(), true);
                }
                return ActionResult.sidedSuccess(stack, world.isClientSide);
            }
        }
        return super.use(world, player, hand);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context){
        CompoundNBT tag = context.getItemInHand().getTag();
        if(tag == null || !tag.contains("controllerDim")){
            PlayerEntity player = context.getPlayer();
            if(player != null && !context.getPlayer().level.isClientSide)
                context.getPlayer().displayClientMessage(TextComponents.translation("movingelevators.remote_controller.not_bound").color(TextFormatting.RED).get(), true);
            return ActionResultType.FAIL;
        }
        if(!tag.getString("controllerDim").equals(context.getLevel().dimension().location().toString())){
            PlayerEntity player = context.getPlayer();
            if(player != null && !context.getPlayer().level.isClientSide)
                context.getPlayer().displayClientMessage(TextComponents.translation("movingelevators.remote_controller.wrong_dimension").color(TextFormatting.RED).get(), true);
            return ActionResultType.FAIL;
        }
        return super.useOn(context);
    }
}
