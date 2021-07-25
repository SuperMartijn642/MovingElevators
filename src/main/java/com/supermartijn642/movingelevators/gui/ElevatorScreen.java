package com.supermartijn642.movingelevators.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.ElevatorBlockTile;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketElevatorName;
import com.supermartijn642.movingelevators.packets.PacketElevatorSize;
import com.supermartijn642.movingelevators.packets.PacketElevatorSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorScreen extends Screen {

    private static final int MAX_NAME_CHARACTER_COUNT = 11;

    private BlockPos elevatorPos;
    private AbstractWidget sizeSlider, speedSlider;
    private EditBox nameField;
    private String lastTickName;

    public ElevatorScreen(BlockPos elevatorPos){
        super(new TranslatableComponent("gui.movingelevators.title"));
        this.elevatorPos = elevatorPos;
    }

    @Override
    protected void init(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null || !tile.hasGroup())
            return;
        int width = 150;
        int height = 20;
        final BlockPos pos = tile.getBlockPos();
        this.sizeSlider = this.addWidget(new ElevatorSizeSlider(this.width / 2 - width - 10, this.height / 2 - height / 2, width, height, tile.getGroup().getSize(), value -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSize(pos, value));
        }));
        this.renderables.add(this.sizeSlider);
        this.speedSlider = this.addWidget(new ElevatorSpeedSlider(this.width / 2 + 10, this.height / 2 - height / 2, width, height, tile.getGroup().getSpeed(), value -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSpeed(pos, value));
        }));
        this.renderables.add(this.speedSlider);
        this.nameField = this.addWidget(new EditBox(this.font, (this.width - width) / 2, this.height / 13 * 4, width, height, new TextComponent("")));
        this.renderables.add(this.nameField);
        this.nameField.setValue(ClientProxy.formatFloorDisplayName(tile.getFloorName(), tile.getGroup().getFloorNumber(tile.getFloorLevel())));
        this.lastTickName = this.nameField.getValue();
        this.nameField.setCanLoseFocus(true);
        this.nameField.setFocus(false);
        this.nameField.setMaxLength(MAX_NAME_CHARACTER_COUNT);
    }

    @Override
    public void tick(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null || !tile.hasGroup())
            return;
        this.nameField.tick();
        if(!this.lastTickName.equals(this.nameField.getValue())){
            String name = this.nameField.getValue();
            String defaultName = ClientProxy.formatFloorDisplayName(null, tile.getGroup().getFloorNumber(tile.getFloorLevel()));
            if(name.isEmpty() ? !defaultName.equals(tile.getFloorName()) : !name.equals(tile.getFloorName()))
                MovingElevators.CHANNEL.sendToServer(new PacketElevatorName(tile.getBlockPos(), name.isEmpty() || name.equals(defaultName) ? null : name));
            this.lastTickName = name;
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);
        this.font.draw(matrixStack, I18n.get("gui.movingelevators.floorname.label"), this.nameField.x + 2, this.height / 4f, Integer.MAX_VALUE);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    private ElevatorBlockTile getTileOrClose(){
        Level world = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if(world == null || player == null)
            return null;
        BlockEntity tile = world.getBlockEntity(this.elevatorPos);
        if(tile instanceof ElevatorBlockTile)
            return (ElevatorBlockTile)tile;
        player.closeContainer();
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton){
        if(mouseButton == 1){ // text field
            if(mouseX >= this.nameField.x && mouseX < this.nameField.x + this.nameField.getWidth()
                && mouseY >= this.nameField.y && mouseY < this.nameField.y + this.nameField.getHeight())
                this.nameField.setValue("");
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return false;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
        if(super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
            return true;
        InputConstants.Key mouseKey = InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_);
        if(!this.nameField.isFocused() && (p_keyPressed_1_ == 256 || Minecraft.getInstance().options.keyInventory.isActiveAndMatches(mouseKey))){
            Minecraft.getInstance().player.closeContainer();
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
