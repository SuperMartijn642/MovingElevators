package com.supermartijn642.movingelevators.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Consumer;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorSizeSlider extends AbstractSliderButton {

    private static final int MAX = 4;

    private final Consumer<Integer> onChange;

    public ElevatorSizeSlider(int xPos, int yPos, int width, int height, int currentVal, Consumer<Integer> onChange){
        super(xPos, yPos, width, height, new TextComponent(""), ((double)(currentVal - 1) / 2) / MAX);
        this.onChange = onChange;

        int val = this.getValue();
        this.setMessage(new TextComponent(I18n.get("movingelevators.platform.size").replace("$number$", val + "x" + val)));
    }

    private int getValue(){
        return (int)Math.round(this.value * MAX) * 2 + 1;
    }

    @Override
    protected void updateMessage(){
    }

    @Override
    protected void applyValue(){
        if(this.value < 0.0F){
            this.value = 0.0F;
        }

        if(this.value > 1.0F){
            this.value = 1.0F;
        }

        int value = this.getValue();
        this.setMessage(new TextComponent(I18n.get("movingelevators.platform.size").replace("$number$", value + "x" + value)));
        this.onChange.accept(value);
    }
}
