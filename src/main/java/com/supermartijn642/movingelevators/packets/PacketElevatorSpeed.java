package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class PacketElevatorSpeed {

    public BlockPos pos;
    public double speed;

    public PacketElevatorSpeed(BlockPos pos, double speed){
        this.pos = pos;
        this.speed = speed;
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeDouble(this.speed);
    }

    public static PacketElevatorSpeed decode(FriendlyByteBuf buffer){
        return new PacketElevatorSpeed(buffer.readBlockPos(), buffer.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        Player player = context.getSender();
        if(player == null)
            return;
        Level world = player.level;
        if(world == null)
            return;
        BlockEntity tile = world.getBlockEntity(this.pos);
        if(!(tile instanceof ElevatorBlockTile))
            return;
        context.enqueueWork(() -> ((ElevatorBlockTile)tile).getGroup().setSpeed(this.speed));
    }

}
