package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.FallDamageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class PacketOnElevator implements IMessage, IMessageHandler<PacketOnElevator,IMessage> {

    public PacketOnElevator(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
    }

    @Override
    public void toBytes(ByteBuf buf){
    }

    @Override
    public IMessage onMessage(PacketOnElevator message, MessageContext ctx){
        EntityPlayer player = ctx.getServerHandler().player;
        if(player == null)
            return null;
        player.getServer().addScheduledTask(() -> FallDamageHandler.resetElevatorTime(player));
        return null;
    }

}
