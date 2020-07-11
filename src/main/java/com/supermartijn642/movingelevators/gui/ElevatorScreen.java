package com.supermartijn642.movingelevators.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.movingelevators.ElevatorBlockTile;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketElevatorName;
import com.supermartijn642.movingelevators.packets.PacketElevatorSize;
import com.supermartijn642.movingelevators.packets.PacketElevatorSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
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
    protected void func_231160_c_(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null)
            return;
        int width = 150;
        int height = 20;
        final BlockPos pos = tile.getPos();
        this.sizeSlider = this.func_230480_a_(new ElevatorSizeSlider(this.field_230708_k_ / 2 - width - 10, this.field_230709_l_ / 2 - height / 2, width, height, tile.getGroup().getSize(), slider -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSize(pos, slider.getValueInt()));
        }));
        this.speedSlider = this.func_230480_a_(new ElevatorSpeedSlider(this.field_230708_k_ / 2 + 10, this.field_230709_l_ / 2 - height / 2, width, height, tile.getGroup().getSpeed(), slider -> {
            MovingElevators.CHANNEL.sendToServer(new PacketElevatorSpeed(pos, slider.getValue()));
        }));
        this.field_230705_e_.add(this.nameField = new TextFieldWidget(this.field_230712_o_, (this.field_230708_k_ - width) / 2, this.field_230709_l_ / 13 * 4, width, height, new StringTextComponent("")));
        this.nameField.setText(tile.getFloorName());
        this.lastTickName = this.nameField.getText();
        this.nameField.setCanLoseFocus(true);
        this.nameField.setFocused2(false);
        this.nameField.setMaxStringLength(MAX_NAME_CHARACTER_COUNT);
    }

    @Override
    public void func_231023_e_(){
        ElevatorBlockTile tile = this.getTileOrClose();
        if(tile == null)
            return;
        this.nameField.tick();
        if(!this.lastTickName.equals(this.nameField.getText())){
            String name = this.nameField.getText();
            if(name.isEmpty() ? !tile.getDefaultFloorName().equals(tile.getFloorName()) : !name.equals(tile.getFloorName()))
                MovingElevators.CHANNEL.sendToServer(new PacketElevatorName(tile.getPos(), name.isEmpty() || name.equals(tile.getDefaultFloorName()) ? null : name));
            this.lastTickName = name;
        }
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.func_230446_a_(matrixStack);
        this.field_230712_o_.func_238422_b_(matrixStack, new TranslationTextComponent("gui.movingelevators.floorname.label"), this.nameField.field_230690_l_ + 2, this.field_230709_l_ / 4f, Integer.MAX_VALUE);
        this.nameField.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean func_231177_au__(){
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
    public boolean func_231044_a_(double mouseX, double mouseY, int mouseButton){
        if(mouseButton == 1){ // text field
            if(mouseX >= this.nameField.field_230690_l_ && mouseX < this.nameField.field_230690_l_ + this.nameField.func_230998_h_()
                && mouseY >= this.nameField.field_230691_m_ && mouseY < this.nameField.field_230691_m_ + this.nameField.getHeight())
                this.nameField.setText("");
        }
        super.func_231044_a_(mouseX, mouseY, mouseButton);
        return false;
    }

    @Override
    public boolean func_231046_a_(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
        if(super.func_231046_a_(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
            return true;
        InputMappings.Input mouseKey = InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_);
        if(!this.nameField.func_230999_j_() && (p_keyPressed_1_ == 256 || Minecraft.getInstance().gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))){
            Minecraft.getInstance().player.closeScreen();
            return true;
        }
        return false;
    }

    @Override
    public boolean func_231048_c_(double mouseX, double mouseY, int mouseButton){
        if(mouseButton == 0){
            this.sizeSlider.func_231000_a__(mouseX, mouseY);
            this.speedSlider.func_231000_a__(mouseX, mouseY);
        }
        return super.func_231048_c_(mouseX, mouseY, mouseButton);
    }
}
