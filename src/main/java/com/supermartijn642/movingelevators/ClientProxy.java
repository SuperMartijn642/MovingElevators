package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTileRenderer;
import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import com.supermartijn642.movingelevators.model.MEBlockBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.stream.Collectors;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(ElevatorBlockTile.class, new ElevatorInputTileRenderer<>());
        ClientRegistry.bindTileEntitySpecialRenderer(ButtonBlockTile.class, new ElevatorInputTileRenderer<>());
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

    private static void setCamouflageModel(ModelBakeEvent e, Block block){
        for(IBlockState state : block.getBlockState().getValidStates()){
            StringBuilder builder = new StringBuilder();
            if(!state.getProperties().isEmpty())
                builder.append(state.getProperties().entrySet().stream().map(entry -> getPropertyName(entry.getKey(), entry.getValue())).collect(Collectors.joining(",")));

            ModelResourceLocation modelLocation = new ModelResourceLocation(block.getRegistryName(), builder.toString());
            IBakedModel model = e.getModelManager().getModel(modelLocation);
            e.getModelRegistry().putObject(modelLocation, new MEBlockBakedModel(model));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value){
        return property.getName() + "=" + property.getName((T)value);
    }

    public static void openElevatorScreen(BlockPos pos){
        Minecraft.getMinecraft().displayGuiScreen(new ElevatorScreen(pos));
    }

    public static String translate(String s){
        return I18n.format(s);
    }

    public static String formatFloorDisplayName(String name, int floor){
        return name == null ? translate("movingelevators.floorname").replace("$number$", Integer.toString(floor)) : name;
    }

    public static EntityPlayer getPlayer(){
        return Minecraft.getMinecraft().player;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e){
        if(e.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().world != null)
            ElevatorGroupCapability.tickWorldCapability(Minecraft.getMinecraft().world);
    }

    public static void queTask(Runnable task){
        Minecraft.getMinecraft().addScheduledTask(task);
    }
}
