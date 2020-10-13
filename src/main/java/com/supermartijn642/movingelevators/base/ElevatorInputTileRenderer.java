package com.supermartijn642.movingelevators.base;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.movingelevators.DisplayBlock;
import com.supermartijn642.movingelevators.ElevatorBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputTileRenderer<T extends ElevatorInputTile> extends METileRenderer<T> {

    private static final ResourceLocation BUTTONS = getTexture("buttons");
    private static final ResourceLocation DISPLAY_BACKGROUND = getTexture("display_overlay");
    private static final ResourceLocation DISPLAY_BACKGROUND_BIG = getTexture("display_overlay_big");
    private static final ResourceLocation DISPLAY_GREEN_DOT = getTexture("green_dot");
    private static final HashMap<DyeColor,ResourceLocation> DISPLAY_BUTTONS = new HashMap<>();
    private static final HashMap<DyeColor,ResourceLocation> DISPLAY_BUTTONS_OFF = new HashMap<>();

    static{
        for(DyeColor color : DyeColor.values()){
            DISPLAY_BUTTONS.put(color, getTexture("display_buttons/display_button_" + color.name().toLowerCase()));
            DISPLAY_BUTTONS_OFF.put(color, getTexture("display_buttons/display_button_off_" + color.name().toLowerCase()));
        }
    }

    private static ResourceLocation getTexture(String name){
        return new ResourceLocation("movingelevators", "textures/blocks/" + name + ".png");
    }

    public ElevatorInputTileRenderer(){
        super();
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
        GlStateManager.pushMatrix();

        GlStateManager.translated(x, y, z);

        GlStateManager.translated(0.5, 0.5, 0.5);
        GlStateManager.rotated(180 - tile.getFacing().getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translated(-0.5, -0.5, -0.51);

        this.drawQuad(BUTTONS, tile.getPos());

        GlStateManager.popMatrix();
    }

    private void renderDisplay(){
        int height = tile.getDisplayHeight();
        if(height <= 0)
            return;

        GlStateManager.pushMatrix();

        GlStateManager.translated(x, y, z);

        GlStateManager.translated(0.5, 0.5 + 1, 0.5);
        GlStateManager.rotated(180 - tile.getFacing().getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translated(-0.5, -0.5, -0.51);

        int button_count;
        ResourceLocation background;
        if(height == 1){
            button_count = DisplayBlock.BUTTON_COUNT;
            background = DISPLAY_BACKGROUND;
        }else{
            button_count = DisplayBlock.BUTTON_COUNT_BIG;
            background = DISPLAY_BACKGROUND_BIG;
        }

        // render background
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1, height, 1);
        this.drawQuad(background, tile.getPos().up());
        GlStateManager.popMatrix();

        List<ElevatorBlockTile> allTiles = tile.getGroup().getTiles();
        int index = tile.getGroup().getFloorNumber(tile.getFloorLevel());
        int below = index;
        int above = allTiles.size() - index - 1;
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
        GlStateManager.pushMatrix();
        GlStateManager.translated(0, 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, -0.002);
        GlStateManager.scaled(1, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < total; i++){
            this.drawQuad((startIndex + i == index ? DISPLAY_BUTTONS_OFF : DISPLAY_BUTTONS).get(allTiles.get(startIndex + i).getDisplayLabelColor()), tile.getPos().up());
            GlStateManager.pushMatrix();
            GlStateManager.translated(18.5 / 32d, 0, 0);
            this.drawString(allTiles.get(startIndex + i).getFloorName());
            GlStateManager.popMatrix();
            GlStateManager.translated(0, 1, 0);
        }
        GlStateManager.popMatrix();

        // render platform dot
        if(tile.getGroup().isMoving()){
            double platformY = tile.getGroup().getCurrentY();
            if(platformY >= allTiles.get(0).getPos().getY() && platformY < allTiles.get(allTiles.size() - 1).getPos().getY()){
                double yOffset = 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d;
                for(int i = 0; i < allTiles.size() - 1; i++){
                    int belowY = allTiles.get(i).getPos().getY();
                    int aboveY = allTiles.get(i + 1).getPos().getY();
                    if(platformY >= belowY && platformY < aboveY)
                        yOffset += (i + (platformY - belowY) / (aboveY - belowY)) * DisplayBlock.BUTTON_HEIGHT;
                }
                GlStateManager.translated(1 - (27.5 / 32d + DisplayBlock.BUTTON_HEIGHT / 2d), yOffset, -0.003);
                GlStateManager.scalef(DisplayBlock.BUTTON_HEIGHT, DisplayBlock.BUTTON_HEIGHT, 1);
                this.drawQuad(DISPLAY_GREEN_DOT, tile.getPos().up());
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawQuad(ResourceLocation texture, BlockPos pos){
        GlStateManager.pushMatrix();

        Minecraft.getInstance().getTextureManager().bindTexture(texture);

        int i = Minecraft.getInstance().world.getCombinedLight(pos.offset(tile.getFacing()), 0);
        int j = i % 65536;
        int k = i / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        GlStateManager.disableLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.polygonOffset(-1, -1);

        builder.pos(0, 0, 0).tex(1, 1).endVertex();
        builder.pos(0, 1, 0).tex(1, 0).endVertex();
        builder.pos(1, 1, 0).tex(0, 0).endVertex();
        builder.pos(1, 0, 0).tex(0, 1).endVertex();

        tessellator.draw();

        GlStateManager.disablePolygonOffset();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    private void drawString(String s){
        if(s == null)
            return;
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.translated(0, 0.07, -0.005);
        GlStateManager.scalef(-0.01f, -0.08f, 1);

        GlStateManager.disableLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.polygonOffset(-1, -1);

        fontRenderer.drawStringWithShadow(s, -fontRenderer.getStringWidth(s) / 2f, -fontRenderer.FONT_HEIGHT, DyeColor.WHITE.getTextColor());

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(T te){
        return true;
    }
}
