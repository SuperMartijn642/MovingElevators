package com.supermartijn642.movingelevators;

import net.minecraft.entity.player.PlayerEntity;
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
public class FallDamageHandler {

    public static final Field floatingTickCount = ObfuscationReflectionHelper.findField(ServerPlayNetHandler.class, "field_147365_f");

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent e){
        CompoundNBT compound = e.getEntityLiving().getPersistentData();
        if(compound.contains("elevatorTime")){
            if(e.getEntity().ticksExisted - compound.getLong("elevatorTime") < 20 * 5)
                e.setCanceled(true);
            else
                compound.remove("elevatorTime");
        }
    }

    public static void resetElevatorTime(PlayerEntity player){
        player.getPersistentData().putLong("elevatorTime", player.ticksExisted);
        if(player instanceof ServerPlayerEntity)
            resetFloatingTicks((ServerPlayerEntity)player);
    }

    public static void resetFloatingTicks(ServerPlayerEntity player){
        try{
            floatingTickCount.setInt(player.connection, 0);
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
    }

}
