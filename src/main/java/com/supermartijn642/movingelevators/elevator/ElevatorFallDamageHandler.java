package com.supermartijn642.movingelevators.elevator;

import net.minecraft.entity.player.EntityPlayer;
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
        NBTTagCompound compound = e.getEntityLiving().getEntityData();
        if(compound.hasKey("elevatorTime")){
            if(e.getEntity().ticksExisted - compound.getLong("elevatorTime") < 20 * 5)
                e.setCanceled(true);
            else
                compound.removeTag("elevatorTime");
        }
    }

    public static void resetElevatorTime(EntityPlayer player){
        player.getEntityData().setLong("elevatorTime", player.ticksExisted);
        if(player instanceof EntityPlayerMP)
            resetFloatingTicks((EntityPlayerMP)player);
    }

    public static void resetFloatingTicks(EntityPlayerMP player){
        try{
            floatingTickCount.setInt(player.connection, 0);
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
    }
}
