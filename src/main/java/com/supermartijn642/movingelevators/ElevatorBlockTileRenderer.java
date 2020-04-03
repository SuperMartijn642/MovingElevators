package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.lwjgl.opengl.GL11;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTileRenderer extends TileEntityRenderer<ElevatorBlockTile> {

    private static final RenderType TYPE;

    static{
        RenderType.State state = RenderType.State.getBuilder().transparency(new RenderState.TransparencyState("translucent_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableAlphaTest();
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.disableAlphaTest();
        })).texture(new RenderState.TextureState(new ResourceLocation("movingelevators", "textures/blocks/buttons.png"), false, false)).build(false);
        TYPE = RenderType.makeType("movingelevators_texture_quad", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 256, false, true, state);
    }

    public ElevatorBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn){
        super(rendererDispatcherIn);
    }

    @Override
    public void render(ElevatorBlockTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn){
        if(tileEntityIn == null || tileEntityIn.getWorld() == null)
            return;

//        System.out.println("rendering at y: " + tileEntityIn.getPos().getY());

        // render camouflage
//        Block block = tileEntityIn.getCamoBlock();
//        if(block != null){
//            matrixStackIn.push();
//
//            matrixStackIn.translate(-0.001,-0.001,-0.001);
//            matrixStackIn.scale(1.002f,1.002f,1.002f);
//
//            int light = WorldRenderer.getCombinedLight(tileEntityIn.getWorld(),tileEntityIn.getPos().offset(tileEntityIn.getFacing()));
//
//            Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(block.getDefaultState(), matrixStackIn, bufferIn, light, combinedOverlayIn);
//
//            matrixStackIn.pop();
//        }

        // render buttons
        matrixStackIn.push();

        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.rotate(new Quaternion(0, 180 - tileEntityIn.getFacing().getHorizontalAngle(), 0, true));
        matrixStackIn.translate(-0.501, -0.501, -0.501);

        Matrix4f matrix = matrixStackIn.getLast().getMatrix();
        IVertexBuilder builder = bufferIn.getBuffer(TYPE);

        builder.pos(matrix, 0, 0, 0).tex(1, 1).endVertex();
        builder.pos(matrix, 0, 1, 0).tex(1, 0).endVertex();
        builder.pos(matrix, 1, 1, 0).tex(0, 0).endVertex();
        builder.pos(matrix, 1, 0, 0).tex(0, 1).endVertex();

        matrixStackIn.pop();

        if(!tileEntityIn.isMoving())
            return;

        // render platform
        BlockState[][] state = tileEntityIn.getPlatform();
        int size = tileEntityIn.getSize();
        double lastY = tileEntityIn.getLastY(), currentY = tileEntityIn.getCurrentY();
        double y = lastY + (currentY - lastY) * partialTicks - tileEntityIn.getPos().getY();
        int startX = tileEntityIn.getFacing().getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = tileEntityIn.getFacing().getZOffset() * (int)Math.ceil(size / 2f) - size / 2;

        BlockPos topPos = tileEntityIn.getPos().offset(tileEntityIn.getFacing(),(int)Math.ceil(size / 2f)).add(0,y,0);
        int currentLight = WorldRenderer.getCombinedLight(tileEntityIn.getWorld(),topPos);
//        int lastLight = WorldRenderer.getCombinedLight(tileEntityIn.getWorld(),topPos.down());
//        int light = (int)(lastLight + (currentLight - lastLight) * (y % 1));

        for(int x = 0; x < size; x++){
            for(int z = 0; z < size; z++){
                matrixStackIn.push();

                matrixStackIn.translate(startX + x, y, startZ + z);

                Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state[x][z], matrixStackIn, bufferIn, currentLight, combinedOverlayIn);

                matrixStackIn.pop();
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(ElevatorBlockTile te){
        return true;
    }
}
