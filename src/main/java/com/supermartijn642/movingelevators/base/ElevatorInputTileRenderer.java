package com.supermartijn642.movingelevators.base;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.DisplayBlock;
import com.supermartijn642.movingelevators.ElevatorGroup;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputTileRenderer<T extends ElevatorInputTile> extends METileRenderer<T> {

    private static final RenderType BUTTONS = getTexture("buttons");
    private static final RenderType DISPLAY_BACKGROUND = getTexture("display_overlay");
    private static final RenderType DISPLAY_BACKGROUND_BIG = getTexture("display_overlay_big");
    private static final RenderType DISPLAY_GREEN_DOT = getTexture("green_dot");
    private static final HashMap<DyeColor,RenderType> DISPLAY_BUTTONS = new HashMap<>();
    private static final HashMap<DyeColor,RenderType> DISPLAY_BUTTONS_OFF = new HashMap<>();

    static{
        for(DyeColor color : DyeColor.values()){
            DISPLAY_BUTTONS.put(color, getTexture("display_buttons/display_button_" + color.name().toLowerCase(Locale.ROOT)));
            DISPLAY_BUTTONS_OFF.put(color, getTexture("display_buttons/display_button_off_" + color.name().toLowerCase(Locale.ROOT)));
        }
    }

    private static RenderType getTexture(final String name){
        RenderType.State state = RenderType.State.getBuilder().transparency(new RenderState.TransparencyState("translucent_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableAlphaTest();
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.disableAlphaTest();
        })).texture(new RenderState.TextureState(new ResourceLocation("movingelevators", "textures/blocks/" + name + ".png"), false, false)).build(false);
        return RenderType.makeType("movingelevators_texture_" + name, DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 256, false, true, state);
    }

    public ElevatorInputTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn){
        super(rendererDispatcherIn);
    }

    @Override
    protected void render(){
        if(!tile.hasGroup() || tile.getFacing() == null)
            return;

        // render buttons
        this.renderButtons();

        // render display
        this.renderDisplay();
    }

    private void renderButtons(){
        matrixStack.push();

        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.rotate(new Quaternion(0, 180 - tile.getFacing().getHorizontalAngle(), 0, true));
        matrixStack.translate(-0.5, -0.5, -0.51);

        this.drawQuad(BUTTONS);

        matrixStack.pop();
    }

    private void renderDisplay(){
        int height = tile.getDisplayHeight();
        if(height <= 0)
            return;

        matrixStack.push();

        matrixStack.translate(0.5, 0.5 + 1, 0.5);
        matrixStack.rotate(new Quaternion(0, 180 - tile.getFacing().getHorizontalAngle(), 0, true));
        matrixStack.translate(-0.5, -0.5, -0.51);

        int button_count;
        RenderType background;
        if(height == 1){
            button_count = DisplayBlock.BUTTON_COUNT;
            background = DISPLAY_BACKGROUND;
        }else{
            button_count = DisplayBlock.BUTTON_COUNT_BIG;
            background = DISPLAY_BACKGROUND_BIG;
        }

        // render background
        matrixStack.push();
        matrixStack.scale(1, height, 1);
        this.drawQuad(background);
        matrixStack.pop();

        ElevatorGroup group = tile.getGroup();
        int index = group.getFloorNumber(tile.getFloorLevel());
        int below = index;
        int above = group.getFloorCount() - index - 1;
        if(below < above){
            below = Math.min(below, button_count);
            above = Math.min(above, button_count * 2 - below);
        }else{
            above = Math.min(above, button_count);
            below = Math.min(below, button_count * 2 - above);
        }
        int startIndex = index - below;
        int total = below + 1 + above;

        // render buttons
        matrixStack.push();
        matrixStack.translate(0, 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, -0.002);
        matrixStack.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < total; i++){
            this.drawQuad((startIndex + i == index ? DISPLAY_BUTTONS_OFF : DISPLAY_BUTTONS).get(group.getFloorDisplayColor(startIndex + i)));
            matrixStack.push();
            matrixStack.translate(18.5 / 32d, 0, 0);
            this.drawString(ClientProxy.formatFloorDisplayName(group.getFloorDisplayName(startIndex + i), startIndex + i));
            matrixStack.pop();
            matrixStack.translate(0, 1, 0);
        }
        matrixStack.pop();

        // render platform dot
        if(tile.getGroup().isMoving()){
            double platformY = tile.getGroup().getCurrentY();
            if(platformY >= group.getFloorYLevel(0) && platformY < group.getFloorYLevel(group.getFloorCount() - 1)){
                double yOffset = 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d;
                for(int i = 0; i < group.getFloorCount() - 1; i++){
                    int belowY = group.getFloorYLevel(i);
                    int aboveY = group.getFloorYLevel(i + 1);
                    if(platformY >= belowY && platformY < aboveY)
                        yOffset += (i + (platformY - belowY) / (aboveY - belowY)) * DisplayBlock.BUTTON_HEIGHT;
                }
                matrixStack.translate(1 - (27.5 / 32d + DisplayBlock.BUTTON_HEIGHT / 2d), yOffset, -0.003);
                matrixStack.scale(DisplayBlock.BUTTON_HEIGHT, DisplayBlock.BUTTON_HEIGHT, 1);
                this.drawQuad(DISPLAY_GREEN_DOT);
            }
        }

        matrixStack.pop();
    }

    private void drawQuad(RenderType type){
        Matrix4f matrix = matrixStack.getLast().getMatrix();
        IVertexBuilder builder = buffer.getBuffer(type);

        builder.pos(matrix, 0, 0, 0).tex(1, 1).endVertex();
        builder.pos(matrix, 0, 1, 0).tex(1, 0).endVertex();
        builder.pos(matrix, 1, 1, 0).tex(0, 0).endVertex();
        builder.pos(matrix, 1, 0, 0).tex(0, 1).endVertex();
    }

    private void drawString(String s){
        if(s == null)
            return;
        FontRenderer fontRenderer = this.renderDispatcher.fontRenderer;
        matrixStack.push();
        matrixStack.translate(0, 0.07, -0.005);
        matrixStack.scale(-0.01f, -0.08f, 1);
        fontRenderer.renderString(s, -fontRenderer.getStringWidth(s) / 2f, -fontRenderer.FONT_HEIGHT, NativeImage.getCombined(255, 255, 255, 255), true, matrixStack.getLast().getMatrix(), buffer, false, 0, combinedLight);
        matrixStack.pop();
    }

    @Override
    public boolean isGlobalRenderer(T te){
        return true;
    }
}
