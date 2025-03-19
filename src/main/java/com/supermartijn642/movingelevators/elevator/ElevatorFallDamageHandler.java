package com.supermartijn642.movingelevators.elevator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created 4/30/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElevatorFallDamageHandler {

    private static final Field floatingTickCount = ObfuscationReflectionHelper.findField(ServerGamePacketListenerImpl.class, "f_9737_");

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
        try{
            floatingTickCount.setInt(player.connection, 0);
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
    }
}
