package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.FallDamageHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
        Player player = context.getSender();
        if(player == null)
            return;
        contextSupplier.get().enqueueWork(() -> FallDamageHandler.resetElevatorTime(player));
    }

}
