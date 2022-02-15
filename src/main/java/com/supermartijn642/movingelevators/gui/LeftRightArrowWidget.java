package com.supermartijn642.movingelevators.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Created 05/02/2022 by SuperMartijn642
 */
public class LeftRightArrowWidget extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation ARROW_BUTTONS = new ResourceLocation("movingelevators", "textures/gui/arrow_buttons.png");

    private final boolean isLeft;
    private final Component hoverText;
    private final Supplier<Boolean> isActive;

    public LeftRightArrowWidget(int x, int y, boolean isLeft, Component hoverText, Supplier<Boolean> isActive, Runnable onPress){
        super(x, y, 7, 11, onPress);
        this.isLeft = isLeft;
        this.hoverText = hoverText;
        this.isActive = isActive;
    }

    @Override
    public Component getHoverText(){
        return this.hoverText;
    }

    @Override
    protected Component getNarrationMessage(){
        return this.hoverText;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.active = this.isActive.get();
        ScreenUtils.bindTexture(ARROW_BUTTONS);
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, this.width, this.height, this.isLeft ? 0 : 1 / 2f, this.active ? this.hovered ? 1 / 3f : 0 : 2 / 3f, 1 / 2f, 1 / 3f);
    }
}
