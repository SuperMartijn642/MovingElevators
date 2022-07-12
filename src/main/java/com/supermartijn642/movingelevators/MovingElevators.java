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
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Objects;
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

    @ObjectHolder(value = "movingelevators:elevator_block", registryName = "minecraft:block")
    public static ControllerBlock elevator_block;
    @ObjectHolder(value = "movingelevators:elevator_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<ControllerBlockEntity> elevator_tile;
    @ObjectHolder(value = "movingelevators:display_block", registryName = "minecraft:block")
    public static DisplayBlock display_block;
    @ObjectHolder(value = "movingelevators:display_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<DisplayBlockEntity> display_tile;
    @ObjectHolder(value = "movingelevators:button_block", registryName = "minecraft:block")
    public static RemoteControllerBlock button_block;
    @ObjectHolder(value = "movingelevators:button_tile", registryName = "minecraft:block_entity_type")
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
        public static void onRegisterEvent(RegisterEvent e){
            if(e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS))
                onBlockRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES))
                onTileRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS))
                onItemRegistry(Objects.requireNonNull(e.getForgeRegistry()));
        }

        public static void onBlockRegistry(IForgeRegistry<Block> registry){
            BlockBehaviour.Properties properties = Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).strength(1.5F, 6.0F);
            registry.register("elevator_block", new ControllerBlock("elevator_block", properties));
            registry.register("display_block", new DisplayBlock("display_block", properties));
            registry.register("button_block", new RemoteControllerBlock("button_block", properties));
        }

        public static void onTileRegistry(IForgeRegistry<BlockEntityType<?>> registry){
            registry.register("elevator_tile", BlockEntityType.Builder.of(ControllerBlockEntity::new, elevator_block).build(null));
            registry.register("display_tile", BlockEntityType.Builder.of(DisplayBlockEntity::new, display_block).build(null));
            registry.register("button_tile", BlockEntityType.Builder.of(RemoteControllerBlockEntity::new, button_block).build(null));
        }

        public static void onItemRegistry(IForgeRegistry<Item> registry){
            registry.register("elevator_block", new BlockItem(elevator_block, new Item.Properties().tab(GROUP)));
            registry.register("display_block", new BlockItem(display_block, new Item.Properties().tab(GROUP)));
            registry.register("button_block", new RemoteControllerBlockItem(button_block, new Item.Properties().tab(GROUP)));
        }

        @SubscribeEvent
        public static void onGatherData(GatherDataEvent e){
            e.getGenerator().addProvider(e.includeClient(), new MovingElevatorsLanguageProvider(e));
            e.getGenerator().addProvider(e.includeServer(), new MovingElevatorsLootTableProvider(e));
            e.getGenerator().addProvider(e.includeServer(), new MovingElevatorsRecipeProvider(e));
            e.getGenerator().addProvider(e.includeServer(), new MovingElevatorsBlockTagsProvider(e));
        }
    }
}
