package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputBlock;
import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class CallButtonBlock extends ElevatorInputBlock {

    public CallButtonBlock(){
        super("call_button_block", CallButtonBlockTile::new);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        if(worldIn == null || pos == null || placer == null || stack.isEmpty())
            return;
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof CallButtonBlockTile){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.hasKey("controllerDim"))
                return;
            ((CallButtonBlockTile)tile).setValues(placer.getHorizontalFacing().getOpposite(), new BlockPos(compound.getInteger("controllerX"), compound.getInteger("controllerY"), compound.getInteger("controllerZ")));
        }
    }

    @Override
    protected void onRightClick(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote)
            return;

        TileEntity tile = worldIn.getTileEntity(pos);
        if(!(tile instanceof ElevatorInputTile))
            return;

        ElevatorInputTile inputTile = (ElevatorInputTile)tile;
        if(inputTile.getFacing() != facing || !inputTile.hasGroup())
            return;

        inputTile.getGroup().onButtonPress(false, false, inputTile.getFloorLevel());
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
