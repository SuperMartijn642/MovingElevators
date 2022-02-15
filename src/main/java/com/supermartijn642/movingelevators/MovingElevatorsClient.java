package com.supermartijn642.movingelevators;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.movingelevators.blocks.DisplayBlockEntityRenderer;
import com.supermartijn642.movingelevators.blocks.ElevatorInputBlockEntityRenderer;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.stream.Collectors;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MovingElevatorsClient {

    public static final ResourceLocation OVERLAY_TEXTURE_LOCATION = new ResourceLocation("movingelevators", "blocks/block_overlays");
    public static TextureAtlasSprite OVERLAY_SPRITE;

    @SubscribeEvent
    public static void setup(EntityRenderersEvent.RegisterRenderers e){
        e.registerBlockEntityRenderer(MovingElevators.elevator_tile, context -> new ElevatorInputBlockEntityRenderer<>());
        e.registerBlockEntityRenderer(MovingElevators.display_tile, context -> new DisplayBlockEntityRenderer());
        e.registerBlockEntityRenderer(MovingElevators.button_tile, context -> new ElevatorInputBlockEntityRenderer<>());
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        ItemBlockRenderTypes.setRenderLayer(MovingElevators.elevator_block, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(MovingElevators.display_block, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(MovingElevators.button_block, RenderType.translucent());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e){
        setCamouflageModel(e, MovingElevators.elevator_block);
        setCamouflageModel(e, MovingElevators.display_block);
        setCamouflageModel(e, MovingElevators.button_block);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre e){
        if(e.getMap().location().equals(TextureAtlas.LOCATION_BLOCKS))
            e.addSprite(OVERLAY_TEXTURE_LOCATION);
    }

    @SubscribeEvent
    public static void onTextureStitchPost(TextureStitchEvent.Post e){
        if(e.getMap().location().equals(TextureAtlas.LOCATION_BLOCKS))
            OVERLAY_SPRITE = e.getMap().getSprite(OVERLAY_TEXTURE_LOCATION);
    }

    private static void setCamouflageModel(ModelBakeEvent e, Block block){
        for(BlockState state : block.getStateDefinition().getPossibleStates()){
            StringBuilder builder = new StringBuilder();
            if(!state.getValues().isEmpty())
                builder.append(state.getValues().entrySet().stream().map(entry -> getPropertyName(entry.getKey(), entry.getValue())).collect(Collectors.joining(",")));

            ModelResourceLocation modelLocation = new ModelResourceLocation(block.getRegistryName(), builder.toString());
            BakedModel model = e.getModelManager().getModel(modelLocation);
            e.getModelRegistry().put(modelLocation, new CamoBakedModel(model));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(Property<T> property, Comparable<?> value){
        return property.getName() + "=" + property.getName((T)value);
    }

    public static void openElevatorScreen(BlockPos pos){
        Minecraft.getInstance().setScreen(new ElevatorScreen(pos));
    }

    public static String formatFloorDisplayName(String name, int floor){
        return name == null ? TextComponents.translation("movingelevators.floor_name", TextComponents.number(floor).get()).format() : name;
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
