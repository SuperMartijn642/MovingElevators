package com.supermartijn642.movingelevators.blocks;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BlockProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlock extends ElevatorInputBlock {

    public static final DataComponentType<Target> TARGET = DataComponentType.<Target>builder()
        .persistent(RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(Target::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(Target::pos),
            Direction.CODEC.fieldOf("facing").forGetter(Target::facing)
        ).apply(instance, Target::new)))
        .networkSynchronized(StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, Target::dimension,
            BlockPos.STREAM_CODEC, Target::pos,
            Direction.STREAM_CODEC, Target::facing,
            Target::new
        )).build();

    public RemoteControllerBlock(BlockProperties properties){
        super(properties, RemoteControllerBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(BlockState state, Level level, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(super.onRightClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation))
            return true;

        if(blockEntity instanceof RemoteControllerBlockEntity){
            if(level.isClientSide){
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof RemoteControllerBlockEntity){
            Target target = stack.get(TARGET);
            if(target == null)
                return;
            ((RemoteControllerBlockEntity)entity).setValues(
                placer == null ? Direction.NORTH : placer.getDirection().getOpposite(),
                target.pos,
                target.facing
            );
        }
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        Target target = stack.get(TARGET);
        if(target == null)
            info.accept(TextComponents.translation("movingelevators.remote_controller.tooltip").color(ChatFormatting.AQUA).get());
        else{
            Component x = TextComponents.number(target.pos.getX()).color(ChatFormatting.GOLD).get();
            Component y = TextComponents.number(target.pos.getY()).color(ChatFormatting.GOLD).get();
            Component z = TextComponents.number(target.pos.getZ()).color(ChatFormatting.GOLD).get();
            Component dimension = TextComponents.dimension(ResourceKey.create(Registries.DIMENSION, target.dimension)).color(ChatFormatting.GOLD).get();
            info.accept(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", x, y, z, dimension).get());
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos){
        BlockEntity entity = level.getBlockEntity(pos);
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

    public record Target(ResourceLocation dimension, BlockPos pos, Direction facing) {
    }
}
