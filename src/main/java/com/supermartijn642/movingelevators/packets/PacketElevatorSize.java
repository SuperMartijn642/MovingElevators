package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class PacketElevatorSize implements IMessage, IMessageHandler<PacketElevatorSize,IMessage> {

    public BlockPos pos;
    public int size;

    public PacketElevatorSize(BlockPos pos, int size){
        this.pos = pos;
        this.size = size;
    }

    public PacketElevatorSize(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.size = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
        buf.writeInt(this.size);
    }

    @Override
    public IMessage onMessage(PacketElevatorSize message, MessageContext ctx){
        EntityPlayer player = ctx.getServerHandler().player;
        if(player == null)
            return null;
        World world = player.world;
        if(world == null)
            return null;
        TileEntity tile = world.getTileEntity(message.pos);
        if(!(tile instanceof ElevatorBlockTile))
            return null;
        player.getServer().addScheduledTask(() -> ((ElevatorBlockTile)tile).getGroup().setSize(message.size));
        return null;
    }
}
