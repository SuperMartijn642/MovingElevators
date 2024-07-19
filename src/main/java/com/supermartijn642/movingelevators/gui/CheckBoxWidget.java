package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.core.util.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 10/02/2022 by SuperMartijn642
 */
public class CheckBoxWidget extends AbstractButtonWidget {

    private static final ResourceLocation CHECKMARK_BOX_TEXTURE = ResourceLocation.fromNamespaceAndPath("movingelevators", "textures/gui/checkmark_box.png");

    private final Function<Boolean,Component> hoverText;
    private final Supplier<Boolean> isChecked;

    public boolean active = true;

    public CheckBoxWidget(int x, int y, Function<Boolean,Component> hoverText, Supplier<Boolean> isChecked, Consumer<Boolean> onPress){
        super(x, y, 11, 11, () -> onPress.accept(isChecked.get()));
        this.hoverText = hoverText;
        this.isChecked = isChecked;
    }

    @Override
    public Component getNarrationMessage(){
        Holder<Component> message = new Holder<>();
        this.getTooltips(message::set);
        return message.get();
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(this.hoverText.apply(this.isChecked.get()));
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        ScreenUtils.bindTexture(CHECKMARK_BOX_TEXTURE);
        ScreenUtils.drawTexture(context.poseStack(), this.x, this.y, this.width + 1, this.height, this.isChecked.get() ? 0 : 1 / 2f, this.active ? this.isFocused() ? 1 / 3f : 0 : 2 / 3f, 1 / 2f, 1 / 3f);
    }
}
