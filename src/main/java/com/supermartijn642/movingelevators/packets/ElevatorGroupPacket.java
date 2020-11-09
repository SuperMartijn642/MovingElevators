package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorGroupCapability;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class ElevatorGroupPacket implements IMessage, IMessageHandler<ElevatorGroupPacket,IMessage> {

    private NBTTagCompound groupData;

    public ElevatorGroupPacket(NBTTagCompound groupData){
        this.groupData = groupData;
    }

    public ElevatorGroupPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        ByteBufUtils.writeTag(buffer, this.groupData);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.groupData = ByteBufUtils.readTag(buffer);
    }

    @Override
    public IMessage onMessage(ElevatorGroupPacket message, MessageContext ctx){
        World world = ClientProxy.getPlayer().world;
        if(world == null)
            return null;
        ElevatorGroupCapability groups = world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
        if(groups == null)
            return null;
        ClientProxy.queTask(() -> groups.readGroup(message.groupData));
        return null;
    }

}
