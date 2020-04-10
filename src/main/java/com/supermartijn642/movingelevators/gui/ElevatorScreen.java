package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketElevatorSize;
import com.supermartijn642.movingelevators.packets.PacketElevatorSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorScreen extends GuiScreen {

    private BlockPos elevatorPos;

    public ElevatorScreen(BlockPos elevatorPos){
        this.elevatorPos = elevatorPos;
    }

    @Override
    public void initGui(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null)
            return;
        int width = 150;
        int height = 20;
        final BlockPos pos = tile.getPos();
        this.addButton(new ElevatorSizeSlider(this.width / 2 - width - 10, this.height / 2 - height / 2, width, height, tile.getGroup().getSize(), slider -> {
            MovingElevators.channel.sendToServer(new PacketElevatorSize(pos, slider.getValue()));
        }));
        this.addButton(new ElevatorSpeedSlider(this.width / 2 + 10, this.height / 2 - height / 2, width, height, tile.getGroup().getSpeed(), slider -> {
            MovingElevators.channel.sendToServer(new PacketElevatorSpeed(pos, slider.getValue()));
        }));
    }

    @Override
    public void updateScreen(){
        this.getTileOrClose();
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

    private ElevatorBlockTile getTileOrClose(){
        World world = Minecraft.getMinecraft().world;
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(world == null || player == null)
            return null;
        TileEntity tile = world.getTileEntity(this.elevatorPos);
        if(tile instanceof ElevatorBlockTile)
            return (ElevatorBlockTile)tile;
        player.closeScreen();
        return null;
    }
}
