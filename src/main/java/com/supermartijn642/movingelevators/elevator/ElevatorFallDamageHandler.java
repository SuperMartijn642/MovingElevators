package com.supermartijn642.movingelevators.elevator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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
        if(shouldCancelFallDamage(e.getEntity()))
            e.setCanceled(true);
    }

    public static boolean shouldCancelFallDamage(LivingEntity entity){
        CompoundTag compound = entity.getPersistentData();
        if(compound.contains("elevatorTime")){
            if(entity.tickCount - compound.getLong("elevatorTime") < 20 * 5)
                return true;
            else
                compound.remove("elevatorTime");
        }
        return false;
    }

    public static void resetElevatorTime(LivingEntity entity){
        entity.getPersistentData().putLong("elevatorTime", entity.tickCount);
        if(entity instanceof ServerPlayer)
            resetFloatingTicks((ServerPlayer)entity);
    }

    public static void resetFloatingTicks(ServerPlayer player){
        player.connection.aboveGroundTickCount = 0;
    }
}
