package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
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
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        if(worldIn == null || pos == null || placer == null || stack.isEmpty())
            return;
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof ButtonBlockTile){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.hasKey("controllerDim"))
                return;
            ((ButtonBlockTile)tile).setValues(placer.getHorizontalFacing().getOpposite(), new BlockPos(compound.getInteger("controllerX"), compound.getInteger("controllerY"), compound.getInteger("controllerZ")));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced){
        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null && tag.hasKey("controllerDim"))
            tooltip.add(TextFormatting.AQUA + ClientProxy.translate("block.movingelevators.button_block.info").replace("$x$", Integer.toString(tag.getInteger("controllerX")))
                .replace("$y$", Integer.toString(tag.getInteger("controllerY"))).replace("$z$", Integer.toString(tag.getInteger("controllerZ"))));
        super.addInformation(stack, player, tooltip, advanced);
    }
}
