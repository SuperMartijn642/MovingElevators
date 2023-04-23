package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

/**
 * Created 8/8/2021 by SuperMartijn642
 */
public class PacketRemoveElevatorGroup implements BasePacket {

    private int x, z;
    private Direction facing;

    public PacketRemoveElevatorGroup(ElevatorGroup group){
        this.x = group.x;
        this.z = group.z;
        this.facing = group.facing;
    }

    public PacketRemoveElevatorGroup(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeInt(this.x);
        buffer.writeInt(this.z);
        buffer.writeInt(this.facing.get2DDataValue());
    }

    @Override
    public void read(PacketBuffer buffer){
        this.x = buffer.readInt();
        this.z = buffer.readInt();
        this.facing = Direction.from2DDataValue(buffer.readInt());
    }

    @Override
    public void handle(PacketContext context){
        World world = ClientUtils.getWorld();
        if(world == null)
            return;
        ElevatorGroupCapability.get(world).removeGroup(this.x, this.z, this.facing);
    }
}
