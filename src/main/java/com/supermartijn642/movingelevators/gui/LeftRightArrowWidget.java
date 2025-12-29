package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 05/02/2022 by SuperMartijn642
 */
public class LeftRightArrowWidget extends AbstractButtonWidget {

    public static final Identifier ARROW_BUTTONS = Identifier.fromNamespaceAndPath("movingelevators", "gui/arrow_buttons");

    private final boolean isLeft;
    private final Component hoverText;
    private final Supplier<Boolean> isActive;

    public boolean active = true;

    public LeftRightArrowWidget(int x, int y, boolean isLeft, Component hoverText, Supplier<Boolean> isActive, Runnable onPress){
        super(x, y, 7, 11, onPress);
        this.isLeft = isLeft;
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
        graphics.submitSprite(ARROW_BUTTONS, this.x, this.y, this.width, this.height, p -> p.uv(this.isLeft ? 0 : 1 / 2f, this.active ? this.isFocused() ? 1 / 3f : 0 : 2 / 3f, 1 / 2f, 1 / 3f));
    }
}
