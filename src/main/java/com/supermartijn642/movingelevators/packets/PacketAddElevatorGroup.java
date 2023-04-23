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
public class PacketAddElevatorGroup implements BasePacket {

    private NBTTagCompound groupData;

    public PacketAddElevatorGroup(NBTTagCompound groupData){
        this.groupData = groupData;
    }

    public PacketAddElevatorGroup(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeCompoundTag(this.groupData);
    }

    @Override
    public void read(PacketBuffer buffer){
        try{
            this.groupData = buffer.readCompoundTag();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void handle(PacketContext context){
        World level = ClientUtils.getWorld();
        if(level == null)
            return;
        ElevatorGroupCapability.get(level).readGroup(this.groupData);
    }
}
