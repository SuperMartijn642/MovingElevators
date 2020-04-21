package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.movingelevators.ElevatorBlockTile;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketElevatorName;
import com.supermartijn642.movingelevators.packets.PacketElevatorSize;
import com.supermartijn642.movingelevators.packets.PacketElevatorSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorScreen extends Screen {

    private static final int MAX_NAME_CHARACTER_COUNT = 11;

    private BlockPos elevatorPos;
    private TextFieldWidget nameField;
    private String lastTickName;

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
        this.addButton(new ElevatorSizeSlider(this.width / 2 - width - 10, this.height / 2 - height / 2, width, height, tile.getGroup().getSize(), slider -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSize(pos, slider.getValue()));
        }));
        this.addButton(new ElevatorSpeedSlider(this.width / 2 + 10, this.height / 2 - height / 2, width, height, tile.getGroup().getSpeed(), slider -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSpeed(pos, slider.getValue()));
        }));
        this.children.add(this.nameField = new TextFieldWidget(this.font, (this.width - width) / 2, this.height / 13 * 4, width, height, ""));
        this.nameField.setText(tile.getName());
        this.lastTickName = this.nameField.getText();
        this.nameField.setCanLoseFocus(true);
        this.nameField.setFocused2(false);
        this.nameField.setMaxStringLength(MAX_NAME_CHARACTER_COUNT);
    }

    @Override
    public void tick(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null)
            return;
        this.nameField.tick();
        if(!this.lastTickName.equals(this.nameField.getText())){
            String name = this.nameField.getText();
            if(name.isEmpty() ? !tile.getDefaultName().equals(tile.getName()) : !name.equals(tile.getName()))
                MovingElevators.CHANNEL.sendToServer(new PacketElevatorName(tile.getPos(), name.isEmpty() || name.equals(tile.getDefaultName()) ? null : name));
            this.lastTickName = name;
        }
    }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_){
        this.renderBackground();
        this.font.drawString(I18n.format("gui.movingelevators.floorname.label"), this.nameField.x + 2, this.height / 4f, Integer.MAX_VALUE);
        this.nameField.render(p_render_1_, p_render_2_, p_render_3_);
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
