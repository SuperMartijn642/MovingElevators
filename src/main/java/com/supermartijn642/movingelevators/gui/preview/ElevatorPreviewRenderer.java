package com.supermartijn642.movingelevators.gui.preview;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class ElevatorPreviewRenderer {

    public static void renderPreview(WorldBlockCapture capture, AABB cabinBox, AABB previewBox, double x, double y, double scale, float yaw, float pitch, boolean doShading){
        AABB bounds = capture.getBounds();
        Vec3 center = bounds.getCenter();
        double span = Math.sqrt(bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize());
        scale /= span;

        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().scale(1, -1, 1);
        RenderSystem.applyModelViewMatrix();

        PoseStack matrixStack = new PoseStack();
        matrixStack.translate(x, -y, 350);
        matrixStack.scale((float)scale, (float)scale, (float)scale);
        matrixStack.mulPose(new Quaternion(pitch, yaw, 0, true));
        matrixStack.translate(-center.x, -center.y, -center.z);

        if(doShading)
            Lighting.setupFor3DItems();

        MultiBufferSource.BufferSource renderTypeBuffer = RenderUtils.getMainBufferSource();
        for(BlockPos pos : capture.getBlockLocations())
            renderBlock(capture, pos, matrixStack, renderTypeBuffer);
        renderTypeBuffer.endBatch();

        RenderSystem.enableDepthTest();
        if(doShading)
            Lighting.setupForFlatItems();

        RenderUtils.renderBox(matrixStack, cabinBox, 1, 1, 1, 0.8f);
        if(previewBox != null)
            RenderUtils.renderBox(matrixStack, previewBox, 0, 0.7f, 0, 0.8f);

        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderBlock(WorldBlockCapture capture, BlockPos pos, PoseStack matrixStack, MultiBufferSource renderTypeBuffer){
        matrixStack.pushPose();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());

        BlockState state = capture.getBlockState(pos);
        if(state.getBlock() != Blocks.AIR){
            BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
            IModelData modelData = EmptyModelData.INSTANCE;
//            if(model instanceof RechiseledConnectedBakedModel){ // TODO
//                RechiseledModelData data = new RechiseledModelData();
//                for(Direction direction : Direction.values())
//                    data.sides.put(direction, new RechiseledModelData.SideData(direction, capture::getBlock, pos, state.getBlock()));
//                modelData = new ModelDataMap.Builder().withInitial(RechiseledModelData.PROPERTY, data).build();
//            }

            RenderType renderType = ItemBlockRenderTypes.getRenderType(state, true);
            renderModel(model, capture, state, pos, matrixStack, renderTypeBuffer.getBuffer(renderType), modelData);
        }

        BlockEntity blockEntity = capture.getBlockEntity(pos);
        if(blockEntity != null)
            ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().render(blockEntity, ClientUtils.getPartialTicks(), matrixStack, renderTypeBuffer);

        matrixStack.popPose();
    }

    private static void renderModel(BakedModel modelIn, WorldBlockCapture capture, BlockState state, BlockPos pos, PoseStack matrixStackIn, VertexConsumer bufferIn, IModelData modelData){
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(capture, state, pos, matrixStackIn, bufferIn, modelIn.getQuads(state, direction, random, modelData));
        }

        random.setSeed(42L);
        renderQuads(capture, state, pos, matrixStackIn, bufferIn, modelIn.getQuads(state, null, random, modelData));
    }

    private static void renderQuads(WorldBlockCapture capture, BlockState state, BlockPos pos, PoseStack matrixStackIn, VertexConsumer bufferIn, List<BakedQuad> quadsIn){
        PoseStack.Pose matrix = matrixStackIn.last();

        for(BakedQuad bakedquad : quadsIn){
            float red = 1, blue = 1, green = 1, alpha = 1;
            if(bakedquad.isTinted()){
                int color = ClientUtils.getMinecraft().getBlockColors().getColor(state, capture.getWorld(), pos, bakedquad.getTintIndex());
                red = (color >> 16 & 255) / 255f;
                green = (color >> 8 & 255) / 255f;
                blue = (color & 255) / 255f;
            }
            bufferIn.putBulkData(matrix, bakedquad, red, green, blue, alpha, 15728880, OverlayTexture.NO_OVERLAY, false);
        }
    }
}
