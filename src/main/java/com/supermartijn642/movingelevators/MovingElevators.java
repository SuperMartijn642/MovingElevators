package com.supermartijn642.movingelevators;

import com.google.common.collect.Sets;
import com.supermartijn642.movingelevators.data.MEBlockTagsProvider;
import com.supermartijn642.movingelevators.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Set;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod("movingelevators")
public class MovingElevators {

    public static final Set<String> CAMOUFLAGE_MOD_BLACKLIST = Sets.newHashSet("movingelevators");

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("movingelevators", "main"), () -> "1", "1"::equals, "1"::equals);

    @ObjectHolder("movingelevators:elevator_block")
    public static ElevatorBlock elevator_block;
    @ObjectHolder("movingelevators:elevator_tile")
    public static BlockEntityType<ElevatorBlockTile> elevator_tile;
    @ObjectHolder("movingelevators:display_block")
    public static DisplayBlock display_block;
    @ObjectHolder("movingelevators:display_tile")
    public static BlockEntityType<DisplayBlockTile> display_tile;
    @ObjectHolder("movingelevators:button_block")
    public static ButtonBlock button_block;
    @ObjectHolder("movingelevators:button_tile")
    public static BlockEntityType<ButtonBlockTile> button_tile;

    public MovingElevators(){
        CHANNEL.registerMessage(0, PacketElevatorSize.class, PacketElevatorSize::encode, PacketElevatorSize::decode, PacketElevatorSize::handle);
        CHANNEL.registerMessage(1, PacketElevatorSpeed.class, PacketElevatorSpeed::encode, PacketElevatorSpeed::decode, PacketElevatorSpeed::handle);
        CHANNEL.registerMessage(2, PacketElevatorName.class, PacketElevatorName::encode, PacketElevatorName::decode, PacketElevatorName::handle);
        CHANNEL.registerMessage(3, PacketOnElevator.class, (a, b) -> {
        }, buffer -> new PacketOnElevator(), PacketOnElevator::handle);
        CHANNEL.registerMessage(4, ElevatorGroupPacket.class, ElevatorGroupPacket::encode, ElevatorGroupPacket::new, ElevatorGroupPacket::handle);
        CHANNEL.registerMessage(5, ElevatorGroupsPacket.class, ElevatorGroupsPacket::encode, ElevatorGroupsPacket::new, ElevatorGroupsPacket::handle);
        CHANNEL.registerMessage(6, ElevatorMovementPacket.class, ElevatorMovementPacket::encode, ElevatorMovementPacket::new, ElevatorMovementPacket::handle);
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
        public static void onTileRegistry(final RegistryEvent.Register<BlockEntityType<?>> e){
            e.getRegistry().register(BlockEntityType.Builder.of(ElevatorBlockTile::new, elevator_block).build(null).setRegistryName("elevator_tile"));
            e.getRegistry().register(BlockEntityType.Builder.of(DisplayBlockTile::new, display_block).build(null).setRegistryName("display_tile"));
            e.getRegistry().register(BlockEntityType.Builder.of(ButtonBlockTile::new, button_block).build(null).setRegistryName("button_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(elevator_block, new Item.Properties().tab(CreativeModeTab.TAB_SEARCH)).setRegistryName("elevator_block"));
            e.getRegistry().register(new BlockItem(display_block, new Item.Properties().tab(CreativeModeTab.TAB_SEARCH)).setRegistryName("display_block"));
            e.getRegistry().register(new ButtonBlockItem(button_block, new Item.Properties().tab(CreativeModeTab.TAB_SEARCH)).setRegistryName("button_block"));
        }

        @SubscribeEvent
        public static void onGatherData(GatherDataEvent e){
            e.getGenerator().addProvider(new MEBlockTagsProvider(e));
        }
    }
}
