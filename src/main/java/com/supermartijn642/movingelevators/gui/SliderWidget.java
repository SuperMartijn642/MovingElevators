package com.supermartijn642.movingelevators.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.ITickableWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created 09/02/2022 by SuperMartijn642
 */
public class SliderWidget extends Widget implements ITickableWidget {

    private static final ResourceLocation SLIDER_TEXTURE = new ResourceLocation("movingelevators", "textures/gui/slider.png");

    private final int min, max, range;
    private int value, lastValue;
    private final Function<Integer,ITextComponent> text;
    private final Consumer<Integer> onChange;
    private boolean dragging = false;

    public SliderWidget(int x, int y, int width, int min, int max, int startValue, Function<Integer,ITextComponent> text, Consumer<Integer> onChange){
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
    protected ITextComponent getNarrationMessage(){
        return this.text.apply(this.value);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        if(this.dragging)
            this.value = Math.min(this.max, Math.max(this.min, Math.round((float)(mouseX - this.x) / this.width * this.range) + this.min));

        ScreenUtils.bindTexture(SLIDER_TEXTURE);
        // Background
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, 1, this.height, 0, 0, 1 / 18f, 1);
        ScreenUtils.drawTexture(matrixStack, this.x + 1, this.y, this.width - 2, this.height, 1 / 18f, 0, 1 / 18f, 1);
        ScreenUtils.drawTexture(matrixStack, this.x + this.width - 1, this.y, 1, this.height, 2 / 18f, 0, 1 / 18f, 1);
        // Slider
        float percentage = (float)(this.value - this.min) / this.range;
        ScreenUtils.drawTexture(matrixStack, this.x + percentage * (this.width - 5), this.y, 5, this.height, this.active ? this.hovered || this.dragging ? 8 / 18f : 3 / 18f : 13 / 18f, 0, 5 / 18f, 1);
        // Text
        ITextComponent text = this.text.apply(this.value);
        if(text != null)
            ScreenUtils.drawCenteredStringWithShadow(matrixStack, text, this.x + this.width / 2f, this.y + 2, ScreenUtils.ACTIVE_TEXT_COLOR);
    }

    @Override
    public void tick(){
        if(this.value != this.lastValue){
            this.onChange.accept(this.value);
            this.lastValue = this.value;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button){
        if(this.hovered)
            this.dragging = true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button){
        this.dragging = false;
    }

    @Override
    public void mouseScrolled(int mouseX, int mouseY, double scroll){
        if(this.hovered){
            if(scroll > 0 && this.value < this.max)
                this.value++;
            else if(scroll < 0 && this.value > this.min)
                this.value--;
        }
    }
}
