package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.core.network.TileEntityBasePacket;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

/**
 * Created 4/21/2020 by SuperMartijn642
 */
public class PacketSetFloorName extends TileEntityBasePacket<ControllerBlockEntity> {

    public String name;

    public PacketSetFloorName(BlockPos pos, String name){
        super(pos);
        this.name = name;
    }

    public PacketSetFloorName(){
    }

    @Override
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeBoolean(this.name == null);
        if(this.name != null)
            buffer.writeUtf(this.name);
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        this.name = buffer.readBoolean() ? null : buffer.readUtf(32767);
    }

    @Override
    public boolean verify(PacketContext context){
        return this.name == null || this.name.length() <= ElevatorScreen.MAX_NAME_LENGTH;
    }

    @Override
    protected void handle(ControllerBlockEntity blockEntity, PacketContext context){
        blockEntity.setFloorName(this.name == null || this.name.trim().isEmpty() ? null : this.name);
    }
}
