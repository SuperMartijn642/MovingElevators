package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorBlockTile;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketElevatorName;
import com.supermartijn642.movingelevators.packets.PacketElevatorSize;
import com.supermartijn642.movingelevators.packets.PacketElevatorSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.gui.widget.Slider;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorScreen extends Screen {

    private static final int MAX_NAME_CHARACTER_COUNT = 11;

    private BlockPos elevatorPos;
    private Slider sizeSlider, speedSlider;
    private TextFieldWidget nameField;
    private String lastTickName;

    public ElevatorScreen(BlockPos elevatorPos){
        super(new TranslationTextComponent("gui.movingelevators.title"));
        this.elevatorPos = elevatorPos;
    }

    @Override
    protected void init(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null || !tile.hasGroup())
            return;
        int width = 150;
        int height = 20;
        final BlockPos pos = tile.getPos();
        this.sizeSlider = this.addButton(new ElevatorSizeSlider(this.width / 2 - width - 10, this.height / 2 - height / 2, width, height, tile.getGroup().getSize(), slider -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSize(pos, slider.getValueInt()));
        }));
        this.speedSlider = this.addButton(new ElevatorSpeedSlider(this.width / 2 + 10, this.height / 2 - height / 2, width, height, tile.getGroup().getSpeed(), slider -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSpeed(pos, slider.getValue()));
        }));
        this.children.add(this.nameField = new TextFieldWidget(this.font, (this.width - width) / 2, this.height / 13 * 4, width, height, ""));
        this.nameField.setText(ClientProxy.formatFloorDisplayName(tile.getFloorName(), tile.getGroup().getFloorNumber(tile.getFloorLevel())));
        this.lastTickName = this.nameField.getText();
        this.nameField.setCanLoseFocus(true);
        this.nameField.setFocused2(false);
        this.nameField.setMaxStringLength(MAX_NAME_CHARACTER_COUNT);
    }

    @Override
    public void tick(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null || !tile.hasGroup())
            return;
        this.nameField.tick();
        if(!this.lastTickName.equals(this.nameField.getText())){
            String name = this.nameField.getText();
            String defaultName = ClientProxy.formatFloorDisplayName(null, tile.getGroup().getFloorNumber(tile.getFloorLevel()));
            if(name.isEmpty() ? !defaultName.equals(tile.getFloorName()) : !name.equals(tile.getFloorName()))
                MovingElevators.CHANNEL.sendToServer(new PacketElevatorName(tile.getPos(), name.isEmpty() || name.equals(defaultName) ? null : name));
            this.lastTickName = name;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        this.renderBackground();
        this.font.drawString(I18n.format("gui.movingelevators.floorname.label"), this.nameField.x + 2, this.height / 4f, Integer.MAX_VALUE);
        this.nameField.render(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton){
        if(mouseButton == 1){ // text field
            if(mouseX >= this.nameField.x && mouseX < this.nameField.x + this.nameField.getWidth()
                && mouseY >= this.nameField.y && mouseY < this.nameField.y + this.nameField.getHeight())
                this.nameField.setText("");
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return false;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
        if(super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
            return true;
        InputMappings.Input mouseKey = InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_);
        if(!this.nameField.isFocused() && (p_keyPressed_1_ == 256 || Minecraft.getInstance().gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))){
            Minecraft.getInstance().player.closeScreen();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton){
        if(mouseButton == 0){
            this.sizeSlider.onRelease(mouseX, mouseY);
            this.speedSlider.onRelease(mouseX, mouseY);
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
