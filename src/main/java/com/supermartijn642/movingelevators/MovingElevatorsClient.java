package com.supermartijn642.movingelevators;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.movingelevators.blocks.*;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.stream.Collectors;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class MovingElevatorsClient {

    public static final ResourceLocation OVERLAY_TEXTURE_LOCATION = new ResourceLocation("movingelevators", "blocks/block_overlays");
    public static TextureAtlasSprite OVERLAY_SPRITE;

    @SubscribeEvent
    public static void setup(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(ControllerBlockEntity.class, new ElevatorInputBlockEntityRenderer<>());
        ClientRegistry.bindTileEntitySpecialRenderer(DisplayBlockEntity.class, new DisplayBlockEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(RemoteControllerBlockEntity.class, new ElevatorInputBlockEntityRenderer<>());

        // TODO place this somewhere better
        ClientUtils.getMinecraft().getBlockColors().registerBlockColorHandler(
            (state, blockAndTintGetter, pos, p_92570_) -> {
                if(blockAndTintGetter == null || pos == null)
                    return 0;
                TileEntity entity = blockAndTintGetter.getTileEntity(pos);
                return entity instanceof CamoBlockEntity && ((CamoBlockEntity)entity).hasCamoState() ? ClientUtils.getMinecraft().getBlockColors().colorMultiplier(((CamoBlockEntity)entity).getCamoState(), blockAndTintGetter, pos, p_92570_) : 0;
            },
            MovingElevators.elevator_block, MovingElevators.display_block, MovingElevators.button_block
        );
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MovingElevators.elevator_block), 0, new ModelResourceLocation(MovingElevators.elevator_block.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MovingElevators.display_block), 0, new ModelResourceLocation(MovingElevators.display_block.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MovingElevators.button_block), 0, new ModelResourceLocation(MovingElevators.button_block.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e){
        setCamouflageModel(e, MovingElevators.elevator_block);
        setCamouflageModel(e, MovingElevators.display_block);
        setCamouflageModel(e, MovingElevators.button_block);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre e){
        if(e.getMap() == ClientUtils.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE))
            OVERLAY_SPRITE = e.getMap().registerSprite(OVERLAY_TEXTURE_LOCATION);
    }

    private static void setCamouflageModel(ModelBakeEvent e, Block block){
        for(IBlockState state : block.getBlockState().getValidStates()){
            StringBuilder builder = new StringBuilder();
            if(!state.getProperties().isEmpty())
                builder.append(state.getProperties().entrySet().stream().map(entry -> getPropertyName(entry.getKey(), entry.getValue())).collect(Collectors.joining(",")));
            else
                builder.append("normal");

            ModelResourceLocation modelLocation = new ModelResourceLocation(block.getRegistryName(), builder.toString());
            IBakedModel model = e.getModelManager().getModel(modelLocation);
            e.getModelRegistry().putObject(modelLocation, new CamoBakedModel(model));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value){
        return property.getName() + "=" + property.getName((T)value);
    }

    public static void openElevatorScreen(BlockPos pos){
        ClientUtils.displayScreen(new ElevatorScreen(pos));
    }

    public static String formatFloorDisplayName(String name, int floor){
        return name == null ? TextComponents.translation("movingelevators.floor_name", TextComponents.number(floor).get()).format() : name;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e){
        if(e.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().world != null)
            ElevatorGroupCapability.tickWorldCapability(Minecraft.getMinecraft().world);
    }
}
