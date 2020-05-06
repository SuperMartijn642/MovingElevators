package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.movingelevators.base.ElevatorInputTileRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTileRenderer extends ElevatorInputTileRenderer<ElevatorBlockTile> {

    public ElevatorBlockTileRenderer(){
        super();
    }

    @Override
    protected void render(){
        if(!tile.hasGroup())
            return;

        super.render();

        // render platform
        this.renderPlatform();
    }

    private void renderPlatform(){
        if(tile.getGroup().getLowest() != tile.getPos().getY() || !tile.getGroup().isMoving() || tile.getGroup().getCurrentY() == tile.getGroup().getLastY())
            return;
        BlockState[][] state = tile.getGroup().getPlatform();
        int size = tile.getGroup().getSize();
        double lastY = tile.getGroup().getLastY(), currentY = tile.getGroup().getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        int startX = tile.getFacing().getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = tile.getFacing().getZOffset() * (int)Math.ceil(size / 2f) - size / 2;

        for(int platformX = 0; platformX < size; platformX++){
            for(int platformZ = 0; platformZ < size; platformZ++){
                BlockPos pos = tile.getPos().add(startX + platformX, renderY, startZ + platformZ);

                GlStateManager.pushMatrix();

                GlStateManager.translated(x, y, z);
                GlStateManager.translated(0, renderY - pos.getY(), 0);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                GlStateManager.disableLighting();

                Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

                try{
                    BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
                    IBakedModel model = brd.getModelForState(state[platformX][platformZ]);
                    brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state[platformX][platformZ], pos, buffer, false, new Random(), 0, EmptyModelData.INSTANCE);
                }catch(Exception e){
                    e.printStackTrace();
                }

                GlStateManager.translated(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

                tessellator.draw();

                GlStateManager.popMatrix();
            }
        }
    }
}
