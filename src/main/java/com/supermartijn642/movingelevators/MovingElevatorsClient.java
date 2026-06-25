package com.supermartijn642.movingelevators;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import com.supermartijn642.movingelevators.blocks.DisplayBlockEntityRenderer;
import com.supermartijn642.movingelevators.blocks.ElevatorInputBlockEntityRenderer;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupRenderer;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class MovingElevatorsClient implements ClientModInitializer {

    public static final SpriteId OVERLAY_TEXTURE_LOCATION = new SpriteId(TextureAtlases.getBlocks(), Identifier.fromNamespaceAndPath("movingelevators", "blocks/block_overlays"));

    @Override
    public void onInitializeClient(){
        ElevatorGroupRenderer.registerEventListeners();

        ClientRegistrationHandler handler = ClientRegistrationHandler.get("movingelevators");
        // Renderers
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.elevator_tile, ElevatorInputBlockEntityRenderer::new);
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.display_tile, DisplayBlockEntityRenderer::new);
        handler.registerCustomBlockEntityRenderer(() -> MovingElevators.button_tile, ElevatorInputBlockEntityRenderer::new);
        // Register texture
        handler.registerAtlasSprite(TextureAtlases.getBlocks(), OVERLAY_TEXTURE_LOCATION.texture());
        // Baked models
        handler.registerBlockStateModelOverwrite(() -> MovingElevators.elevator_block, CamoBakedModel::new);
        handler.registerBlockStateModelOverwrite(() -> MovingElevators.display_block, CamoBakedModel::new);
        handler.registerBlockStateModelOverwrite(() -> MovingElevators.button_block, CamoBakedModel::new);

        BlockColorRegistry.register(
            (state, level, pos, tintLayers) -> {
                BlockEntity entity = level.getBlockEntity(pos);
                if(!(entity instanceof CamoBlockEntity) || !((CamoBlockEntity)entity).hasCamoState())
                    return;
                BlockState camoState = ((CamoBlockEntity)entity).getCamoState();
                for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(camoState))
                    tintLayers.add(tintSource.colorInWorld(camoState, level, pos));
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
}
