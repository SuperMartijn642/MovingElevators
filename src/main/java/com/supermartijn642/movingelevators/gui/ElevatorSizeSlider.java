package com.supermartijn642.movingelevators.gui;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.resources.I18n;

import java.util.function.Consumer;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorSizeSlider extends AbstractSlider {

    private final Consumer<ElevatorSizeSlider> onChange;

    public ElevatorSizeSlider(int xIn, int yIn, int widthIn, int heightIn, int currentValue, Consumer<ElevatorSizeSlider> onChange){
        super(null, xIn, yIn, widthIn, heightIn, (currentValue - 1) / 2 / 4f);
        this.onChange = onChange;
        this.updateMessage();
    }

    protected void applyValue(){
        this.onChange.accept(this);
    }

    protected void updateMessage(){
        int val = this.getValue();
        this.setMessage(I18n.format("movingelevators.platform.size").replace("$number$", val + "x" + val));
    }

    public int getValue(){
        return (int)Math.round(this.value * 4) * 2 + 1;
    }
}
