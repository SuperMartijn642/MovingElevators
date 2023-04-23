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
public class PacketAddElevatorGroup implements BasePacket {

    private CompoundTag groupData;

    public PacketAddElevatorGroup(CompoundTag groupData){
        this.groupData = groupData;
    }

    public PacketAddElevatorGroup(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeNbt(this.groupData);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.groupData = buffer.readNbt();
    }

    @Override
    public void handle(PacketContext context){
        Level world = ClientUtils.getWorld();
        if(world == null)
            return;
        ElevatorGroupCapability.get(world).readGroup(this.groupData);
    }
}
