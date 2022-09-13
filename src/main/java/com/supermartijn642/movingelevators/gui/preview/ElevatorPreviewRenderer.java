package com.supermartijn642.movingelevators.gui.preview;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class ElevatorPreviewRenderer {

    public static void renderPreview(WorldBlockCapture capture, AxisAlignedBB cabinBox, AxisAlignedBB previewBox, double x, double y, double scale, float yaw, float pitch, boolean doShading){
        AxisAlignedBB bounds = capture.getBounds();
        Vector3d center = bounds.getCenter();
        double span = Math.sqrt(bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize());
        scale /= span;

        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, 350);
        RenderSystem.scalef(1, -1, 1);
        RenderSystem.scaled(scale, scale, scale);

        MatrixStack poseStack = new MatrixStack();
        poseStack.mulPose(new Quaternion(pitch, yaw, 0, true));
        poseStack.translate(-center.x, -center.y, -center.z);

        if(doShading)
            RenderSystem.enableLighting();

        IRenderTypeBuffer.Impl renderTypeBuffer = RenderUtils.getMainBufferSource();
        for(BlockPos pos : capture.getBlockLocations())
            renderBlock(capture, pos, poseStack, renderTypeBuffer);
        renderTypeBuffer.endBatch();

        RenderSystem.enableDepthTest();
        if(doShading)
            RenderSystem.disableLighting();

        RenderUtils.renderBox(poseStack, cabinBox, 1, 1, 1, 0.8f, true);
        if(previewBox != null)
            RenderUtils.renderBox(poseStack, previewBox, 0, 0.7f, 0, 0.8f, true);

        RenderSystem.popMatrix();

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
    }

    private static void renderBlock(WorldBlockCapture capture, BlockPos pos, MatrixStack poseStack, IRenderTypeBuffer renderTypeBuffer){
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        BlockState state = capture.getBlockState(pos);
        if(state.getBlock() != Blocks.AIR){
            IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
            IModelData modelData = EmptyModelData.INSTANCE;
//            if(model instanceof RechiseledConnectedBakedModel){ // TODO
//                RechiseledModelData data = new RechiseledModelData();
//                for(Direction direction : Direction.values())
//                    data.sides.put(direction, new RechiseledModelData.SideData(direction, capture::getBlock, pos, state.getBlock()));
//                modelData = new ModelDataMap.Builder().withInitial(RechiseledModelData.PROPERTY, data).build();
//            }

            RenderType renderType = RenderTypeLookup.getRenderType(state, true);
            renderModel(model, capture, state, pos, poseStack, renderTypeBuffer.getBuffer(renderType), modelData);
        }

        TileEntity blockEntity = capture.getBlockEntity(pos);
        if(blockEntity != null)
            TileEntityRendererDispatcher.instance.render(blockEntity, ClientUtils.getPartialTicks(), poseStack, renderTypeBuffer);

        poseStack.popPose();
    }

    private static void renderModel(IBakedModel model, WorldBlockCapture capture, BlockState state, BlockPos pos, MatrixStack poseStack, IVertexBuilder buffer, IModelData modelData){
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(capture, state, pos, poseStack, buffer, model.getQuads(state, direction, random, modelData));
        }

        random.setSeed(42L);
        renderQuads(capture, state, pos, poseStack, buffer, model.getQuads(state, null, random, modelData));
    }

    private static void renderQuads(WorldBlockCapture capture, BlockState state, BlockPos pos, MatrixStack poseStack, IVertexBuilder buffer, List<BakedQuad> quads){
        MatrixStack.Entry matrix = poseStack.last();

        for(BakedQuad bakedquad : quads){
            float red = 1, blue = 1, green = 1, alpha = 1;
            if(bakedquad.isTinted()){
                int color = ClientUtils.getMinecraft().getBlockColors().getColor(state, capture.getLevel(), pos, bakedquad.getTintIndex());
                red = (color >> 16 & 255) / 255f;
                green = (color >> 8 & 255) / 255f;
                blue = (color & 255) / 255f;
            }
            buffer.addVertexData(matrix, bakedquad, red, green, blue, alpha, 15728880, OverlayTexture.NO_OVERLAY, false);
        }
    }
}
