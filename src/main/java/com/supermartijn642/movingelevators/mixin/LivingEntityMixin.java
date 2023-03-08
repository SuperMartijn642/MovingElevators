package com.supermartijn642.movingelevators.mixin;

import com.supermartijn642.movingelevators.elevator.ElevatorFallDamageHandler;
import com.supermartijn642.movingelevators.extensions.MovingElevatorsLivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 08/03/2023 by SuperMartijn642
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin implements MovingElevatorsLivingEntity {

    private int movingelevatorsElevatorTime = -1;

    @Inject(
        method = "causeFallDamage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void causeFallDamage(float f, float g, DamageSource damageSource, CallbackInfoReturnable<Boolean> ci) {
        //noinspection DataFlowIssue
        LivingEntity entity = (LivingEntity)(Object)this;
        if(ElevatorFallDamageHandler.onFallDamage(entity))
            ci.setReturnValue(false);
    }

    @Override
    public int movingelevatorsGetElevatorTime(){
        return this.movingelevatorsElevatorTime;
    }

    @Override
    public void movingelevatorsSetElevatorTime(int time){
        this.movingelevatorsElevatorTime = time;
    }
}
