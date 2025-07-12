package com.supermartijn642.movingelevators.generators;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.movingelevators.gui.*;

/**
 * Created 07/07/2025 by SuperMartijn642
 */
public class MovingElevatorsAtlasSourceGenerator extends AtlasSourceGenerator {

    public MovingElevatorsAtlasSourceGenerator(ResourceCache cache){
        super("movingelevators", cache);
    }

    @Override
    public void generate(){
        this.guiAtlas()
            .texture(CheckBoxWidget.CHECKMARK_BOX_TEXTURE)
            .texture(ElevatorScreen.BACKGROUND)
            .texture(ElevatorScreen.SIZE_ICONS)
            .texture(LeftRightArrowWidget.ARROW_BUTTONS)
            .texture(PlusMinusButtonWidget.PLUS_MINUS_BUTTONS)
            .texture(SliderWidget.SLIDER_TEXTURE);
    }
}
