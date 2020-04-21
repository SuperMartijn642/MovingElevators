package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
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
        ClientRegistry.bindTileEntityRenderer(MovingElevators.elevator_tile, ElevatorBlockTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(MovingElevators.display_tile, METileRenderer::new);
    }

    public static void openElevatorScreen(BlockPos pos){
        Minecraft.getInstance().displayGuiScreen(new ElevatorScreen(pos));
    }

    public static String translate(String s){
        return I18n.format(s);
    }
}
