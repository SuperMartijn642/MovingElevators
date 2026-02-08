package com.supermartijn642.movingelevators;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 05/02/2022 by SuperMartijn642
 */
public class MovingElevatorsConfig {

    public static final Supplier<Integer> maxCabinHorizontalSize;
    public static final Supplier<Integer> maxCabinVerticalSize;
    public static final Supplier<Boolean> allowUnbreakableBlocks;

    static{
        IConfigBuilder builder = ConfigBuilders.newTomlConfig("movingelevators", null, false);

        builder.push("General");
        maxCabinHorizontalSize = builder.comment("What should be the maximum width of an elevator cabin? Higher numbers may cause lag.").define("maxCabinHorizontalSize", 7, 1, 15);
        maxCabinVerticalSize = builder.comment("What should be the maximum height of an elevator cabin? Higher numbers may cause lag.").define("maxCabinVerticalSize", 7, 1, 15);
        allowUnbreakableBlocks = builder.comment("Is the elevator allowed to move unbreakable blocks? If set to true, this may allow players to move blocks like bedrock and portals using elevators!").define("allowUnbreakableBlocks", false);
        builder.pop();

        builder.build();
    }

    public static void init(){
    }
}
