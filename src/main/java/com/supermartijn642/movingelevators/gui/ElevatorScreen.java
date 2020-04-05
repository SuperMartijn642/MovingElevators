package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketElevatorSize;
import com.supermartijn642.movingelevators.packets.PacketElevatorSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorScreen extends Screen {

    private BlockPos elevatorPos;

    public ElevatorScreen(BlockPos elevatorPos){
        super(new TranslationTextComponent("gui.movingelevators.title"));
        this.elevatorPos = elevatorPos;
    }

    @Override
    protected void init(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null)
            return;
        int width = 150;
        int height = 20;
        final BlockPos pos = tile.getPos();
        this.addButton(new ElevatorSizeSlider(this.width / 2 - width - 10, this.height / 2 - height / 2, width, height, tile.getSize(), slider -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSize(pos, slider.getValue()));
        }));
        this.addButton(new ElevatorSpeedSlider(this.width / 2 + 10, this.height / 2 - height / 2, width, height, tile.getSpeed(), slider -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSpeed(pos, slider.getValue()));
        }));
    }

    @Override
    public void tick(){
        this.getTileOrClose();
    }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_){
        this.renderBackground();
        super.render(p_render_1_, p_render_2_, p_render_3_);
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    private ElevatorBlockTile getTileOrClose(){
        World world = Minecraft.getInstance().world;
        PlayerEntity player = Minecraft.getInstance().player;
        if(world == null || player == null)
            return null;
        TileEntity tile = world.getTileEntity(this.elevatorPos);
        if(tile instanceof ElevatorBlockTile)
            return (ElevatorBlockTile)tile;
        player.closeScreen();
        return null;
    }
}
