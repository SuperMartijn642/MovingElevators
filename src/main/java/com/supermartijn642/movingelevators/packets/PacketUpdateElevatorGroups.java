package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class PacketUpdateElevatorGroups implements BasePacket {

    private CompoundTag capabilityData;

    public PacketUpdateElevatorGroups(CompoundTag capabilityData){
        this.capabilityData = capabilityData;
    }

    public PacketUpdateElevatorGroups(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeNbt(this.capabilityData);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.capabilityData = buffer.readNbt();
    }

    @Override
    public void handle(PacketContext context){
        Level world = ClientUtils.getWorld();
        if(world == null)
            return;
        world.getCapability(ElevatorGroupCapability.CAPABILITY).ifPresent(groups -> groups.read(this.capabilityData));
    }
}
