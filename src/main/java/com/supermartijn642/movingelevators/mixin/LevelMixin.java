package com.supermartijn642.movingelevators.mixin;

import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import com.supermartijn642.movingelevators.extensions.MovingElevatorsLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Created 08/03/2023 by SuperMartijn642
 */
@Mixin(Level.class)
public class LevelMixin implements MovingElevatorsLevel {

    private ElevatorGroupCapability movingelevatorsElevatorGroupCapability;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void constructor(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, CallbackInfo ci){
        //noinspection DataFlowIssue
        Level level = (Level)(Object)this;
        this.movingelevatorsElevatorGroupCapability = new ElevatorGroupCapability(level);
    }

    @Override
    public ElevatorGroupCapability getElevatorGroupCapability(){
        return this.movingelevatorsElevatorGroupCapability;
    }
}
