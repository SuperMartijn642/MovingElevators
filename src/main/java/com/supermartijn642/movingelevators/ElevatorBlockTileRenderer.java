package com.supermartijn642.movingelevators;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlockTileRenderer extends METileRenderer<ElevatorBlockTile> {

    private static final ResourceLocation BUTTONS = getTexture("buttons");
    private static final ResourceLocation DISPLAY_BACKGROUND = getTexture("display_overlay");
    private static final ResourceLocation DISPLAY_BACKGROUND_BIG = getTexture("display_overlay_big");
    private static final ResourceLocation DISPLAY_GREEN_DOT = getTexture("green_dot");
    private static final HashMap<EnumDyeColor,ResourceLocation> DISPLAY_BUTTONS = new HashMap<>();
    private static final HashMap<EnumDyeColor,ResourceLocation> DISPLAY_BUTTONS_OFF = new HashMap<>();

    static{
        for(EnumDyeColor color : EnumDyeColor.values()){
            DISPLAY_BUTTONS.put(color, getTexture("display_buttons/display_button_" + color.name().toLowerCase()));
            DISPLAY_BUTTONS_OFF.put(color, getTexture("display_buttons/display_button_off_" + color.name().toLowerCase()));
        }
    }

    private static ResourceLocation getTexture(String name){
        return new ResourceLocation("movingelevators", "textures/blocks/" + name + ".png");
    }

    public ElevatorBlockTileRenderer(){
        super();
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
        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        GlStateManager.translate(0.5, 0.5, 0.5);
        GlStateManager.rotate(180 - tile.getFacing().getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translate(-0.5, -0.5, -0.51);

        this.drawQuad(BUTTONS, tile.getPos());

        GlStateManager.popMatrix();
    }

    private void renderPlatform(){
        if(tile.getGroup().getLowest() != tile.getPos().getY() || !tile.getGroup().isMoving() || tile.getGroup().getCurrentY() == tile.getGroup().getLastY())
            return;
        IBlockState[][] state = tile.getGroup().getPlatform();
        int size = tile.getGroup().getSize();
        double lastY = tile.getGroup().getLastY(), currentY = tile.getGroup().getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        int startX = tile.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = tile.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;

        for(int platformX = 0; platformX < size; platformX++){
            for(int platformZ = 0; platformZ < size; platformZ++){
                BlockPos pos = tile.getPos().add(startX + platformX, renderY, startZ + platformZ);

                GlStateManager.pushMatrix();

                GlStateManager.translate(x, y, z);
                GlStateManager.translate(0, renderY - pos.getY(), 0);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                GlStateManager.disableLighting();

                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                try{
                    BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
                    IBakedModel model = brd.getModelForState(state[platformX][platformZ]);
                    brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state[platformX][platformZ], pos, buffer, false);
                }catch(Exception e){
                    e.printStackTrace();
                }

                GlStateManager.translate(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

                tessellator.draw();

                GlStateManager.popMatrix();
            }
        }
    }

    private void renderDisplay(){
        int height = tile.getDisplayHeight();
        if(height <= 0)
            return;

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        GlStateManager.translate(0.5, 0.5 + 1, 0.5);
        GlStateManager.rotate(180 - tile.getFacing().getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translate(-0.5, -0.5, -0.51);

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
        GlStateManager.scale(1, height, 1);
        this.drawQuad(background, tile.getPos().up());
        GlStateManager.popMatrix();

        List<ElevatorBlockTile> allTiles = tile.getGroup().getTiles();
        int index = tile.getGroup().getTiles().indexOf(tile);
        ArrayList<ElevatorBlockTile> belowTiles = new ArrayList<>();
        for(int a = index - 1; a >= 0 && belowTiles.size() < button_count; a--)
            belowTiles.add(allTiles.get(a));
        ArrayList<ElevatorBlockTile> aboveTiles = new ArrayList<>();
        for(int a = index + 1; a < allTiles.size() && aboveTiles.size() < button_count; a++)
            aboveTiles.add(allTiles.get(a));

        // render center button
        GlStateManager.translate(0, 0.5 * height - DisplayBlock.BUTTON_HEIGHT / 2, 0);
        GlStateManager.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        this.drawQuad(DISPLAY_BUTTONS_OFF.get(tile.getDisplayLabelColor()), tile.getPos().up());
        GlStateManager.pushMatrix();
        GlStateManager.translate(18.5 / 32d, 0, 0);
        this.drawString(tile.getName());
        GlStateManager.popMatrix();

        // render bottom buttons
        GlStateManager.pushMatrix();
        for(int i = 0; i < belowTiles.size(); i++){
            GlStateManager.translate(0, -1, 0);
            this.drawQuad(DISPLAY_BUTTONS.get(belowTiles.get(i).getDisplayLabelColor()), tile.getPos().up());
            GlStateManager.pushMatrix();
            GlStateManager.translate(18.5 / 32d, 0, 0);
            this.drawString(belowTiles.get(i).getName());
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

        // render top buttons
        GlStateManager.pushMatrix();
        for(int i = 0; i < aboveTiles.size(); i++){
            GlStateManager.translate(0, 1, 0);
            this.drawQuad(DISPLAY_BUTTONS.get(aboveTiles.get(i).getDisplayLabelColor()), tile.getPos().up());
            GlStateManager.pushMatrix();
            GlStateManager.translate(18.5 / 32d, 0, 0);
            this.drawString(aboveTiles.get(i).getName());
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

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
                GlStateManager.translate(1 - (27.5 / 32d + DisplayBlock.BUTTON_HEIGHT / 2d), yOffset, -0.002);
                GlStateManager.scale(DisplayBlock.BUTTON_HEIGHT, 1, 1);
                this.drawQuad(DISPLAY_GREEN_DOT, tile.getPos().up());
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawQuad(ResourceLocation texture, BlockPos pos){
        GlStateManager.pushMatrix();

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        int i = Minecraft.getMinecraft().world.getCombinedLight(pos.offset(tile.getFacing()), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        GlStateManager.disableLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.doPolygonOffset(-1, -1);

        builder.pos(0, 0, 0).tex(1, 1).endVertex();
        builder.pos(0, 1, 0).tex(1, 0).endVertex();
        builder.pos(1, 1, 0).tex(0, 0).endVertex();
        builder.pos(1, 0, 0).tex(0, 1).endVertex();

        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    private void drawString(String s){
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.07, -0.005);
        GlStateManager.scale(-0.01f, -0.08f, 1);

        GlStateManager.disableLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.doPolygonOffset(-1, -1);


        fontRenderer.drawStringWithShadow(s, -fontRenderer.getStringWidth(s) / 2f, -fontRenderer.FONT_HEIGHT, EnumDyeColor.WHITE.getColorValue());

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(ElevatorBlockTile te){
        return true;
    }
}
