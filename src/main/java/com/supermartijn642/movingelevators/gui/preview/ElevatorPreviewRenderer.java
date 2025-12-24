package com.supermartijn642.movingelevators.gui.preview;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class ElevatorPreviewRenderer {

    private static final RandomSource RANDOM = RandomSource.create();
    private static final CameraRenderState DUMMY_CAMERA_RENDER_STATE = new CameraRenderState();
    private static FeatureRenderDispatcher featureRenderDispatcher;

    public static void renderPreview(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, WorldBlockCapture.RenderState capture, AABB cabinBox, AABB previewBox, double x, double y, double scale, float yaw, float pitch){
        AABB bounds = capture.bounds();
        Vec3 center = bounds.getCenter();
        double span = Math.sqrt(bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize());
        scale /= span;

        poseStack.translate(x, y, 0);
        poseStack.scale((float)scale, (float)-scale, (float)-scale);
        poseStack.mulPose(new Quaternionf().setAngleAxis(pitch / 180 * Math.PI, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis(yaw / 180 * Math.PI, 0, 1, 0));
        poseStack.translate(-center.x, -center.y, -center.z);

        setupFeatureRenderer(bufferSource);

        for(int i = 0; i < capture.size(); i++)
            renderBlock(capture, i, poseStack, bufferSource);

        featureRenderDispatcher.renderAllFeatures();

        RenderUtils.renderBox(poseStack, cabinBox, 1, 1, 1, 0.8f, true);
        if(previewBox != null)
            RenderUtils.renderBox(poseStack, previewBox, 0, 0.7f, 0, 0.8f, true);
    }

    private static void renderBlock(WorldBlockCapture.RenderState capture, int index, PoseStack poseStack, MultiBufferSource bufferSource){
        BlockPos pos = capture.position(index);
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        BlockState state = capture.state(index);
        if(state.getBlock() != Blocks.AIR){
            BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
            int tint = capture.tint(index);
            ModelData modelData = capture.modelData(index);
            RANDOM.setSeed(42);
            for(ChunkSectionLayer layer : model.getRenderTypes(state, RANDOM, modelData)){
                ModelBlockRenderer.renderModel(poseStack.last(), bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(layer)), model, ARGB.redFloat(tint), ARGB.greenFloat(tint), ARGB.blueFloat(tint), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, modelData, layer);
            }
        }

        BlockEntityRenderState entityRenderState = capture.entityRenderState(index);
        if(entityRenderState != null)
            ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().submit(entityRenderState, poseStack, featureRenderDispatcher.getSubmitNodeStorage(), DUMMY_CAMERA_RENDER_STATE);

        poseStack.popPose();
    }

    private static void setupFeatureRenderer(MultiBufferSource.BufferSource bufferSource){
        if(featureRenderDispatcher == null){
            featureRenderDispatcher = new FeatureRenderDispatcher(
                new SubmitNodeStorage(),
                ClientUtils.getBlockRenderer(),
                bufferSource,
                ClientUtils.getMinecraft().getAtlasManager(),
                new OutlineBufferSource() {
                    @Override
                    public VertexConsumer getBuffer(RenderType renderType){
                        return VertexMultiConsumer.create(new VertexConsumer[0]); // Discard everything
                    }
                },
                MultiBufferSource.immediate(ByteBufferBuilder.exactlySized(0)),
                ClientUtils.getFontRenderer()
            );
        }else
            featureRenderDispatcher.bufferSource = bufferSource;
    }
}
