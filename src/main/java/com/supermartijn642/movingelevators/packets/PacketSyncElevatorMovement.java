package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class PacketSyncElevatorMovement implements BasePacket {

    private int x, z;
    private EnumFacing facing;
    private double currentY;
    private double speed;

    public PacketSyncElevatorMovement(int x, int z, EnumFacing facing, double currentY, double speed){
        this.x = x;
        this.z = z;
        this.facing = facing;
        this.currentY = currentY;
        this.speed = speed;
    }

    public PacketSyncElevatorMovement(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeInt(this.x);
        buffer.writeInt(this.z);
        buffer.writeInt(this.facing.getHorizontalIndex());
        buffer.writeDouble(this.currentY);
        buffer.writeDouble(this.speed);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.x = buffer.readInt();
        this.z = buffer.readInt();
        this.facing = EnumFacing.getHorizontal(buffer.readInt());
        this.currentY = buffer.readDouble();
        this.speed = buffer.readDouble();
    }

    @Override
    public void handle(PacketContext context){
        World level = ClientUtils.getWorld();
        if(level == null)
            return;
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(level);
        if(groups == null)
            return;
        ElevatorGroup group = groups.get(this.x, this.z, this.facing);
        if(group == null)
            return;
        group.updateCurrentY(this.currentY, this.speed);
    }
}
