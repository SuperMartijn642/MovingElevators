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
public class ElevatorGroupsPacket implements IMessage, IMessageHandler<ElevatorGroupsPacket,IMessage> {

    private NBTTagCompound capabilityData;

    public ElevatorGroupsPacket(NBTTagCompound groupData){
        this.capabilityData = groupData;
    }

    public ElevatorGroupsPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        ByteBufUtils.writeTag(buffer, this.capabilityData);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.capabilityData = ByteBufUtils.readTag(buffer);
    }

    @Override
    public IMessage onMessage(ElevatorGroupsPacket message, MessageContext ctx){
        ClientProxy.queTask(() -> {
            World world = ClientProxy.getPlayer().world;
            if(world == null)
                return;
            ElevatorGroupCapability groups = world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
            if(groups == null)
                return;
            ClientProxy.queTask(() -> groups.read(message.capabilityData));
        });
        return null;
    }

}
