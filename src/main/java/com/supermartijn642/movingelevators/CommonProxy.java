package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        e.getRegistry().register(new ElevatorBlock());
        GameRegistry.registerTileEntity(ElevatorBlockTile.class, new ResourceLocation(MovingElevators.MODID, "elevatorblocktile"));
        e.getRegistry().register(new DisplayBlock());
        GameRegistry.registerTileEntity(METile.class, new ResourceLocation(MovingElevators.MODID, "displayblocktile"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e){
        e.getRegistry().register(new ItemBlock(MovingElevators.elevator_block).setRegistryName(MovingElevators.elevator_block.getRegistryName()));
        e.getRegistry().register(new ItemBlock(MovingElevators.display_block).setRegistryName(MovingElevators.display_block.getRegistryName()));
    }
}
