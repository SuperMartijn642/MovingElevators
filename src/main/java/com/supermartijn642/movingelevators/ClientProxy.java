package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTileRenderer;
import com.supermartijn642.movingelevators.base.METileRenderer;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        ClientRegistry.bindTileEntitySpecialRenderer(ElevatorBlockTile.class, new ElevatorInputTileRenderer<>());
        ClientRegistry.bindTileEntitySpecialRenderer(DisplayBlockTile.class, new METileRenderer<>());
        ClientRegistry.bindTileEntitySpecialRenderer(ButtonBlockTile.class, new ElevatorInputTileRenderer<>());
    }

    public static void openElevatorScreen(BlockPos pos){
        Minecraft.getInstance().displayGuiScreen(new ElevatorScreen(pos));
    }

    public static String translate(String s){
        return I18n.format(s);
    }

    public static String formatFloorDisplayName(String name, int floor){
        return name == null ? translate("movingelevators.floorname").replace("$number$", Integer.toString(floor)) : name;
    }

    public static PlayerEntity getPlayer(){
        return Minecraft.getInstance().player;
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEventListeners {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent e){
            if(e.phase == TickEvent.Phase.END && !Minecraft.getInstance().isGamePaused() && Minecraft.getInstance().world != null)
                ElevatorGroupCapability.tickWorldCapability(Minecraft.getInstance().world);
        }
    }
}
