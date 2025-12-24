package com.supermartijn642.movingelevators;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import com.supermartijn642.movingelevators.blocks.DisplayBlockEntityRenderer;
import com.supermartijn642.movingelevators.blocks.ElevatorInputBlockEntityRenderer;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "movingelevators")
public class MovingElevatorsClient {

    public static final Material OVERLAY_TEXTURE_LOCATION = new Material(TextureAtlases.getBlocks(), ResourceLocation.fromNamespaceAndPath("movingelevators", "blocks/block_overlays"));

    public static void register(){
        RegisterColorHandlersEvent.Block.BUS.addListener(MovingElevatorsClient::setup);
        ElevatorGroupRenderer.registerEventListeners();

        ClientRegistrationHandler handler = ClientRegistrationHandler.get("movingelevators");
        // Renderers
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.elevator_tile, ElevatorInputBlockEntityRenderer::new);
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.display_tile, DisplayBlockEntityRenderer::new);
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.button_tile, ElevatorInputBlockEntityRenderer::new);
        // Register texture
        handler.registerAtlasSprite(TextureAtlases.getBlocks(), OVERLAY_TEXTURE_LOCATION.texture().getPath());
        // Baked models
        handler.registerBlockModelOverwrite(() -> MovingElevators.elevator_block, CamoBakedModel::new);
        handler.registerBlockModelOverwrite(() -> MovingElevators.display_block, CamoBakedModel::new);
        handler.registerBlockModelOverwrite(() -> MovingElevators.button_block, CamoBakedModel::new);
    }

    public static void setup(RegisterColorHandlersEvent.Block e){
        e.register(
            (state, blockAndTintGetter, pos, p_92570_) -> {
                if(blockAndTintGetter == null || pos == null)
                    return 0;
                BlockEntity entity = blockAndTintGetter.getBlockEntity(pos);
                return entity instanceof CamoBlockEntity && ((CamoBlockEntity)entity).hasCamoState() ? ClientUtils.getMinecraft().getBlockColors().getColor(((CamoBlockEntity)entity).getCamoState(), blockAndTintGetter, pos, p_92570_) : 0;
            },
            MovingElevators.elevator_block, MovingElevators.display_block, MovingElevators.button_block
        );
    }

    public static TextureAtlasSprite getOverlaySprite(){
        return ClientUtils.getMinecraft().getAtlasManager().get(OVERLAY_TEXTURE_LOCATION);
    }

    public static void openElevatorScreen(BlockPos pos){
        ClientUtils.displayScreen(WidgetScreen.of(new ElevatorScreen(pos)));
    }

    public static String formatFloorDisplayName(String name, int floor){
        return name == null ? TextComponents.translation("movingelevators.floor_name", TextComponents.number(floor).get()).format() : name;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post e){
        if(!ClientUtils.getMinecraft().isPaused() && ClientUtils.getWorld() != null)
            ElevatorGroupCapability.tickWorldCapability(ClientUtils.getWorld());
    }
}
