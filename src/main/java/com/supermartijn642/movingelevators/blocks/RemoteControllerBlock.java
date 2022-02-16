package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

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
    protected boolean onRightClick(BlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult rayTraceResult){
        if(super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, rayTraceResult))
            return true;

        if(blockEntity instanceof RemoteControllerBlockEntity){
            if(worldIn.isClientSide){
                BlockPos controllerPos = ((RemoteControllerBlockEntity)blockEntity).getControllerPos();
                ITextComponent x = TextComponents.number(controllerPos.getX()).color(TextFormatting.GOLD).get();
                ITextComponent y = TextComponents.number(controllerPos.getY()).color(TextFormatting.GOLD).get();
                ITextComponent z = TextComponents.number(controllerPos.getZ()).color(TextFormatting.GOLD).get();
                player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.controller_location", x, y, z).get(), true);
            }
            return true;
        }
        return false;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        TileEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof RemoteControllerBlockEntity){
            CompoundNBT compound = stack.getTag();
            if(compound == null || !compound.contains("controllerDim"))
                return;
            ((RemoteControllerBlockEntity)tile).setValues(placer.getDirection().getOpposite(), new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ")));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        CompoundNBT tag = stack.getTag();
        if(tag == null || !tag.contains("controllerDim"))
            tooltip.add(TextComponents.translation("movingelevators.remote_controller.tooltip").color(TextFormatting.AQUA).get());
        else{
            ITextComponent x = TextComponents.number(tag.getInt("controllerX")).color(TextFormatting.GOLD).get();
            ITextComponent y = TextComponents.number(tag.getInt("controllerY")).color(TextFormatting.GOLD).get();
            ITextComponent z = TextComponents.number(tag.getInt("controllerZ")).color(TextFormatting.GOLD).get();
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInt("controllerDim"))).color(TextFormatting.GOLD).get();
            tooltip.add(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).get());
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos){
        TileEntity entity = world.getBlockEntity(pos);
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
