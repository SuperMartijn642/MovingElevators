package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.movingelevators.elevator.ElevatorFallDamageHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * Created 8/13/2020 by SuperMartijn642
 */
public class PacketOnElevator implements BasePacket {

    public PacketOnElevator(){
    }

    @Override
    public void write(PacketBuffer buffer){
    }

    @Override
    public void read(PacketBuffer buffer){
    }

    @Override
    public void handle(PacketContext context){
        EntityPlayer player = context.getSendingPlayer();
        if(player == null)
            return;
        Entity vehicle = player.getRidingEntity();
        if(vehicle instanceof EntityLivingBase)
            ElevatorFallDamageHandler.resetElevatorTime((EntityLivingBase)vehicle);
        else
            ElevatorFallDamageHandler.resetElevatorTime(player);
    }
}
