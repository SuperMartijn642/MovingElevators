package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 4/21/2020 by SuperMartijn642
 */
public class PacketElevatorName {

    public BlockPos pos;
    public String name;

    public PacketElevatorName(BlockPos pos, String name){
        this.pos = pos;
        this.name = name;
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeBoolean(this.name == null);
        if(this.name != null)
            buffer.writeUtf(this.name);
    }

    public static PacketElevatorName decode(FriendlyByteBuf buffer){
        return new PacketElevatorName(buffer.readBlockPos(), buffer.readBoolean() ? null : buffer.readUtf(32767));
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
        context.enqueueWork(() -> ((ElevatorBlockTile)tile).setFloorName(this.name));
    }
}
