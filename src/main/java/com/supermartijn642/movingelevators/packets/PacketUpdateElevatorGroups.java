package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class PacketUpdateElevatorGroups implements BasePacket {

    private CompoundNBT capabilityData;

    public PacketUpdateElevatorGroups(CompoundNBT capabilityData){
        this.capabilityData = capabilityData;
    }

    public PacketUpdateElevatorGroups(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeNbt(this.capabilityData);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.capabilityData = buffer.readNbt();
    }

    @Override
    public void handle(PacketContext context){
        World level = ClientUtils.getWorld();
        if(level == null)
            return;
        ElevatorGroupCapability.get(level).read(this.capabilityData);
    }
}
