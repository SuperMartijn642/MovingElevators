package com.supermartijn642.movingelevators.base;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class METileRenderer<T extends METile> extends TileEntitySpecialRenderer<T> {

    protected T tile;
    protected double x, y, z;
    protected float partialTicks;
    protected int destroyStage;
    protected float alpha;

    public METileRenderer(){
        super();
    }

    @Override
    public void render(T tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        if(tile == null || tile.getWorld() == null)
            return;

        this.tile = tile;
        this.x = x;
        this.y = y;
        this.z = z;
        this.partialTicks = partialTicks;
        this.destroyStage = destroyStage;
        this.alpha = alpha;

        // render camouflage
        this.renderCamouflage();

        this.render();
    }

    protected void render(){
    }

    private void renderCamouflage(){
        if(tile.getCamoBlock() == null)
            return;
        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        GlStateManager.disableLighting();

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        IBlockState state = tile.getCamoBlock();
        try{
            BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBakedModel model = brd.getModelForState(state);
            brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), buffer, false);
        }catch(Exception e){
            e.printStackTrace();
        }

        GlStateManager.translate(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
