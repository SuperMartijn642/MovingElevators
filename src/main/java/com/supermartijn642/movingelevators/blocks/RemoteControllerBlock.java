package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BlockProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlock extends ElevatorInputBlock {

    public RemoteControllerBlock(BlockProperties properties){
        super(properties, RemoteControllerBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(IBlockState state, World level, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        if(super.onRightClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation))
            return true;

        if(blockEntity instanceof RemoteControllerBlockEntity){
            if(level.isRemote){
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
    public void onBlockPlacedBy(World level, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof RemoteControllerBlockEntity){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.hasKey("controllerDim"))
                return;
            ((RemoteControllerBlockEntity)entity).setValues(
                placer.getHorizontalFacing().getOpposite(),
                new BlockPos(compound.getInteger("controllerX"), compound.getInteger("controllerY"), compound.getInteger("controllerZ")),
                compound.hasKey("controllerFacing", Constants.NBT.TAG_INT) ? EnumFacing.getHorizontal(compound.getInteger("controllerFacing")) : null
            );
        }
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null || !tag.hasKey("controllerDim"))
            info.accept(TextComponents.translation("movingelevators.remote_controller.tooltip").color(TextFormatting.AQUA).get());
        else{
            ITextComponent x = TextComponents.number(tag.getInteger("controllerX")).color(TextFormatting.GOLD).get();
            ITextComponent y = TextComponents.number(tag.getInteger("controllerY")).color(TextFormatting.GOLD).get();
            ITextComponent z = TextComponents.number(tag.getInteger("controllerZ")).color(TextFormatting.GOLD).get();
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInteger("controllerDim"))).color(TextFormatting.GOLD).get();
            info.accept(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).get());
        }
    }

    @Override
    public void breakBlock(World level, BlockPos pos, IBlockState state){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof RemoteControllerBlockEntity)
            ((RemoteControllerBlockEntity)entity).onBreak();
        super.breakBlock(level, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state){
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World level, BlockPos pos){
        TileEntity entity = level.getTileEntity(pos);
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
