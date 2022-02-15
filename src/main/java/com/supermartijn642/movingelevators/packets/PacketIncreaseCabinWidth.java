package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.core.network.TileEntityBasePacket;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import net.minecraft.core.BlockPos;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class PacketIncreaseCabinWidth extends TileEntityBasePacket<ControllerBlockEntity> {

    public PacketIncreaseCabinWidth(BlockPos pos){
        super(pos);
    }

    public PacketIncreaseCabinWidth(){
    }

    @Override
    protected void handle(ControllerBlockEntity elevatorTile, PacketContext packetContext){
        elevatorTile.getGroup().increaseCageWidth();
    }
}
