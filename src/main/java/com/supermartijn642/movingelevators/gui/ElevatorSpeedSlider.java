package com.supermartijn642.movingelevators.gui;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.gui.widget.Slider;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorSpeedSlider extends Slider {

    private int min = 1, max = 10;

    public ElevatorSpeedSlider(int xPos, int yPos, int width, int height, double currentVal, ISlider slider){
        super(xPos, yPos, width, height, "", "", 0.1, 1, currentVal, false, true, b -> {
        }, slider);

        float val = ((int)Math.round(this.sliderValue * (this.max - this.min)) + this.min) / 10f;
        this.precision = 0;
        setMessage(I18n.format("movingelevators.platform.speed").replace("$number$", Float.toString(val)));
    }

    @Override
    public void updateSlider(){
        if(this.sliderValue < 0.0F){
            this.sliderValue = 0.0F;
        }

        if(this.sliderValue > 1.0F){
            this.sliderValue = 1.0F;
        }

        float val = ((int)Math.round(this.sliderValue * (this.max - this.min)) + this.min) / 10f;

        setMessage(I18n.format("movingelevators.platform.speed").replace("$number$", Float.toString(val)));

        if(parent != null){
            parent.onChangeSliderValue(this);
        }
    }

    @Override
    public double getValue(){
        return ((int)Math.round(this.sliderValue * (this.max - this.min)) + this.min) / 10f;
    }

    @Override
    public int getValueInt(){
        return ((int)Math.round(this.sliderValue * (this.max - this.min)) + this.min);
    }

}
