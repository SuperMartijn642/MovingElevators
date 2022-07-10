package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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
    protected boolean onRightClick(BlockState state, Level worldIn, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
        if(super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, rayTraceResult))
            return true;

        if(blockEntity instanceof RemoteControllerBlockEntity){
            if(worldIn.isClientSide){
                BlockPos controllerPos = ((RemoteControllerBlockEntity)blockEntity).getControllerPos();
                Component x = TextComponents.number(controllerPos.getX()).color(ChatFormatting.GOLD).get();
                Component y = TextComponents.number(controllerPos.getY()).color(ChatFormatting.GOLD).get();
                Component z = TextComponents.number(controllerPos.getZ()).color(ChatFormatting.GOLD).get();
                player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.controller_location", x, y, z).get(), true);
            }
            return true;
        }
        return false;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof RemoteControllerBlockEntity){
            CompoundTag compound = stack.getTag();
            if(compound == null || !compound.contains("controllerDim"))
                return;
            ((RemoteControllerBlockEntity)tile).setValues(
                placer.getDirection().getOpposite(),
                new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ")),
                compound.contains("controllerFacing", Tag.TAG_INT) ? Direction.from2DDataValue(compound.getInt("controllerFacing")) : null
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
        CompoundTag tag = stack.getTag();
        if(tag == null || !tag.contains("controllerDim"))
            tooltip.add(TextComponents.translation("movingelevators.remote_controller.tooltip").color(ChatFormatting.AQUA).get());
        else{
            Component x = TextComponents.number(tag.getInt("controllerX")).color(ChatFormatting.GOLD).get();
            Component y = TextComponents.number(tag.getInt("controllerY")).color(ChatFormatting.GOLD).get();
            Component z = TextComponents.number(tag.getInt("controllerZ")).color(ChatFormatting.GOLD).get();
            Component dimension = TextComponents.dimension(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("controllerDim")))).color(ChatFormatting.GOLD).get();
            tooltip.add(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).get());
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos){
        BlockEntity entity = world.getBlockEntity(pos);
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
