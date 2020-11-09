package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTileRenderer;
import com.supermartijn642.movingelevators.base.METileRenderer;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(ElevatorBlockTile.class, new ElevatorInputTileRenderer<>());
        ClientRegistry.bindTileEntitySpecialRenderer(DisplayBlockTile.class, new METileRenderer<>());
        ClientRegistry.bindTileEntitySpecialRenderer(ButtonBlockTile.class, new ElevatorInputTileRenderer<>());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MovingElevators.elevator_block), 0, new ModelResourceLocation(MovingElevators.elevator_block.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MovingElevators.display_block), 0, new ModelResourceLocation(MovingElevators.display_block.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MovingElevators.button_block), 0, new ModelResourceLocation(MovingElevators.button_block.getRegistryName(), "inventory"));
    }

    public static void openElevatorScreen(BlockPos pos){
        Minecraft.getMinecraft().displayGuiScreen(new ElevatorScreen(pos));
    }

    public static String translate(String s){
        return I18n.format(s);
    }

    public static String formatFloorDisplayName(String name, int floor){
        return name == null ? translate("movingelevators.floorname").replace("$number$", Integer.toString(floor)) : name;
    }

    public static EntityPlayer getPlayer(){
        return Minecraft.getMinecraft().player;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e){
        if(e.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().world != null)
            ElevatorGroupCapability.tickWorldCapability(Minecraft.getMinecraft().world);
    }

    public static void queTask(Runnable task){
        Minecraft.getMinecraft().addScheduledTask(task);
    }
}
