package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorGroupCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class ElevatorGroupPacket {

    private CompoundTag groupData;

    public ElevatorGroupPacket(CompoundTag groupData){
        this.groupData = groupData;
    }

    public ElevatorGroupPacket(FriendlyByteBuf buffer){
        this.decode(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeNbt(this.groupData);
    }

    public void decode(FriendlyByteBuf buffer){
        this.groupData = buffer.readNbt();
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
        context.enqueueWork(() -> groups.readGroup(this.groupData));
    }

}
