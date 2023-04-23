package com.supermartijn642.movingelevators.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputBlockEntityRenderer<T extends ElevatorInputBlockEntity> implements CustomBlockEntityRenderer<T> {

    @Override
    public void render(T entity, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
        if(entity.getFacing() == null || (entity instanceof ControllerBlockEntity && !((ControllerBlockEntity)entity).shouldShowButtons()))
            return;

        IVertexBuilder buffer = bufferSource.getBuffer(RenderType.cutout());

        // render buttons
        Direction facing = entity.getFacing();
        combinedLight = WorldRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternion(0, 180 - facing.toYRot(), 0, true));
        poseStack.translate(-0.5, -0.5, -0.51);

        boolean canReceiveInput = entity.canReceiveInput();
        boolean renderUp = canReceiveInput && entity.canMoveUp();
        boolean renderDown = canReceiveInput && entity.canMoveDown();
        this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, renderUp ? 64 : 87, 23, 23);
        this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 23, canReceiveInput ? 64 : 87, 23, 23);
        this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 46, renderDown ? 64 : 87, 23, 23);

        poseStack.popPose();
    }

    private void drawOverlayPart(MatrixStack poseStack, IVertexBuilder buffer, int combinedLight, int combinedOverlay, Direction facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        float minU = MovingElevatorsClient.OVERLAY_SPRITE.getU(tX / 8f), maxU = MovingElevatorsClient.OVERLAY_SPRITE.getU((tX + tWidth) / 8f);
        float minV = MovingElevatorsClient.OVERLAY_SPRITE.getV(tY / 8f), maxV = MovingElevatorsClient.OVERLAY_SPRITE.getV((tY + tHeight) / 8f);

        buffer.vertex(matrix, x, y + height, 0).color(255, 255, 255, 255).uv(maxU, minV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(255, 255, 255, 255).uv(minU, minV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(255, 255, 255, 255).uv(minU, maxV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x, y, 0).color(255, 255, 255, 255).uv(maxU, maxV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
    }
}
