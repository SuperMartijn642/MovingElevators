package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.packets.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
@Mod(modid = MovingElevators.MODID, name = MovingElevators.NAME, version = MovingElevators.VERSION, dependencies = MovingElevators.DEPENDENCIES)
public class MovingElevators {

    public static final String MODID = "movingelevators";
    public static final String NAME = "Moving Elevators";
    public static final String VERSION = "1.2.27";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2779,)";

    public static SimpleNetworkWrapper channel;

    @GameRegistry.ObjectHolder("movingelevators:elevator_block")
    public static ElevatorBlock elevator_block;
    @GameRegistry.ObjectHolder("movingelevators:display_block")
    public static DisplayBlock display_block;
    @GameRegistry.ObjectHolder("movingelevators:button_block")
    public static ButtonBlock button_block;

    @SidedProxy(clientSide = "com.supermartijn642.movingelevators.ClientProxy", serverSide = "com.supermartijn642.movingelevators.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        channel.registerMessage(PacketElevatorSize.class, PacketElevatorSize.class, 0, Side.SERVER);
        channel.registerMessage(PacketElevatorSpeed.class, PacketElevatorSpeed.class, 1, Side.SERVER);
        channel.registerMessage(PacketElevatorName.class, PacketElevatorName.class, 2, Side.SERVER);
        channel.registerMessage(PacketOnElevator.class, PacketOnElevator.class, 3, Side.SERVER);
        channel.registerMessage(ElevatorGroupPacket.class, ElevatorGroupPacket.class, 4, Side.CLIENT);
        channel.registerMessage(ElevatorGroupsPacket.class, ElevatorGroupsPacket.class, 5, Side.CLIENT);
        channel.registerMessage(ElevatorMovementPacket.class, ElevatorMovementPacket.class, 6, Side.CLIENT);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        ElevatorGroupCapability.register();
    }

}
