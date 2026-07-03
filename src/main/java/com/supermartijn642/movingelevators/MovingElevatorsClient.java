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
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class MovingElevatorsClient {

    public static final SpriteId OVERLAY_TEXTURE_LOCATION = new SpriteId(TextureAtlases.getBlocks(), Identifier.fromNamespaceAndPath("movingelevators", "blocks/block_overlays"));

    public static void register(){
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
    }

    @SubscribeEvent
    public static void setup(RegisterColorHandlersEvent.BlockTintSources e){
        int layers = 4;
        List<BlockTintSource> tintSources = new ArrayList<>(layers);
        for(int i = 0; i < layers; i++){
            int index = i;
            tintSources.add(new BlockTintSource() {
                @Override
                public int color(BlockState state){
                    return -1;
                }

                @Override
                public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos){
                    BlockEntity entity = level.getBlockEntity(pos);
                    if(!(entity instanceof CamoBlockEntity) || !((CamoBlockEntity)entity).hasCamoState())
                        return -1;
                    BlockState camoState = ((CamoBlockEntity)entity).getCamoState();
                    BlockTintSource tintSource = ClientUtils.getMinecraft().getBlockColors().getTintSource(camoState, index);
                    return tintSource == null ? -1 : tintSource.colorInWorld(camoState, level, pos);
                }

                @Override
                public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos){
                    BlockEntity entity = level.getBlockEntity(pos);
                    if(!(entity instanceof CamoBlockEntity) || !((CamoBlockEntity)entity).hasCamoState())
                        return -1;
                    BlockState camoState = ((CamoBlockEntity)entity).getCamoState();
                    BlockTintSource tintSource = ClientUtils.getMinecraft().getBlockColors().getTintSource(camoState, index);
                    return tintSource == null ? -1 : tintSource.colorAsTerrainParticle(camoState, level, pos);
                }
            });
        }
        e.register(
            tintSources,
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
    public static void onClientTick(ClientTickEvent.Post e){
        if(!ClientUtils.getMinecraft().isPaused() && ClientUtils.getWorld() != null)
            ElevatorGroupCapability.tickWorldCapability(ClientUtils.getWorld());
    }
}
