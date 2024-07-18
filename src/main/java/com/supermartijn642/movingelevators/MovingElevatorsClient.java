package com.supermartijn642.movingelevators;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import com.supermartijn642.movingelevators.blocks.DisplayBlockEntityRenderer;
import com.supermartijn642.movingelevators.blocks.ElevatorInputBlockEntityRenderer;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class MovingElevatorsClient implements ClientModInitializer {

    public static final ResourceLocation OVERLAY_TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("movingelevators", "blocks/block_overlays");

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("movingelevators");
        // Renderers
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.elevator_tile, ElevatorInputBlockEntityRenderer::new);
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.display_tile, DisplayBlockEntityRenderer::new);
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.button_tile, ElevatorInputBlockEntityRenderer::new);
        // Register texture
        handler.registerAtlasSprite(TextureAtlases.getBlocks(), OVERLAY_TEXTURE_LOCATION.getPath());
        // Baked models
        handler.registerBlockModelOverwrite(() -> MovingElevators.elevator_block, CamoBakedModel::new);
        handler.registerBlockModelOverwrite(() -> MovingElevators.display_block, CamoBakedModel::new);
        handler.registerBlockModelOverwrite(() -> MovingElevators.button_block, CamoBakedModel::new);
        // Block render types
        handler.registerBlockModelTranslucentRenderType(() -> MovingElevators.elevator_block);
        handler.registerBlockModelTranslucentRenderType(() -> MovingElevators.display_block);
        handler.registerBlockModelTranslucentRenderType(() -> MovingElevators.button_block);

        ColorProviderRegistry.BLOCK.register(
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
        return ClientUtils.getMinecraft().getTextureAtlas(TextureAtlases.getBlocks()).apply(OVERLAY_TEXTURE_LOCATION);
    }

    public static void openElevatorScreen(BlockPos pos){
        ClientUtils.displayScreen(WidgetScreen.of(new ElevatorScreen(pos)));
    }

    public static String formatFloorDisplayName(String name, int floor){
        return name == null ? TextComponents.translation("movingelevators.floor_name", TextComponents.number(floor).get()).format() : name;
    }
}
