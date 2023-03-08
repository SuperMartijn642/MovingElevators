package com.supermartijn642.movingelevators.gui.preview;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

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

        PoseStack poseStack = new PoseStack();
        poseStack.translate(x, -y, 350);
        poseStack.scale((float)scale, (float)scale, (float)scale);
        poseStack.mulPose(new Quaternionf().setAngleAxis(pitch / 180 * Math.PI, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis(yaw / 180 * Math.PI, 0, 1, 0));
        poseStack.translate(-center.x, -center.y, -center.z);

        if(doShading)
            Lighting.setupFor3DItems();

        MultiBufferSource.BufferSource renderTypeBuffer = RenderUtils.getMainBufferSource();
        for(BlockPos pos : capture.getBlockLocations())
            renderBlock(capture, pos, poseStack, renderTypeBuffer);
        renderTypeBuffer.endBatch();

        RenderSystem.enableDepthTest();
        if(doShading)
            Lighting.setupForFlatItems();

        RenderUtils.renderBox(poseStack, cabinBox, 1, 1, 1, 0.8f, true);
        if(previewBox != null)
            RenderUtils.renderBox(poseStack, previewBox, 0, 0.7f, 0, 0.8f, true);

        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderBlock(WorldBlockCapture capture, BlockPos pos, PoseStack poseStack, MultiBufferSource renderTypeBuffer){
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        BlockState state = capture.getBlockState(pos);
        if(state.getBlock() != Blocks.AIR){
            BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
//            if(model instanceof RechiseledConnectedBakedModel){ // TODO
//                RechiseledModelData data = new RechiseledModelData();
//                for(Direction direction : Direction.values())
//                    data.sides.put(direction, new RechiseledModelData.SideData(direction, capture::getBlock, pos, state.getBlock()));
//                modelData = new ModelDataMap.Builder().withInitial(RechiseledModelData.PROPERTY, data).build();
//            }

            RenderType renderType = ItemBlockRenderTypes.getRenderType(state, true);
            renderModel(model, capture, state, pos, poseStack, renderTypeBuffer.getBuffer(renderType));
        }

        BlockEntity blockEntity = capture.getBlockEntity(pos);
        if(blockEntity != null)
            ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().render(blockEntity, ClientUtils.getPartialTicks(), poseStack, renderTypeBuffer);

        poseStack.popPose();
    }

    private static void renderModel(BakedModel model, WorldBlockCapture capture, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer buffer){
        RandomSource random = RandomSource.create();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(capture, state, pos, poseStack, buffer, model.getQuads(state, direction, random));
        }

        random.setSeed(42L);
        renderQuads(capture, state, pos, poseStack, buffer, model.getQuads(state, null, random));
    }

    private static void renderQuads(WorldBlockCapture capture, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads){
        PoseStack.Pose matrix = poseStack.last();

        for(BakedQuad bakedquad : quads){
            float red = 1, blue = 1, green = 1, alpha = 1;
            if(bakedquad.isTinted()){
                int color = ClientUtils.getMinecraft().getBlockColors().getColor(state, capture.getWorld(), pos, bakedquad.getTintIndex());
                red = (color >> 16 & 255) / 255f;
                green = (color >> 8 & 255) / 255f;
                blue = (color & 255) / 255f;
            }
            buffer.putBulkData(matrix, bakedquad, red, green, blue, 15728880, OverlayTexture.NO_OVERLAY);
        }
    }
}
