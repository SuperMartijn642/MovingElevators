package com.supermartijn642.movingelevators;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlockTileRenderer extends TileEntitySpecialRenderer<ElevatorBlockTile> {

    @Override
    public void render(ElevatorBlockTile tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        if(tileEntityIn == null || tileEntityIn.getWorld() == null)
            return;

        // render buttons
        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        GlStateManager.translate(0.5, 0.5, 0.5);
        GlStateManager.rotate(180 - tileEntityIn.getFacing().getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translate(-0.501, -0.501, -0.501);

        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("movingelevators", "textures/blocks/buttons.png"));

        int i = Minecraft.getMinecraft().world.getCombinedLight(tileEntityIn.getPos().offset(tileEntityIn.getFacing()), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);

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
        IBlockState[][] state = tileEntityIn.getPlatform();
        int size = tileEntityIn.getSize();
        double lastY = tileEntityIn.getLastY(), currentY = tileEntityIn.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        int startX = tileEntityIn.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = tileEntityIn.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;

        for(int platformX = 0; platformX < size; platformX++){
            for(int platformZ = 0; platformZ < size; platformZ++){
                BlockPos pos = tileEntityIn.getPos().add(startX + platformX, renderY, startZ + platformZ);

                GlStateManager.pushMatrix();

                GlStateManager.translate(x, y, z);
                GlStateManager.translate(0, renderY - pos.getY(), 0);

                tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                GlStateManager.disableLighting();

                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                try{
                    BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
                    IBakedModel model = brd.getModelForState(state[platformX][platformZ]);
                    brd.getBlockModelRenderer().renderModel(tileEntityIn.getWorld(), model, state[platformX][platformZ], pos, buffer, false);
                }catch(Exception e){
                    e.printStackTrace();
                }

                GlStateManager.translate(-tileEntityIn.getPos().getX(), -tileEntityIn.getPos().getY(), -tileEntityIn.getPos().getZ());

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
