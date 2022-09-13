package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
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
    protected boolean onRightClick(BlockState state, World level, CamoBlockEntity blockEntity, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vector3d hitLocation){
        if(super.onRightClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation))
            return true;

        if(blockEntity instanceof RemoteControllerBlockEntity){
            if(level.isClientSide){
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
    public void setPlacedBy(World level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof RemoteControllerBlockEntity){
            CompoundNBT compound = stack.getTag();
            if(compound == null || !compound.contains("controllerDim"))
                return;
            ((RemoteControllerBlockEntity)entity).setValues(
                placer.getDirection().getOpposite(),
                new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ")),
                compound.contains("controllerFacing", Constants.NBT.TAG_INT) ? Direction.from2DDataValue(compound.getInt("controllerFacing")) : null
            );
        }
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        CompoundNBT tag = stack.getTag();
        if(tag == null || !tag.contains("controllerDim"))
            info.accept(TextComponents.translation("movingelevators.remote_controller.tooltip").color(TextFormatting.AQUA).get());
        else{
            ITextComponent x = TextComponents.number(tag.getInt("controllerX")).color(TextFormatting.GOLD).get();
            ITextComponent y = TextComponents.number(tag.getInt("controllerY")).color(TextFormatting.GOLD).get();
            ITextComponent z = TextComponents.number(tag.getInt("controllerZ")).color(TextFormatting.GOLD).get();
            ITextComponent dimension = TextComponents.dimension(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("controllerDim")))).color(TextFormatting.GOLD).get();
            info.accept(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).get());
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World level, BlockPos pos){
        TileEntity entity = level.getBlockEntity(pos);
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
