package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorGroup;
import com.supermartijn642.movingelevators.ElevatorGroupCapability;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

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

    public ElevatorMovementPacket(FriendlyByteBuf buffer){
        this.decode(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeInt(this.x);
        buffer.writeInt(this.z);
        buffer.writeInt(this.facing.get2DDataValue());
        buffer.writeDouble(this.currentY);
    }

    public void decode(FriendlyByteBuf buffer){
        this.x = buffer.readInt();
        this.z = buffer.readInt();
        this.facing = Direction.from2DDataValue(buffer.readInt());
        this.currentY = buffer.readDouble();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        Level world = ClientProxy.getPlayer().level;
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
