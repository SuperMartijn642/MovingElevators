package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class PacketElevatorSpeed {

    public BlockPos pos;
    public double speed;

    public PacketElevatorSpeed(BlockPos pos, double speed){
        this.pos = pos;
        this.speed = speed;
    }

    public void encode(PacketBuffer buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeDouble(this.speed);
    }

    public static PacketElevatorSpeed decode(PacketBuffer buffer){
        return new PacketElevatorSpeed(buffer.readBlockPos(), buffer.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        PlayerEntity player = context.getSender();
        if(player == null)
            return;
        World world = player.world;
        if(world == null)
            return;
        TileEntity tile = world.getTileEntity(this.pos);
        if(!(tile instanceof ElevatorBlockTile))
            return;
        context.enqueueWork(() -> ((ElevatorBlockTile)tile).getGroup().setSpeed(this.speed));
    }

}
