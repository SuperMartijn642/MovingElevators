package com.supermartijn642.movingelevators.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorSizeSlider extends Slider {

    public ElevatorSizeSlider(int xPos, int yPos, int width, int height, int currentVal, ISlider slider){
        super(xPos, yPos, width, height, new StringTextComponent(""), new StringTextComponent(""), 0, 4, (currentVal - 1) / 2, false, true, b -> {
        }, slider);
        int val = (int)Math.round(sliderValue * (maxValue - minValue)) * 2 + 1;
        precision = 0;
        func_238482_a_(new StringTextComponent(I18n.format("movingelevators.platform.size").replace("$number$", val + "x" + val)));
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

        func_238482_a_(new StringTextComponent(I18n.format("movingelevators.platform.size").replace("$number$", val + "x" + val)));

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
