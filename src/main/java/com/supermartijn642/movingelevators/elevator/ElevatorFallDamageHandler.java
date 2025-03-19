package com.supermartijn642.movingelevators.elevator;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created 4/30/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElevatorFallDamageHandler {

    private static final Field floatingTickCount = ObfuscationReflectionHelper.findField(ServerPlayNetHandler.class, "field_147365_f");

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent e){
        if(shouldCancelFallDamage(e.getEntityLiving()))
            e.setCanceled(true);
    }

    public static boolean shouldCancelFallDamage(LivingEntity entity){
        CompoundNBT compound = entity.getPersistentData();
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
        if(entity instanceof ServerPlayerEntity)
            resetFloatingTicks((ServerPlayerEntity)entity);
    }

    public static void resetFloatingTicks(ServerPlayerEntity player){
        try{
            floatingTickCount.setInt(player.connection, 0);
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
    }
}
