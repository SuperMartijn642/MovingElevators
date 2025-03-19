package com.supermartijn642.movingelevators.elevator;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

/**
 * Created 4/30/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class ElevatorFallDamageHandler {

    private static final Field floatingTickCount = ObfuscationReflectionHelper.findField(NetHandlerPlayServer.class, "field_147365_f");

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent e){
        if(shouldCancelFallDamage(e.getEntityLiving()))
            e.setCanceled(true);
    }

    public static boolean shouldCancelFallDamage(EntityLivingBase entity){
        NBTTagCompound compound = entity.getEntityData();
        if(compound.hasKey("elevatorTime")){
            if(entity.ticksExisted - compound.getLong("elevatorTime") < 20 * 5)
                return true;
            else
                compound.removeTag("elevatorTime");
        }
        return false;
    }

    public static void resetElevatorTime(EntityLivingBase entity){
        entity.getEntityData().setLong("elevatorTime", entity.ticksExisted);
        if(entity instanceof EntityPlayerMP)
            resetFloatingTicks((EntityPlayerMP)entity);
    }

    public static void resetFloatingTicks(EntityPlayerMP player){
        try{
            floatingTickCount.setInt(player.connection, 0);
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
    }
}
