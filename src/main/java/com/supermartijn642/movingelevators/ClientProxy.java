package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTileRenderer;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import com.supermartijn642.movingelevators.model.MEBlockBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        BlockEntityRenderers.register(MovingElevators.elevator_tile, context -> new ElevatorInputTileRenderer());
        BlockEntityRenderers.register(MovingElevators.button_tile, context -> new ElevatorInputTileRenderer());

        ItemBlockRenderTypes.setRenderLayer(MovingElevators.elevator_block, RenderType.cutoutMipped()); // TODO change this to translucent
        ItemBlockRenderTypes.setRenderLayer(MovingElevators.display_block, RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(MovingElevators.button_block, RenderType.cutoutMipped());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e){
        setCamouflageModel(e, MovingElevators.elevator_block);
        setCamouflageModel(e, MovingElevators.display_block);
        setCamouflageModel(e, MovingElevators.button_block);
    }

    private static void setCamouflageModel(ModelBakeEvent e, Block block){
        for(BlockState state : block.getStateDefinition().getPossibleStates()){
            StringBuilder builder = new StringBuilder();
            if(!state.getValues().isEmpty())
                builder.append(state.getValues().entrySet().stream().map(entry -> getPropertyName(entry.getKey(), entry.getValue())).collect(Collectors.joining(",")));

            ModelResourceLocation modelLocation = new ModelResourceLocation(block.getRegistryName(), builder.toString());
            BakedModel model = e.getModelManager().getModel(modelLocation);
            e.getModelRegistry().put(modelLocation, new MEBlockBakedModel(model));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(Property<T> property, Comparable<?> value){
        return property.getName() + "=" + property.getName((T)value);
    }

    public static void openElevatorScreen(BlockPos pos){
        Minecraft.getInstance().setScreen(new ElevatorScreen(pos));
    }

    public static String translate(String s){
        return I18n.get(s);
    }

    public static String formatFloorDisplayName(String name, int floor){
        return name == null ? translate("movingelevators.floorname").replace("$number$", Integer.toString(floor)) : name;
    }

    public static Player getPlayer(){
        return Minecraft.getInstance().player;
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEventListeners {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent e){
            if(e.phase == TickEvent.Phase.END && !Minecraft.getInstance().isPaused() && Minecraft.getInstance().level != null)
                ElevatorGroupCapability.tickWorldCapability(Minecraft.getInstance().level);
        }
    }
}
