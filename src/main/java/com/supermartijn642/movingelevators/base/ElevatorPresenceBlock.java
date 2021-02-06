package com.supermartijn642.movingelevators.base;

import java.util.function.Supplier;

public class ElevatorPresenceBlock extends MEBlock {
    public ElevatorPresenceBlock(String registry_name, Supplier<? extends METile> tileSupplier) {
        super(registry_name, tileSupplier);
    }
}
