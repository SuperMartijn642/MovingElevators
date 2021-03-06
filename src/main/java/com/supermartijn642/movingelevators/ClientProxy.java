package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTileRenderer;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import com.supermartijn642.movingelevators.model.MEBlockBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.stream.Collectors;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        ClientRegistry.bindTileEntityRenderer(MovingElevators.elevator_tile, ElevatorInputTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(MovingElevators.button_tile, ElevatorInputTileRenderer::new);

        RenderTypeLookup.setRenderLayer(MovingElevators.elevator_block, RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(MovingElevators.display_block, RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(MovingElevators.button_block, RenderType.getCutoutMipped());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e){
        setCamouflageModel(e, MovingElevators.elevator_block);
        setCamouflageModel(e, MovingElevators.display_block);
        setCamouflageModel(e, MovingElevators.button_block);
    }

    private static void setCamouflageModel(ModelBakeEvent e, Block block){
        for(BlockState state : block.getStateContainer().getValidStates()){
            StringBuilder builder = new StringBuilder();
            if(!state.getValues().isEmpty())
                builder.append(state.getValues().entrySet().stream().map(entry -> getPropertyName(entry.getKey(), entry.getValue())).collect(Collectors.joining(",")));

            ModelResourceLocation modelLocation = new ModelResourceLocation(block.getRegistryName(), builder.toString());
            IBakedModel model = e.getModelManager().getModel(modelLocation);
            e.getModelRegistry().put(modelLocation, new MEBlockBakedModel(model));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value){
        return property.getName() + "=" + property.getName((T)value);
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
