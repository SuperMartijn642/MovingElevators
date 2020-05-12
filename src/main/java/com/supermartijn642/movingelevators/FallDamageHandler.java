package com.supermartijn642.movingelevators;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created 4/30/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(modid = MovingElevators.MODID)
public class FallDamageHandler {

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent e){
        NBTTagCompound compound = e.getEntityLiving().getEntityData();
        if(compound.hasKey("elevatorTime")){
            if(compound.getLong("elevatorTime") - e.getEntity().ticksExisted > 20 * 5)
                e.setCanceled(true);
            else
                compound.removeTag("elevatorTime");
        }
    }

}
