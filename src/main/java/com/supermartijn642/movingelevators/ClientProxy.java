package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.gui.ElevatorScreen;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(ElevatorBlockTile.class,new ElevatorBlockTileRenderer());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MovingElevators.elevator_block), 0, new ModelResourceLocation(MovingElevators.elevator_block.getRegistryName(), "inventory"));
    }

    public static void openElevatorScreen(BlockPos pos){
        Minecraft.getMinecraft().displayGuiScreen(new ElevatorScreen(pos));
    }
}
