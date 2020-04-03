package com.supermartijn642.movingelevators.gui;

import net.minecraftforge.fml.client.gui.widget.Slider;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorSizeSlider extends Slider {

    public ElevatorSizeSlider(int xPos, int yPos, int width, int height, int currentVal, ISlider slider){
        super(xPos, yPos, width, height, "Platform size: ", " blocks", 0, 4, (currentVal - 1) / 2, false, true, b -> {}, slider);
        int val = (int)Math.round(sliderValue * (maxValue - minValue)) * 2 + 1;
        precision = 0;
        setMessage(dispString + val + "x" + val + suffix);
    }

    @Override
    public void updateSlider(){
        if(this.sliderValue < 0.0F){
            this.sliderValue = 0.0F;
        }

        if(this.sliderValue > 1.0F){
            this.sliderValue = 1.0F;
        }

        int val = (int)Math.round(sliderValue * (maxValue - minValue)) * 2 + 1;

        setMessage(dispString + val + "x" + val + suffix);

        if(parent != null){
            parent.onChangeSliderValue(this);
        }
    }

    @Override
    public double getValue(){
        return this.getValueInt();
    }

    @Override
    public int getValueInt(){
        return (int)Math.round(sliderValue * (maxValue - minValue)) * 2 + 1;
    }
}
