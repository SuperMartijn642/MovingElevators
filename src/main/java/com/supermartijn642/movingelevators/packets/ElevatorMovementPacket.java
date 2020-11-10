package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorGroup;
import com.supermartijn642.movingelevators.ElevatorGroupCapability;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class ElevatorMovementPacket {

    private int x, z;
    private Direction facing;
    private double currentY;

    public ElevatorMovementPacket(int x, int z, Direction facing, double currentY){
        this.x = x;
        this.z = z;
        this.facing = facing;
        this.currentY = currentY;
    }

    public ElevatorMovementPacket(PacketBuffer buffer){
        this.decode(buffer);
    }

    public void encode(PacketBuffer buffer){
        buffer.writeInt(this.x);
        buffer.writeInt(this.z);
        buffer.writeInt(this.facing.getHorizontalIndex());
        buffer.writeDouble(this.currentY);
    }

    public void decode(PacketBuffer buffer){
        this.x = buffer.readInt();
        this.z = buffer.readInt();
        this.facing = Direction.byHorizontalIndex(buffer.readInt());
        this.currentY = buffer.readDouble();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        World world = ClientProxy.getPlayer().world;
        if(world == null)
            return;
        ElevatorGroupCapability groups = world.getCapability(ElevatorGroupCapability.CAPABILITY).orElse(null);
        if(groups == null)
            return;
        ElevatorGroup group = groups.get(this.x, this.z, this.facing);
        if(group == null)
            return;
        context.enqueueWork(() -> group.updateCurrentY(this.currentY));
    }
}
