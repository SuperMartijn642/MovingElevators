package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.network.BlockEntityBasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class PacketElevatorSpeed extends BlockEntityBasePacket<ControllerBlockEntity> {

    public double speed;

    public PacketElevatorSpeed(BlockPos pos, double speed){
        super(pos);
        this.speed = speed;
    }

    public PacketElevatorSpeed(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        super.write(buffer);
        buffer.writeDouble(this.speed);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        super.read(buffer);
        this.speed = buffer.readDouble();
    }

    @Override
    public boolean verify(PacketContext context){
        return this.speed >= 0.1 && this.speed <= 1;
    }

    @Override
    protected void handle(ControllerBlockEntity blockEntity, PacketContext context){
        blockEntity.getGroup().setTargetSpeed(this.speed);
    }
}
