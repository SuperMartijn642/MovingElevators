package com.supermartijn642.movingelevators.base;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.supermartijn642.movingelevators.ClientProxy;
import com.supermartijn642.movingelevators.DisplayBlock;
import com.supermartijn642.movingelevators.ElevatorGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

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

    private static final double TEXT_RENDER_DISTANCE = 15 * 15;

    static{
        for(DyeColor color : DyeColor.values()){
            DISPLAY_BUTTONS.put(color, getTexture("display_buttons/display_button_" + color.name().toLowerCase(Locale.ROOT)));
            DISPLAY_BUTTONS_OFF.put(color, getTexture("display_buttons/display_button_off_" + color.name().toLowerCase(Locale.ROOT)));
        }
    }

    private static RenderType getTexture(final String name){
        RenderType.CompositeState state = RenderType.CompositeState.builder().setShaderState(RenderTypeExtension.getPositionTexShader()).setTransparencyState(RenderTypeExtension.getTranslucentTransparency()).setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation("movingelevators", "textures/blocks/" + name + ".png"), false, false)).createCompositeState(false);
        return RenderType.create("movingelevators_texture_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, true, state);
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
        matrixStack.pushPose();

        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.mulPose(new Quaternion(0, 180 - tile.getFacing().toYRot(), 0, true));
        matrixStack.translate(-0.5, -0.5, -0.51);

        this.drawQuad(BUTTONS);

        matrixStack.popPose();
    }

    private void renderDisplay(){
        int height = tile.getDisplayHeight();
        if(height <= 0)
            return;

        matrixStack.pushPose();

        matrixStack.translate(0.5, 0.5 + 1, 0.5);
        matrixStack.mulPose(new Quaternion(0, 180 - tile.getFacing().toYRot(), 0, true));
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
        matrixStack.pushPose();
        matrixStack.scale(1, height, 1);
        this.drawQuad(background);
        matrixStack.popPose();

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
        Vec3 buttonPos = new Vec3(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 1 + 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, tile.getBlockPos().getZ() + 0.5);
        Vec3 cameraPos = Minecraft.getInstance().cameraEntity.getEyePosition(partialTicks);
        matrixStack.pushPose();
        matrixStack.translate(0, 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, -0.002);
        matrixStack.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < total; i++){
            this.drawQuad((startIndex + i == index ? DISPLAY_BUTTONS_OFF : DISPLAY_BUTTONS).get(group.getFloorDisplayColor(startIndex + i)));
            boolean drawText = cameraPos.distanceToSqr(buttonPos) < TEXT_RENDER_DISTANCE; // text rendering is VERY slow apparently, so only draw it within a certain distance
            if(drawText){
                matrixStack.pushPose();
                matrixStack.translate(18.5 / 32d, 0, 0);
                this.drawString(ClientProxy.formatFloorDisplayName(group.getFloorDisplayName(startIndex + i), startIndex + i));
                matrixStack.popPose();
            }
            matrixStack.translate(0, 1, 0);
            buttonPos = buttonPos.add(0, DisplayBlock.BUTTON_HEIGHT, 0);
        }
        matrixStack.popPose();

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

        matrixStack.popPose();
    }

    private void drawQuad(RenderType type){
        Matrix4f matrix = matrixStack.last().pose();
        VertexConsumer builder = buffer.getBuffer(type);

        builder.vertex(matrix, 0, 0, 0).uv(1, 1).endVertex();
        builder.vertex(matrix, 0, 1, 0).uv(1, 0).endVertex();
        builder.vertex(matrix, 1, 1, 0).uv(0, 0).endVertex();
        builder.vertex(matrix, 1, 0, 0).uv(0, 1).endVertex();
    }

    private void drawString(String s){
        if(s == null)
            return;
        Font fontRenderer = Minecraft.getInstance().font;
        matrixStack.pushPose();
        matrixStack.translate(0, 0.07, -0.005);
        matrixStack.scale(-0.01f, -0.08f, 1);
        fontRenderer.drawInBatch(s, -fontRenderer.width(s) / 2f, -fontRenderer.lineHeight, NativeImage.combine(255, 255, 255, 255), true, matrixStack.last().pose(), buffer, false, 0, combinedLight);
        matrixStack.popPose();
    }

    private static class RenderTypeExtension extends RenderStateShard {

        public RenderTypeExtension(String p_110161_, Runnable p_110162_, Runnable p_110163_){
            super(p_110161_, p_110162_, p_110163_);
        }

        public static TransparencyStateShard getTranslucentTransparency(){
            return RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        }

        public static ShaderStateShard getPositionTexShader(){
            return ShaderStateShard.POSITION_TEX_SHADER;
        }
    }
}
