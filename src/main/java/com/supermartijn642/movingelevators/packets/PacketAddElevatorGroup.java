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
public class PacketAddElevatorGroup implements BasePacket {

    private CompoundNBT groupData;

    public PacketAddElevatorGroup(CompoundNBT groupData){
        this.groupData = groupData;
    }

    public PacketAddElevatorGroup(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeNbt(this.groupData);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.groupData = buffer.readNbt();
    }

    @Override
    public void handle(PacketContext context){
        World world = ClientUtils.getWorld();
        if(world == null)
            return;
        world.getCapability(ElevatorGroupCapability.CAPABILITY).ifPresent(groups -> groups.readGroup(this.groupData));
    }
}
