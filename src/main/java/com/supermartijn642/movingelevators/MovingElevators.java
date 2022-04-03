package com.supermartijn642.movingelevators;

import com.google.common.collect.Sets;
import com.supermartijn642.core.ToolType;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.movingelevators.blocks.*;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import com.supermartijn642.movingelevators.packets.*;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Set;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
@Mod(modid = MovingElevators.MODID, name = MovingElevators.NAME, version = MovingElevators.VERSION, dependencies = MovingElevators.DEPENDENCIES)
public class MovingElevators {

    public static final String MODID = "movingelevators";
    public static final String NAME = "Moving Elevators";
    public static final String VERSION = "1.3.6";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2779,);required-after:supermartijn642corelib@[1.0.16,);required-after:supermartijn642configlib@[1.0.9,)";

    public static final Set<String> CAMOUFLAGE_MOD_BLACKLIST = Sets.newHashSet("secretroomsmod", "movingelevators");

    public static final PacketChannel CHANNEL = PacketChannel.create("movingelevators");

    public static final CreativeTabs GROUP = new CreativeTabs("movingelevators") {
        @Override
        public ItemStack getTabIconItem(){
            return new ItemStack(elevator_block);
        }
    };

    @GameRegistry.ObjectHolder("movingelevators:elevator_block")
    public static ControllerBlock elevator_block;
    @GameRegistry.ObjectHolder("movingelevators:display_block")
    public static DisplayBlock display_block;
    @GameRegistry.ObjectHolder("movingelevators:button_block")
    public static RemoteControllerBlock button_block;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
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

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        ElevatorGroupCapability.register();
    }

    @Mod.EventBusSubscriber
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            BaseBlock.Properties properties = BaseBlock.Properties.create(Material.ROCK, MapColor.GRAY).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).hardnessAndResistance(1.5F, 6.0F);
            e.getRegistry().register(new ControllerBlock("elevator_block", properties).setCreativeTab(GROUP));
            e.getRegistry().register(new DisplayBlock("display_block", properties).setCreativeTab(GROUP));
            e.getRegistry().register(new RemoteControllerBlock("button_block", properties).setCreativeTab(GROUP));
            GameRegistry.registerTileEntity(ControllerBlockEntity.class, new ResourceLocation(MovingElevators.MODID, "elevatorblocktile"));
            GameRegistry.registerTileEntity(DisplayBlockEntity.class, new ResourceLocation(MovingElevators.MODID, "displayblocktile"));
            GameRegistry.registerTileEntity(RemoteControllerBlockEntity.class, new ResourceLocation(MovingElevators.MODID, "buttonblocktile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new ItemBlock(elevator_block).setRegistryName("elevator_block"));
            e.getRegistry().register(new ItemBlock(display_block).setRegistryName("display_block"));
            e.getRegistry().register(new RemoteControllerBlockItem(button_block).setRegistryName("button_block"));
        }
    }
}
