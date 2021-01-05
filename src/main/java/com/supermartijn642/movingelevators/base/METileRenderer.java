package com.supermartijn642.movingelevators.base;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class METileRenderer<T extends METile> extends TileEntityRenderer<T> {

    protected T tile;
    protected double x, y, z;
    protected float partialTicks;
    protected int destroyStage;

    public METileRenderer(){
        super();
    }

    @Override
    public void render(T tile, double x, double y, double z, float partialTicks, int destroyStage){
        if(tile == null || tile.getWorld() == null)
            return;

        this.tile = tile;
        this.x = x;
        this.y = y;
        this.z = z;
        this.partialTicks = partialTicks;
        this.destroyStage = destroyStage;

        // render camouflage
        this.renderCamouflage();

        this.render();
    }

    protected void render(){
    }

    private void renderCamouflage(){
        if(tile.getCamoBlock() == null)
            return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        GlStateManager.disableLighting();

        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        BlockState state = tile.getCamoBlock();
        IModelData data = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state).getModelData(tile.getWorld(), tile.getPos(), state, EmptyModelData.INSTANCE);
        try{
            BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
            IBakedModel model = brd.getModelForState(state);
            buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
            brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), buffer, false, new Random(), 42L, data);
        }catch(Exception e){
            e.printStackTrace();
        }

        buffer.setTranslation(0,0,0);
        tessellator.draw();
    }
}
