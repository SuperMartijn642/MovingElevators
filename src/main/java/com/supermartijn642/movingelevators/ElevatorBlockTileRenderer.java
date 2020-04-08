package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTileRenderer extends METileRenderer<ElevatorBlockTile> {

    private static final RenderType BUTTONS = getTexture("buttons");
    private static final RenderType DISPLAY_BACKGROUND = getTexture("display_overlay");
    private static final RenderType DISPLAY_BACKGROUND_BIG = getTexture("display_overlay_big");
    private static final RenderType DISPLAY_GREEN_DOT = getTexture("green_dot");
    private static final RenderType DISPLAY_BUTTON = getTexture("display_button");
    private static final RenderType DISPLAY_BUTTON_OFF = getTexture("display_button_off");

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

    public ElevatorBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn){
        super(rendererDispatcherIn);
    }

    @Override
    protected void render(){
        if(!tile.hasGroup())
            return;

        // render buttons
        this.renderButtons();

        // render platform
        this.renderPlatform();

        // render display
        this.renderDisplay();
    }

    private void renderButtons(){
        matrixStack.push();

        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.rotate(new Quaternion(0, 180 - tile.getFacing().getHorizontalAngle(), 0, true));
        matrixStack.translate(-0.502, -0.502, -0.502);

        this.drawQuad(BUTTONS);

        matrixStack.pop();
    }

    private void renderPlatform(){
        if(tile.getGroup().getLowest() != tile.getPos().getY() || !tile.getGroup().isMoving() || tile.getGroup().getCurrentY() == tile.getGroup().getLastY())
            return;
        BlockState[][] state = tile.getGroup().getPlatform();
        int size = tile.getGroup().getSize();
        double lastY = tile.getGroup().getLastY(), currentY = tile.getGroup().getCurrentY();
        double y = lastY + (currentY - lastY) * partialTicks - tile.getPos().getY();
        int startX = tile.getFacing().getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = tile.getFacing().getZOffset() * (int)Math.ceil(size / 2f) - size / 2;

        BlockPos topPos = tile.getPos().offset(tile.getFacing(), (int)Math.ceil(size / 2f)).add(0, y, 0);
        int currentLight = WorldRenderer.getCombinedLight(tile.getWorld(), topPos);

        for(int x = 0; x < size; x++){
            for(int z = 0; z < size; z++){
                matrixStack.push();

                matrixStack.translate(startX + x, y, startZ + z);

                Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state[x][z], matrixStack, buffer, currentLight, combinedOverlay, EmptyModelData.INSTANCE);

                matrixStack.pop();
            }
        }
    }

    private void renderDisplay(){
        int height = tile.getDisplayHeight();
        if(height <= 0)
            return;

        matrixStack.push();

        matrixStack.translate(0.5, 0.5 + 1, 0.5);
        matrixStack.rotate(new Quaternion(0, 180 - tile.getFacing().getHorizontalAngle(), 0, true));
        matrixStack.translate(-0.5, -0.5, -0.502);

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
        int index = tile.getGroup().getTiles().indexOf(tile);
        ArrayList<ElevatorBlockTile> belowTiles = new ArrayList<>();
        for(int a = index - 1; a >= 0 && belowTiles.size() < button_count; a--)
            belowTiles.add(allTiles.get(a));
        ArrayList<ElevatorBlockTile> aboveTiles = new ArrayList<>();
        for(int a = index + 1; a < allTiles.size() && aboveTiles.size() < button_count; a++)
            aboveTiles.add(allTiles.get(a));

        // render center button
        matrixStack.translate(0, 0.5 * height - DisplayBlock.BUTTON_HEIGHT / 2, -0.002);
        matrixStack.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        this.drawQuad(DISPLAY_BUTTON_OFF);
        matrixStack.push();
        matrixStack.translate(18.5 / 32d, 0, 0);
        this.drawString(tile.getName());
        matrixStack.pop();

        // render bottom buttons
        matrixStack.push();
        for(int i = 0; i < belowTiles.size(); i++){
            matrixStack.translate(0, -1, 0);
            this.drawQuad(DISPLAY_BUTTON);
            matrixStack.push();
            matrixStack.translate(18.5 / 32d, 0, 0);
            this.drawString(belowTiles.get(i).getName());
            matrixStack.pop();
        }
        matrixStack.pop();

        // render top buttons
        matrixStack.push();
        for(int i = 0; i < aboveTiles.size(); i++){
            matrixStack.translate(0, 1, 0);
            this.drawQuad(DISPLAY_BUTTON);
            matrixStack.push();
            matrixStack.translate(18.5 / 32d, 0, 0);
            this.drawString(aboveTiles.get(i).getName());
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
        matrixStack.translate(0, 0.07, -0.002);
        matrixStack.scale(-0.01f, -0.08f, 1);
        fontRenderer.renderString(s, -fontRenderer.getStringWidth(s) / 2f, -fontRenderer.FONT_HEIGHT, NativeImage.getCombined(255, 255, 255, 255), true, matrixStack.getLast().getMatrix(), buffer, false, 0, Integer.MAX_VALUE);
        matrixStack.pop();
    }

    @Override
    public boolean isGlobalRenderer(ElevatorBlockTile te){
        return true;
    }
}
