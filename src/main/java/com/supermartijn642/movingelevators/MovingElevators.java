package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.packets.PacketElevatorSize;
import com.supermartijn642.movingelevators.packets.PacketElevatorSpeed;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod("movingelevators")
public class MovingElevators {

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("movingelevators","main"),() -> "1","1"::equals,"1"::equals);

    @ObjectHolder("movingelevators:elevator_block")
    public static ElevatorBlock elevator_block;
    @ObjectHolder("movingelevators:elevator_tile")
    public static TileEntityType<ElevatorBlockTile> elevator_tile;

    public MovingElevators(){
        CHANNEL.registerMessage(0, PacketElevatorSize.class,PacketElevatorSize::encode,PacketElevatorSize::decode,PacketElevatorSize::handle);
        CHANNEL.registerMessage(1, PacketElevatorSpeed.class,PacketElevatorSpeed::encode,PacketElevatorSpeed::decode,PacketElevatorSpeed::handle);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new ElevatorBlock());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e){
            e.getRegistry().register(TileEntityType.Builder.create(ElevatorBlockTile::new, elevator_block).build(null).setRegistryName("elevator_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(elevator_block, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName("elevator_block"));
        }
    }
}
