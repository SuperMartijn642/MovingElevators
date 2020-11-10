package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.packets.*;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod("movingelevators")
public class MovingElevators {

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("movingelevators", "main"), () -> "1", "1"::equals, "1"::equals);

    @ObjectHolder("movingelevators:elevator_block")
    public static ElevatorBlock elevator_block;
    @ObjectHolder("movingelevators:elevator_tile")
    public static TileEntityType<ElevatorBlockTile> elevator_tile;
    @ObjectHolder("movingelevators:display_block")
    public static DisplayBlock display_block;
    @ObjectHolder("movingelevators:display_tile")
    public static TileEntityType<DisplayBlockTile> display_tile;
    @ObjectHolder("movingelevators:button_block")
    public static ButtonBlock button_block;
    @ObjectHolder("movingelevators:button_tile")
    public static TileEntityType<ButtonBlockTile> button_tile;

    public MovingElevators(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);

        CHANNEL.registerMessage(0, PacketElevatorSize.class, PacketElevatorSize::encode, PacketElevatorSize::decode, PacketElevatorSize::handle);
        CHANNEL.registerMessage(1, PacketElevatorSpeed.class, PacketElevatorSpeed::encode, PacketElevatorSpeed::decode, PacketElevatorSpeed::handle);
        CHANNEL.registerMessage(2, PacketElevatorName.class, PacketElevatorName::encode, PacketElevatorName::decode, PacketElevatorName::handle);
        CHANNEL.registerMessage(3, PacketOnElevator.class, (a, b) -> {
        }, buffer -> new PacketOnElevator(), PacketOnElevator::handle);
        CHANNEL.registerMessage(4, ElevatorGroupPacket.class, ElevatorGroupPacket::encode, ElevatorGroupPacket::new, ElevatorGroupPacket::handle);
        CHANNEL.registerMessage(5, ElevatorGroupsPacket.class, ElevatorGroupsPacket::encode, ElevatorGroupsPacket::new, ElevatorGroupsPacket::handle);
        CHANNEL.registerMessage(5, ElevatorMovementPacket.class, ElevatorMovementPacket::encode, ElevatorMovementPacket::new, ElevatorMovementPacket::handle);
    }

    public void init(FMLCommonSetupEvent e){
        ElevatorGroupCapability.register();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new ElevatorBlock());
            e.getRegistry().register(new DisplayBlock());
            e.getRegistry().register(new ButtonBlock());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e){
            e.getRegistry().register(TileEntityType.Builder.create(ElevatorBlockTile::new, elevator_block).build(null).setRegistryName("elevator_tile"));
            e.getRegistry().register(TileEntityType.Builder.create(DisplayBlockTile::new, display_block).build(null).setRegistryName("display_tile"));
            e.getRegistry().register(TileEntityType.Builder.create(ButtonBlockTile::new, button_block).build(null).setRegistryName("button_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(elevator_block, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName("elevator_block"));
            e.getRegistry().register(new BlockItem(display_block, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName("display_block"));
            e.getRegistry().register(new ButtonBlockItem(button_block, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName("button_block"));
        }
    }
}
