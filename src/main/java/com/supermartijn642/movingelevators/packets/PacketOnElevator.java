package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.FallDamageHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 8/13/2020 by SuperMartijn642
 */
public class PacketOnElevator {

    public PacketOnElevator(){
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        PlayerEntity player = context.getSender();
        if(player == null)
            return;
        contextSupplier.get().enqueueWork(() -> FallDamageHandler.resetElevatorTime(player));
    }

}
