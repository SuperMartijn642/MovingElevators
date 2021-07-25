package com.supermartijn642.movingelevators.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Consumer;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorSpeedSlider extends AbstractSliderButton {

    private static final int MIN = 1, MAX = 10;

    private final Consumer<Double> onChange;

    public ElevatorSpeedSlider(int xPos, int yPos, int width, int height, double currentVal, Consumer<Double> onChange){
        super(xPos, yPos, width, height, new TextComponent(""), currentVal);
        this.onChange = onChange;

        this.setMessage(new TextComponent(I18n.get("movingelevators.platform.speed").replace("$number$", String.format("%.1f", this.getValue()))));
    }

    public double getValue(){
        return ((int)Math.round(this.value * (MAX - MIN)) + MIN) / 10f;
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

        double value = this.getValue();
        this.setMessage(new TextComponent(I18n.get("movingelevators.platform.speed").replace("$number$", String.format("%.1f", value))));
        this.onChange.accept(value);
    }
}
