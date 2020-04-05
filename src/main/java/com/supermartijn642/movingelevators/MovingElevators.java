package com.supermartijn642.movingelevators;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
@Mod(modid = MovingElevators.MODID,name = MovingElevators.NAME, version = MovingElevators.VERSION)
public class MovingElevators {

    public static final String MODID = "movingelevators";
    public static final String NAME = "Moving Elevators";
    public static final String VERSION = "1.0.0";

    @GameRegistry.ObjectHolder("movingelevators:elevator_block")
    public static ElevatorBlock elevator_block;

    @SidedProxy(clientSide = "com.supermartijn642.movingelevators.ClientProxy", serverSide = "com.supermartijn642.movingelevators.CommonProxy")
    public static CommonProxy proxy;

}
