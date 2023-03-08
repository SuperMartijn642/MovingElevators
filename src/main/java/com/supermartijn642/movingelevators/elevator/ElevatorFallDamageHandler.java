package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.movingelevators.extensions.MovingElevatorsLivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Created 4/30/2020 by SuperMartijn642
 */
public class ElevatorFallDamageHandler {

    public static boolean onFallDamage(LivingEntity entity){
        int elevatorTime = ((MovingElevatorsLivingEntity)entity).movingelevatorsGetElevatorTime();
        if(elevatorTime >= 0){
            if(entity.tickCount - elevatorTime < 20 * 5)
                return true;
            else
                ((MovingElevatorsLivingEntity)entity).movingelevatorsSetElevatorTime(-1);
        }
        return false;
    }

    public static void resetElevatorTime(Player player){
        ((MovingElevatorsLivingEntity)player).movingelevatorsSetElevatorTime(player.tickCount);
        if(player instanceof ServerPlayer)
            resetFloatingTicks((ServerPlayer)player);
    }

    public static void resetFloatingTicks(ServerPlayer player){
        player.connection.aboveGroundTickCount = 0;
    }
}
