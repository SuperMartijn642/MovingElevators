package com.supermartijn642.movingelevators.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.core.network.TileEntityBasePacket;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class PacketDecreaseCabinSideOffset extends TileEntityBasePacket<ControllerBlockEntity> {

    public PacketDecreaseCabinSideOffset(BlockPos pos){
        super(pos);
    }

    public PacketDecreaseCabinSideOffset(){
    }

    @Override
    protected void handle(ControllerBlockEntity elevatorTile, PacketContext packetContext){
        elevatorTile.getGroup().decreaseCageSideOffset();
    }
}
