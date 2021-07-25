package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        if(worldIn == null || pos == null || placer == null || stack.isEmpty())
            return;
        TileEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof ButtonBlockTile){
            CompoundNBT compound = stack.getTag();
            if(compound == null || !compound.contains("controllerDim"))
                return;
            ((ButtonBlockTile)tile).setValues(placer.getDirection().getOpposite(), new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ")));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        CompoundNBT tag = stack.getTag();
        if(tag != null && tag.contains("controllerDim"))
            tooltip.add(new StringTextComponent(ClientProxy.translate("block.movingelevators.button_block.info").replace("$x$", Integer.toString(tag.getInt("controllerX")))
                .replace("$y$", Integer.toString(tag.getInt("controllerY"))).replace("$z$", Integer.toString(tag.getInt("controllerZ")))).withStyle(TextFormatting.AQUA));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
