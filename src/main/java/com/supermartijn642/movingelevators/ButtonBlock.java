package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ButtonBlock extends ElevatorInputBlock {

    public ButtonBlock(){
        super("button_block", ButtonBlockTile::new);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        if(worldIn == null || pos == null || placer == null || stack.isEmpty())
            return;
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof ButtonBlockTile){
            CompoundTag compound = stack.getTag();
            if(compound == null || !compound.contains("controllerDim"))
                return;
            ((ButtonBlockTile)tile).setValues(placer.getDirection().getOpposite(), new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ")));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
        CompoundTag tag = stack.getTag();
        if(tag != null && tag.contains("controllerDim"))
            tooltip.add(new TextComponent(ClientProxy.translate("block.movingelevators.button_block.info").replace("$x$", Integer.toString(tag.getInt("controllerX")))
                .replace("$y$", Integer.toString(tag.getInt("controllerY"))).replace("$z$", Integer.toString(tag.getInt("controllerZ")))).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
