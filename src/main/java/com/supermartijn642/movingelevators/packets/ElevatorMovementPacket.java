package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorGroup;
import com.supermartijn642.movingelevators.ElevatorGroupCapability;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class ElevatorMovementPacket implements IMessage, IMessageHandler<ElevatorMovementPacket,IMessage> {

    private int x, z;
    private EnumFacing facing;
    private double currentY;

    public ElevatorMovementPacket(int x, int z, EnumFacing facing, double currentY){
        this.x = x;
        this.z = z;
        this.facing = facing;
        this.currentY = currentY;
    }

    public ElevatorMovementPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeInt(this.x);
        buffer.writeInt(this.z);
        buffer.writeInt(this.facing.getHorizontalIndex());
        buffer.writeDouble(this.currentY);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.x = buffer.readInt();
        this.z = buffer.readInt();
        this.facing = EnumFacing.getHorizontal(buffer.readInt());
        this.currentY = buffer.readDouble();
    }

    @Override
    public IMessage onMessage(ElevatorMovementPacket message, MessageContext ctx){
        World world = ClientProxy.getPlayer().world;
        if(world == null)
            return null;
        ElevatorGroupCapability groups = world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
        if(groups == null)
            return null;
        ElevatorGroup group = groups.get(message.x, message.z, message.facing);
        if(group == null)
            return null;
        ClientProxy.queTask(() -> group.updateCurrentY(message.currentY));
        return null;
    }
}
