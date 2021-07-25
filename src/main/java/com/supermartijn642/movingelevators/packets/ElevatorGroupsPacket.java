package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorGroupCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class ElevatorGroupsPacket {

    private CompoundTag capabilityData;

    public ElevatorGroupsPacket(CompoundTag capabilityData){
        this.capabilityData = capabilityData;
    }

    public ElevatorGroupsPacket(FriendlyByteBuf buffer){
        this.decode(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeNbt(this.capabilityData);
    }

    public void decode(FriendlyByteBuf buffer){
        this.capabilityData = buffer.readNbt();
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
        context.enqueueWork(() -> groups.read(this.capabilityData));
    }

}
