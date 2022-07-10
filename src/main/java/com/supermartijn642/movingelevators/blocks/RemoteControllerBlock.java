package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlock extends ElevatorInputBlock {

    public RemoteControllerBlock(String registryName, Properties properties){
        super(registryName, properties, RemoteControllerBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(IBlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, facing, hitX, hitY, hitZ))
            return true;

        if(blockEntity instanceof RemoteControllerBlockEntity){
            if(worldIn.isRemote){
                BlockPos controllerPos = ((RemoteControllerBlockEntity)blockEntity).getControllerPos();
                ITextComponent x = TextComponents.number(controllerPos.getX()).color(TextFormatting.GOLD).get();
                ITextComponent y = TextComponents.number(controllerPos.getY()).color(TextFormatting.GOLD).get();
                ITextComponent z = TextComponents.number(controllerPos.getZ()).color(TextFormatting.GOLD).get();
                player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.controller_location", x, y, z).get(), true);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof RemoteControllerBlockEntity){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.hasKey("controllerDim"))
                return;
            ((RemoteControllerBlockEntity)tile).setValues(
                placer.getHorizontalFacing().getOpposite(),
                new BlockPos(compound.getInteger("controllerX"), compound.getInteger("controllerY"), compound.getInteger("controllerZ")),
                compound.hasKey("controllerFacing", Constants.NBT.TAG_INT) ? EnumFacing.getHorizontal(compound.getInteger("controllerFacing")) : null
            );
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World reader, List<String> tooltips, ITooltipFlag advanced){
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null || !tag.hasKey("controllerDim"))
            tooltips.add(TextComponents.translation("movingelevators.remote_controller.tooltip").color(TextFormatting.AQUA).format());
        else{
            ITextComponent x = TextComponents.number(tag.getInteger("controllerX")).color(TextFormatting.GOLD).get();
            ITextComponent y = TextComponents.number(tag.getInteger("controllerY")).color(TextFormatting.GOLD).get();
            ITextComponent z = TextComponents.number(tag.getInteger("controllerZ")).color(TextFormatting.GOLD).get();
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInteger("controllerDim"))).color(TextFormatting.GOLD).get();
            tooltips.add(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).format());
        }
        super.addInformation(stack, reader, tooltips, advanced);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof RemoteControllerBlockEntity)
            ((RemoteControllerBlockEntity)tile).onBreak();
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state){
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos){
        TileEntity entity = world.getTileEntity(pos);
        if(entity instanceof RemoteControllerBlockEntity){
            entity = ((RemoteControllerBlockEntity)entity).getController();
            if(entity != null
                && ((ControllerBlockEntity)entity).hasGroup()
                && ((ControllerBlockEntity)entity).getGroup().isCageAvailableAt((ControllerBlockEntity)entity)){
                return 15;
            }
        }
        return 0;
    }
}
