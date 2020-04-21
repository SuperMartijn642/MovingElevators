package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketElevatorName;
import com.supermartijn642.movingelevators.packets.PacketElevatorSize;
import com.supermartijn642.movingelevators.packets.PacketElevatorSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorScreen extends GuiScreen {

    private static final int MAX_NAME_CHARACTER_COUNT = 11;

    private BlockPos elevatorPos;
    private GuiTextField nameField;
    private String lastTickName;

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
        this.nameField = new GuiTextField(0, this.fontRenderer, (this.width - width) / 2, this.height / 13 * 4, width, height);
        this.nameField.setText(tile.getName());
        this.lastTickName = this.nameField.getText();
        this.nameField.setCanLoseFocus(true);
        this.nameField.setFocused(false);
        this.nameField.setMaxStringLength(MAX_NAME_CHARACTER_COUNT);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseButton == 1 && mouseX >= this.nameField.x && mouseX < this.nameField.x + this.nameField.width && mouseY >= this.nameField.y && mouseY < this.nameField.y + this.nameField.height){
            this.nameField.setText("");
        }
    }

    @Override
    protected void keyTyped(char c, int keyCode) throws IOException{
        super.keyTyped(c, keyCode);
        this.nameField.textboxKeyTyped(c, keyCode);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null)
            return;
        this.nameField.updateCursorCounter();
        if(!this.lastTickName.equals(this.nameField.getText())){
            String name = this.nameField.getText();
            if(name.isEmpty() ? !tile.getDefaultName().equals(tile.getName()) : !name.equals(tile.getName()))
                MovingElevators.channel.sendToServer(new PacketElevatorName(tile.getPos(), name.isEmpty() || name.equals(tile.getDefaultName()) ? null : name));
            this.lastTickName = name;
        }
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

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        this.fontRenderer.drawString(I18n.format("gui.movingelevators.floorname.label"), this.nameField.x + 2, this.height / 4, Integer.MAX_VALUE);
        this.nameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
