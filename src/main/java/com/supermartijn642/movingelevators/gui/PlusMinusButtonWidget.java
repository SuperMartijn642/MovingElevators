package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 05/02/2022 by SuperMartijn642
 */
public class PlusMinusButtonWidget extends AbstractButtonWidget {

    public static final ResourceLocation PLUS_MINUS_BUTTONS = ResourceLocation.fromNamespaceAndPath("movingelevators", "gui/plus_minus_buttons");

    private final boolean isPlus;
    private final Component hoverText;
    private final Supplier<Boolean> isActive;

    public boolean active = true;

    public PlusMinusButtonWidget(int x, int y, boolean isPlus, Component hoverText, Supplier<Boolean> isActive, Runnable onPress){
        super(x, y, 11, 11, onPress);
        this.isPlus = isPlus;
        this.hoverText = hoverText;
        this.isActive = isActive;
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(this.hoverText);
    }

    @Override
    public Component getNarrationMessage(){
        return this.hoverText;
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        this.active = this.isActive.get();
        graphics.submitSprite(PLUS_MINUS_BUTTONS, this.x, this.y, this.width, this.height, p -> p.uv(this.isPlus ? 0 : 1 / 2f, this.active ? this.isFocused() ? 1 / 3f : 0 : 2 / 3f, 1 / 2f, 1 / 3f));
    }
}
