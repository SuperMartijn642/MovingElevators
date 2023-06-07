package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created 09/02/2022 by SuperMartijn642
 */
public class SliderWidget extends BaseWidget {

    private static final ResourceLocation SLIDER_TEXTURE = new ResourceLocation("movingelevators", "textures/gui/slider.png");

    private final int min, max, range;
    private int value, lastValue;
    private final Function<Integer,Component> text;
    private final Consumer<Integer> onChange;
    private boolean dragging = false;

    public boolean active = true;

    public SliderWidget(int x, int y, int width, int min, int max, int startValue, Function<Integer,Component> text, Consumer<Integer> onChange){
        super(x, y, width, 11);
        if(max < min)
            throw new IllegalArgumentException("Maximum must be greater than the minimum!");
        if(startValue < min || startValue > max)
            throw new IllegalArgumentException("Start value must be between the minimum and maximum!");

        this.min = min;
        this.max = max;
        this.range = max - min;
        this.value = this.lastValue = startValue;
        this.text = text;
        this.onChange = onChange;
    }

    @Override
    public Component getNarrationMessage(){
        return this.text.apply(this.value);
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        if(this.dragging)
            this.value = Math.min(this.max, Math.max(this.min, Math.round((float)(mouseX - this.x) / this.width * this.range) + this.min));

        ScreenUtils.bindTexture(SLIDER_TEXTURE);
        // Background
        ScreenUtils.drawTexture(context.poseStack(), this.x, this.y, 1, this.height, 0, 0, 1 / 18f, 1);
        ScreenUtils.drawTexture(context.poseStack(), this.x + 1, this.y, this.width - 2, this.height, 1 / 18f, 0, 1 / 18f, 1);
        ScreenUtils.drawTexture(context.poseStack(), this.x + this.width - 1, this.y, 1, this.height, 2 / 18f, 0, 1 / 18f, 1);
        // Slider
        float percentage = (float)(this.value - this.min) / this.range;
        ScreenUtils.drawTexture(context.poseStack(), this.x + percentage * (this.width - 5), this.y, 5, this.height, this.active ? this.isFocused() || this.dragging ? 8 / 18f : 3 / 18f : 13 / 18f, 0, 5 / 18f, 1);
        // Text
        Component text = this.text.apply(this.value);
        if(text != null)
            ScreenUtils.drawCenteredStringWithShadow(context.poseStack(), text, this.x + this.width / 2f, this.y + 2, ScreenUtils.ACTIVE_TEXT_COLOR);
    }

    @Override
    public void update(){
        super.update();
        if(this.value != this.lastValue){
            this.onChange.accept(this.value);
            this.lastValue = this.value;
        }
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(!hasBeenHandled && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height){
            this.dragging = true;
            return true;
        }
        return super.mousePressed(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(this.dragging){
            this.dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled){
        if(!hasBeenHandled && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height){
            if(scrollAmount > 0 && this.value < this.max)
                this.value++;
            else if(scrollAmount < 0 && this.value > this.min)
                this.value--;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled);
    }
}
