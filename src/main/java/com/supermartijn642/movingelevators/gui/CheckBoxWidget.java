package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 10/02/2022 by SuperMartijn642
 */
public class CheckBoxWidget extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation CHECKMARK_BOX_TEXTURE = new ResourceLocation("movingelevators", "textures/gui/checkmark_box.png");

    private final Function<Boolean,ITextComponent> hoverText;
    private final Supplier<Boolean> isChecked;

    public CheckBoxWidget(int x, int y, Function<Boolean,ITextComponent> hoverText, Supplier<Boolean> isChecked, Consumer<Boolean> onPress){
        super(x, y, 11, 11, () -> onPress.accept(isChecked.get()));
        this.hoverText = hoverText;
        this.isChecked = isChecked;
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.getHoverText();
    }

    @Override
    public ITextComponent getHoverText(){
        return this.hoverText.apply(this.isChecked.get());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        GlStateManager.enableAlpha();
        ScreenUtils.bindTexture(CHECKMARK_BOX_TEXTURE);
        ScreenUtils.drawTexture(this.x, this.y, this.width + 1, this.height, this.isChecked.get() ? 0 : 1 / 2f, this.active ? this.hovered ? 1 / 3f : 0 : 2 / 3f, 1 / 2f, 1 / 3f);
    }
}
