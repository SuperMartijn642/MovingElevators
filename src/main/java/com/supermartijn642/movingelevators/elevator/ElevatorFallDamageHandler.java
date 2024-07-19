package com.supermartijn642.movingelevators.elevator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 4/30/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElevatorFallDamageHandler {

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent e){
        CompoundTag compound = e.getEntity().getPersistentData();
        if(compound.contains("elevatorTime")){
            if(e.getEntity().tickCount - compound.getLong("elevatorTime") < 20 * 5)
                e.setCanceled(true);
            else
                compound.remove("elevatorTime");
        }
    }

    public static void resetElevatorTime(Player player){
        player.getPersistentData().putLong("elevatorTime", player.tickCount);
        if(player instanceof ServerPlayer)
            resetFloatingTicks((ServerPlayer)player);
    }

    public static void resetFloatingTicks(ServerPlayer player){
        player.connection.aboveGroundTickCount = 0;
    }
}
