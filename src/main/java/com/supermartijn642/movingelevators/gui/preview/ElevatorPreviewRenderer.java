package com.supermartijn642.movingelevators.gui.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class ElevatorPreviewRenderer {

    private static final CameraRenderState DUMMY_CAMERA_RENDER_STATE = new CameraRenderState();

    public static void renderPreview(PoseStack poseStack, SubmitNodeCollector output, WorldBlockCapture.RenderState capture, AABB cabinBox, AABB previewBox, double x, double y, double scale, float yaw, float pitch){
        AABB bounds = capture.bounds();
        Vec3 center = bounds.getCenter();
        double span = Math.sqrt(bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize());
        scale /= span;

        poseStack.translate(x, y, 0);
        poseStack.scale((float)scale, (float)-scale, (float)-scale);
        poseStack.mulPose(new Quaternionf().setAngleAxis(pitch / 180 * Math.PI, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis(yaw / 180 * Math.PI, 0, 1, 0));
        poseStack.translate(-center.x, -center.y, -center.z);

        for(int i = 0; i < capture.size(); i++)
            renderBlock(capture, i, poseStack, output);

        RenderUtils.submitShape(output, poseStack, BlockShape.create(cabinBox), 1, 1, 1, 0.8f, true);
        if(previewBox != null)
            RenderUtils.submitShape(output, poseStack, BlockShape.create(previewBox), 0, 0.7f, 0, 0.8f, true);
    }

    private static void renderBlock(WorldBlockCapture.RenderState capture, int index, PoseStack poseStack, SubmitNodeCollector output){
        BlockPos pos = capture.position(index);
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        capture.blockRenderState(index).submit(poseStack, output, capture.lighting(index), OverlayTexture.NO_OVERLAY, 0);
        BlockEntityRenderState entityRenderState = capture.entityRenderState(index);
        if(entityRenderState != null)
            ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().submit(entityRenderState, poseStack, output, DUMMY_CAMERA_RENDER_STATE);

        poseStack.popPose();
    }
}
