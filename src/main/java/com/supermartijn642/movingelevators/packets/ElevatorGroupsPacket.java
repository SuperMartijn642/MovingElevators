package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorGroupCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class ElevatorGroupsPacket {

    private CompoundNBT capabilityData;

    public ElevatorGroupsPacket(CompoundNBT capabilityData){
        this.capabilityData = capabilityData;
    }

    public ElevatorGroupsPacket(PacketBuffer buffer){
        this.decode(buffer);
    }

    public void encode(PacketBuffer buffer){
        buffer.writeNbt(this.capabilityData);
    }

    public void decode(PacketBuffer buffer){
        this.capabilityData = buffer.readNbt();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        World world = ClientProxy.getPlayer().level;
        if(world == null)
            return;
        ElevatorGroupCapability groups = world.getCapability(ElevatorGroupCapability.CAPABILITY).orElse(null);
        if(groups == null)
            return;
        context.enqueueWork(() -> groups.read(this.capabilityData));
    }

}
