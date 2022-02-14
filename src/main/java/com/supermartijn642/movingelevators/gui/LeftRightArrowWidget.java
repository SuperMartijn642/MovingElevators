package com.supermartijn642.movingelevators.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Created 05/02/2022 by SuperMartijn642
 */
public class LeftRightArrowWidget extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation ARROW_BUTTONS = new ResourceLocation("movingelevators", "textures/gui/arrow_buttons.png");

    private final boolean isLeft;
    private final ITextComponent hoverText;
    private final Supplier<Boolean> isActive;

    public LeftRightArrowWidget(int x, int y, boolean isLeft, ITextComponent hoverText, Supplier<Boolean> isActive, Runnable onPress){
        super(x, y, 7, 11, onPress);
        this.isLeft = isLeft;
        this.hoverText = hoverText;
        this.isActive = isActive;
    }

    @Override
    public ITextComponent getHoverText(){
        return this.hoverText;
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.hoverText;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.active = this.isActive.get();
        GlStateManager._enableAlphaTest();
        ScreenUtils.bindTexture(ARROW_BUTTONS);
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, this.width, this.height, this.isLeft ? 0 : 1 / 2f, this.active ? this.hovered ? 1 / 3f : 0 : 2 / 3f, 1 / 2f, 1 / 3f);
    }
}
