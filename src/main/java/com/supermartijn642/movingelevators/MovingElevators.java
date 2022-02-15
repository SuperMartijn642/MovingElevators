package com.supermartijn642.movingelevators;

import com.google.common.collect.Sets;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.movingelevators.blocks.*;
import com.supermartijn642.movingelevators.data.MovingElevatorsBlockTagsProvider;
import com.supermartijn642.movingelevators.data.MovingElevatorsLanguageProvider;
import com.supermartijn642.movingelevators.data.MovingElevatorsLootTableProvider;
import com.supermartijn642.movingelevators.data.MovingElevatorsRecipeProvider;
import com.supermartijn642.movingelevators.packets.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Set;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod("movingelevators")
public class MovingElevators {

    public static final Set<String> CAMOUFLAGE_MOD_BLACKLIST = Sets.newHashSet("movingelevators");

    public static final PacketChannel CHANNEL = PacketChannel.create("movingelevators");

    public static final CreativeModeTab GROUP = new CreativeModeTab("movingelevators") {
        @Override
        public ItemStack makeIcon(){
            return new ItemStack(elevator_block);
        }
    };

    @ObjectHolder("movingelevators:elevator_block")
    public static ControllerBlock elevator_block;
    @ObjectHolder("movingelevators:elevator_tile")
    public static BlockEntityType<ControllerBlockEntity> elevator_tile;
    @ObjectHolder("movingelevators:display_block")
    public static DisplayBlock display_block;
    @ObjectHolder("movingelevators:display_tile")
    public static BlockEntityType<DisplayBlockEntity> display_tile;
    @ObjectHolder("movingelevators:button_block")
    public static RemoteControllerBlock button_block;
    @ObjectHolder("movingelevators:button_tile")
    public static BlockEntityType<RemoteControllerBlockEntity> button_tile;

    public MovingElevators(){
        CHANNEL.registerMessage(PacketAddElevatorGroup.class, PacketAddElevatorGroup::new, true);
        CHANNEL.registerMessage(PacketDecreaseCabinDepth.class, PacketDecreaseCabinDepth::new, true);
        CHANNEL.registerMessage(PacketDecreaseCabinDepthOffset.class, PacketDecreaseCabinDepthOffset::new, true);
        CHANNEL.registerMessage(PacketDecreaseCabinHeight.class, PacketDecreaseCabinHeight::new, true);
        CHANNEL.registerMessage(PacketDecreaseCabinHeightOffset.class, PacketDecreaseCabinHeightOffset::new, true);
        CHANNEL.registerMessage(PacketDecreaseCabinSideOffset.class, PacketDecreaseCabinSideOffset::new, true);
        CHANNEL.registerMessage(PacketDecreaseCabinWidth.class, PacketDecreaseCabinWidth::new, true);
        CHANNEL.registerMessage(PacketElevatorSpeed.class, PacketElevatorSpeed::new, true);
        CHANNEL.registerMessage(PacketIncreaseCabinDepth.class, PacketIncreaseCabinDepth::new, true);
        CHANNEL.registerMessage(PacketIncreaseCabinDepthOffset.class, PacketIncreaseCabinDepthOffset::new, true);
        CHANNEL.registerMessage(PacketIncreaseCabinHeight.class, PacketIncreaseCabinHeight::new, true);
        CHANNEL.registerMessage(PacketIncreaseCabinHeightOffset.class, PacketIncreaseCabinHeightOffset::new, true);
        CHANNEL.registerMessage(PacketIncreaseCabinSideOffset.class, PacketIncreaseCabinSideOffset::new, true);
        CHANNEL.registerMessage(PacketIncreaseCabinWidth.class, PacketIncreaseCabinWidth::new, true);
        CHANNEL.registerMessage(PacketOnElevator.class, PacketOnElevator::new, true);
        CHANNEL.registerMessage(PacketRemoveElevatorGroup.class, PacketRemoveElevatorGroup::new, true);
        CHANNEL.registerMessage(PacketSetFloorName.class, PacketSetFloorName::new, true);
        CHANNEL.registerMessage(PacketSyncElevatorMovement.class, PacketSyncElevatorMovement::new, true);
        CHANNEL.registerMessage(PacketToggleShowControllerButtons.class, PacketToggleShowControllerButtons::new, true);
        CHANNEL.registerMessage(PacketUpdateElevatorGroups.class, PacketUpdateElevatorGroups::new, true);

        MovingElevatorsConfig.init();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            BlockBehaviour.Properties properties = Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).strength(1.5F, 6.0F);
            e.getRegistry().register(new ControllerBlock("elevator_block", properties));
            e.getRegistry().register(new DisplayBlock("display_block", properties));
            e.getRegistry().register(new RemoteControllerBlock("button_block", properties));
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<BlockEntityType<?>> e){
            e.getRegistry().register(BlockEntityType.Builder.of(ControllerBlockEntity::new, elevator_block).build(null).setRegistryName("elevator_tile"));
            e.getRegistry().register(BlockEntityType.Builder.of(DisplayBlockEntity::new, display_block).build(null).setRegistryName("display_tile"));
            e.getRegistry().register(BlockEntityType.Builder.of(RemoteControllerBlockEntity::new, button_block).build(null).setRegistryName("button_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(elevator_block, new Item.Properties().tab(GROUP)).setRegistryName("elevator_block"));
            e.getRegistry().register(new BlockItem(display_block, new Item.Properties().tab(GROUP)).setRegistryName("display_block"));
            e.getRegistry().register(new RemoteControllerBlockItem(button_block, new Item.Properties().tab(GROUP)).setRegistryName("button_block"));
        }

        @SubscribeEvent
        public static void onGatherData(GatherDataEvent e){
            e.getGenerator().addProvider(new MovingElevatorsLanguageProvider(e));
            e.getGenerator().addProvider(new MovingElevatorsLootTableProvider(e));
            e.getGenerator().addProvider(new MovingElevatorsRecipeProvider(e));
            e.getGenerator().addProvider(new MovingElevatorsBlockTagsProvider(e));
        }
    }
}
