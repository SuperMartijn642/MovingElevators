package com.supermartijn642.movingelevators.base;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.movingelevators.DisplayBlock;
import com.supermartijn642.movingelevators.ElevatorBlockTile;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            DISPLAY_BUTTONS.put(color, getTexture("display_buttons/display_button_" + color.name().toLowerCase()));
            DISPLAY_BUTTONS_OFF.put(color, getTexture("display_buttons/display_button_off_" + color.name().toLowerCase()));
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
        if(!tile.hasGroup())
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

        List<ElevatorBlockTile> allTiles = tile.getGroup().getTiles();
        int index = tile.getGroup().getFloorNumber(tile.getFloorLevel());
        ArrayList<ElevatorBlockTile> belowTiles = new ArrayList<>();
        for(int a = index - 1; a >= 0 && belowTiles.size() < button_count; a--)
            belowTiles.add(allTiles.get(a));
        ArrayList<ElevatorBlockTile> aboveTiles = new ArrayList<>();
        for(int a = index + 1; a < allTiles.size() && aboveTiles.size() < button_count; a++)
            aboveTiles.add(allTiles.get(a));

        // render center button
        matrixStack.translate(0, 0.5 * height - DisplayBlock.BUTTON_HEIGHT / 2, -0.002);
        matrixStack.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        this.drawQuad(DISPLAY_BUTTONS_OFF.get(this.tile.getDisplayLabelColor()));
        matrixStack.push();
        matrixStack.translate(18.5 / 32d, 0, 0);
        this.drawString(tile.getFloorName());
        matrixStack.pop();

        // render bottom buttons
        matrixStack.push();
        for(int i = 0; i < belowTiles.size(); i++){
            matrixStack.translate(0, -1, 0);
            this.drawQuad(DISPLAY_BUTTONS.get(belowTiles.get(i).getDisplayLabelColor()));
            matrixStack.push();
            matrixStack.translate(18.5 / 32d, 0, 0);
            this.drawString(belowTiles.get(i).getFloorName());
            matrixStack.pop();
        }
        matrixStack.pop();

        // render top buttons
        matrixStack.push();
        for(int i = 0; i < aboveTiles.size(); i++){
            matrixStack.translate(0, 1, 0);
            this.drawQuad(DISPLAY_BUTTONS.get(aboveTiles.get(i).getDisplayLabelColor()));
            matrixStack.push();
            matrixStack.translate(18.5 / 32d, 0, 0);
            this.drawString(aboveTiles.get(i).getFloorName());
            matrixStack.pop();
        }
        matrixStack.pop();

        // render platform dot
        if(tile.getGroup().isMoving()){
            double platformY = tile.getGroup().getCurrentY();
            if((aboveTiles.size() == 0 ? tile.getPos().getY() : aboveTiles.get(aboveTiles.size() - 1).getPos().getY()) >= platformY
                && (belowTiles.size() == 0 ? tile.getPos().getY() : belowTiles.get(belowTiles.size() - 1).getPos().getY()) <= platformY){
                double yOffset = 0;
                if(platformY == tile.getPos().getY())
                    yOffset = 0;
                else if(platformY < tile.getPos().getY()){
                    int steps = 0;
                    int lastY = tile.getPos().getY();
                    for(ElevatorBlockTile tile2 : belowTiles){
                        int y = tile2.getPos().getY();
                        if(y == platformY){
                            yOffset = -(steps + 1);
                            break;
                        }
                        if(y < platformY){
                            yOffset = -(steps + (lastY - platformY) / (lastY - y));
                            break;
                        }
                        steps++;
                        lastY = tile2.getPos().getY();
                    }
                }else{
                    int steps = 0;
                    int lastY = tile.getPos().getY();
                    for(ElevatorBlockTile tile2 : aboveTiles){
                        int y = tile2.getPos().getY();
                        if(y == platformY){
                            yOffset = steps + 1;
                            break;
                        }
                        if(y > platformY){
                            yOffset = steps + (platformY - lastY) / (y - lastY);
                            break;
                        }
                        steps++;
                        lastY = tile2.getPos().getY();
                    }
                }
                matrixStack.translate(1 - (27.5 / 32d + DisplayBlock.BUTTON_HEIGHT / 2d), yOffset, -0.002);
                matrixStack.scale(DisplayBlock.BUTTON_HEIGHT, 1, 1);
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
        FontRenderer fontRenderer = this.renderDispatcher.fontRenderer;
        matrixStack.push();
        matrixStack.translate(0, 0.07, -0.005);
        matrixStack.scale(-0.01f, -0.08f, 1);
        fontRenderer.renderString(s, -fontRenderer.getStringWidth(s) / 2f, -fontRenderer.FONT_HEIGHT, NativeImage.getCombined(255, 255, 255, 255), true, matrixStack.getLast().getMatrix(), buffer, false, 0, Integer.MAX_VALUE);
        matrixStack.pop();
    }

    @Override
    public boolean isGlobalRenderer(T te){
        return true;
    }
}
