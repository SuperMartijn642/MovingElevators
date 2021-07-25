package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public void encode(PacketBuffer buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeBoolean(this.name == null);
        if(this.name != null)
            buffer.writeUtf(this.name);
    }

    public static PacketElevatorName decode(PacketBuffer buffer){
        return new PacketElevatorName(buffer.readBlockPos(), buffer.readBoolean() ? null : buffer.readUtf(32767));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        PlayerEntity player = context.getSender();
        if(player == null)
            return;
        World world = player.level;
        if(world == null)
            return;
        TileEntity tile = world.getBlockEntity(this.pos);
        if(!(tile instanceof ElevatorBlockTile))
            return;
        context.enqueueWork(() -> ((ElevatorBlockTile)tile).setFloorName(this.name));
    }
}
