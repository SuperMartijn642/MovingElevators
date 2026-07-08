package com.supermartijn642.movingelevators.elevator;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;

/**
 * Created 08/07/2026 by SuperMartijn642
 */
public class ClientElevatorCabinLevel extends ElevatorCabinLevel implements BlockAndTintGetter {

    protected ClientElevatorCabinLevel(ClientLevel clientLevel){
        super(clientLevel);
    }

    @Override
    public CardinalLighting cardinalLighting(){
        return CardinalLighting.DEFAULT;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color){
        return color.getColor(this.getBiome(pos).value(), pos.getX(), pos.getZ());
    }
}
