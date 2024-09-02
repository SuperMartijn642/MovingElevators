package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import java.io.IOException;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class PacketUpdateElevatorGroups implements BasePacket {

    private NBTTagCompound capabilityData;

    public PacketUpdateElevatorGroups(NBTTagCompound capabilityData){
        this.capabilityData = capabilityData;
    }

    public PacketUpdateElevatorGroups(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeCompoundTag(this.capabilityData);
    }

    @Override
    public void read(PacketBuffer buffer){
        try{
            this.capabilityData = buffer.readCompoundTag();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void handle(PacketContext context){
        World level = ClientUtils.getWorld();
        if(level == null)
            return;
        ElevatorGroupCapability.get(level).read(this.capabilityData);
    }
}
