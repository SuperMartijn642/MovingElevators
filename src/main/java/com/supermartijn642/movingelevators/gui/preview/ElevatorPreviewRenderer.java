package com.supermartijn642.movingelevators.gui.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class ElevatorPreviewRenderer {

    public static void renderPreview(PoseStack poseStack, WorldBlockCapture capture, AABB cabinBox, AABB previewBox, double x, double y, double scale, float yaw, float pitch){
        AABB bounds = capture.getBounds();
        Vec3 center = bounds.getCenter();
        double span = Math.sqrt(bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize());
        scale /= span;

        poseStack.translate(x, y, 0);
        poseStack.scale((float)scale, (float)-scale, (float)-scale);
        poseStack.mulPose(new Quaternionf().setAngleAxis(pitch / 180 * Math.PI, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis(yaw / 180 * Math.PI, 0, 1, 0));
        poseStack.translate(-center.x, -center.y, -center.z);

        MultiBufferSource.BufferSource renderTypeBuffer = RenderUtils.getMainBufferSource();
        for(BlockPos pos : capture.getBlockLocations())
            renderBlock(capture, pos, poseStack, renderTypeBuffer);

        RenderUtils.renderBox(poseStack, cabinBox, 1, 1, 1, 0.8f, true);
        if(previewBox != null)
            RenderUtils.renderBox(poseStack, previewBox, 0, 0.7f, 0, 0.8f, true);
    }

    private static void renderBlock(WorldBlockCapture capture, BlockPos pos, PoseStack poseStack, MultiBufferSource renderTypeBuffer){
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        BlockState state = capture.getBlockState(pos);
        if(state.getBlock() != Blocks.AIR){
            BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
            int tint = ClientUtils.getMinecraft().getBlockColors().getColor(state, capture.getLevel(), pos, 0);
            ModelBlockRenderer.renderModel(poseStack.last(), renderTypeBuffer, model, ARGB.redFloat(tint), ARGB.greenFloat(tint), ARGB.blueFloat(tint), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, capture.getLevel(), pos, state);
        }

        BlockEntity blockEntity = capture.getBlockEntity(pos);
        if(blockEntity != null)
            ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().render(blockEntity, ClientUtils.getPartialTicks(), poseStack, renderTypeBuffer);

        poseStack.popPose();
    }
}
