package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.platform.GLX;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTileRenderer extends TileEntityRenderer<ElevatorBlockTile> {

    public ElevatorBlockTileRenderer(){
        super();
    }

    @Override
    public void render(ElevatorBlockTile tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage){
        if(tileEntityIn == null || tileEntityIn.getWorld() == null)
            return;

        // render buttons
        GlStateManager.pushMatrix();

        GlStateManager.translated(x, y, z);

        GlStateManager.translated(0.5, 0.5, 0.5);
        GlStateManager.rotated(180 - tileEntityIn.getFacing().getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translated(-0.501, -0.501, -0.501);

        Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("movingelevators", "textures/blocks/buttons.png"));

        int i = Minecraft.getInstance().world.getCombinedLight(tileEntityIn.getPos().offset(tileEntityIn.getFacing()), 0);
        int j = i % 65536;
        int k = i / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        GlStateManager.disableLighting();

        builder.pos(0, 0, 0).tex(1, 1).endVertex();
        builder.pos(0, 1, 0).tex(1, 0).endVertex();
        builder.pos(1, 1, 0).tex(0, 0).endVertex();
        builder.pos(1, 0, 0).tex(0, 1).endVertex();

        tessellator.draw();

        GlStateManager.popMatrix();

        if(!tileEntityIn.isMoving())
            return;

        // render platform
        BlockState[][] state = tileEntityIn.getPlatform();
        int size = tileEntityIn.getSize();
        double lastY = tileEntityIn.getLastY(), currentY = tileEntityIn.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        int startX = tileEntityIn.getFacing().getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = tileEntityIn.getFacing().getZOffset() * (int)Math.ceil(size / 2f) - size / 2;

        for(int platformX = 0; platformX < size; platformX++){
            for(int platformZ = 0; platformZ < size; platformZ++){
                BlockPos pos = tileEntityIn.getPos().add(startX + platformX, renderY, startZ + platformZ);

                GlStateManager.pushMatrix();

                GlStateManager.translated(x, y, z);
                GlStateManager.translated(0, renderY - pos.getY(), 0);

                tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                GlStateManager.disableLighting();

                Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

                try{
                    BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
                    IBakedModel model = brd.getModelForState(state[platformX][platformZ]);
                    brd.getBlockModelRenderer().renderModel(tileEntityIn.getWorld(), model, state[platformX][platformZ], pos, buffer, false, new Random(), 0, EmptyModelData.INSTANCE);
                }catch(Exception e){
                    e.printStackTrace();
                }

                GlStateManager.translated(-tileEntityIn.getPos().getX(), -tileEntityIn.getPos().getY(), -tileEntityIn.getPos().getZ());

                tessellator.draw();

                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(ElevatorBlockTile te){
        return true;
    }
}
