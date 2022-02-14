package com.supermartijn642.movingelevators;

import com.google.common.collect.Sets;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.movingelevators.blocks.*;
import com.supermartijn642.movingelevators.data.MovingElevatorsLanguageProvider;
import com.supermartijn642.movingelevators.data.MovingElevatorsLootTableProvider;
import com.supermartijn642.movingelevators.data.MovingElevatorsRecipeProvider;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import com.supermartijn642.movingelevators.packets.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Set;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod("movingelevators")
public class MovingElevators {

    public static final Set<String> CAMOUFLAGE_MOD_BLACKLIST = Sets.newHashSet("movingelevators");

    public static final PacketChannel CHANNEL = PacketChannel.create("movingelevators");

    public static final ItemGroup GROUP = new ItemGroup("movingelevators") {
        @Override
        public ItemStack makeIcon(){
            return new ItemStack(elevator_block);
        }
    };

    @ObjectHolder("movingelevators:elevator_block")
    public static ControllerBlock elevator_block;
    @ObjectHolder("movingelevators:elevator_tile")
    public static TileEntityType<ControllerBlockEntity> elevator_tile;
    @ObjectHolder("movingelevators:display_block")
    public static DisplayBlock display_block;
    @ObjectHolder("movingelevators:display_tile")
    public static TileEntityType<DisplayBlockEntity> display_tile;
    @ObjectHolder("movingelevators:button_block")
    public static RemoteControllerBlock button_block;
    @ObjectHolder("movingelevators:button_tile")
    public static TileEntityType<RemoteControllerBlockEntity> button_tile;

    public MovingElevators(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);

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

    public void init(FMLCommonSetupEvent e){
        ElevatorGroupCapability.register();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            AbstractBlock.Properties properties = Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).strength(1.5F, 6.0F);
            e.getRegistry().register(new ControllerBlock("elevator_block", properties));
            e.getRegistry().register(new DisplayBlock("display_block", properties));
            e.getRegistry().register(new RemoteControllerBlock("button_block", properties));
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e){
            e.getRegistry().register(TileEntityType.Builder.of(ControllerBlockEntity::new, elevator_block).build(null).setRegistryName("elevator_tile"));
            e.getRegistry().register(TileEntityType.Builder.of(DisplayBlockEntity::new, display_block).build(null).setRegistryName("display_tile"));
            e.getRegistry().register(TileEntityType.Builder.of(RemoteControllerBlockEntity::new, button_block).build(null).setRegistryName("button_tile"));
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
        }
    }
}
