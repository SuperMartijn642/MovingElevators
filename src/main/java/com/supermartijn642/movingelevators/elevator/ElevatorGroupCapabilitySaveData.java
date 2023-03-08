package com.supermartijn642.movingelevators.elevator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Created 20/02/2023 by SuperMartijn642
 */
public class ElevatorGroupCapabilitySaveData extends SavedData {

    private static final String IDENTIFIER = "movingelevators_elevator_groups";

    private final ElevatorGroupCapability capability;

    public static void init(ServerLevel level, ElevatorGroupCapability capability){
        level.getDataStorage().computeIfAbsent(tag -> {
            ElevatorGroupCapabilitySaveData saveData = new ElevatorGroupCapabilitySaveData(capability);
            saveData.load(tag);
            return saveData;
        }, () -> new ElevatorGroupCapabilitySaveData(capability), IDENTIFIER);
    }

    public ElevatorGroupCapabilitySaveData(ElevatorGroupCapability capability){
        this.capability = capability;
    }

    @Override
    public CompoundTag save(CompoundTag tag){
        return this.capability.write();
    }

    public void load(CompoundTag tag){
        this.capability.read(tag);
    }

    @Override
    public boolean isDirty(){
        return true;
    }
}
