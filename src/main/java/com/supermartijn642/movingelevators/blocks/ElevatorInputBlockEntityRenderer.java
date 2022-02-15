package com.supermartijn642.movingelevators.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputBlockEntityRenderer<T extends ElevatorInputBlockEntity> implements BlockEntityRenderer<T> {

    @Override
    public void render(T entity, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(!entity.hasGroup() || entity.getFacing() == null || (entity instanceof ControllerBlockEntity && !((ControllerBlockEntity)entity).shouldShowButtons()))
            return;

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());

        // render buttons
        Direction facing = entity.getFacing();
        combinedLight = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));

        matrixStack.pushPose();

        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.mulPose(new Quaternion(0, 180 - facing.toYRot(), 0, true));
        matrixStack.translate(-0.5, -0.5, -0.51);

        ElevatorGroup group = entity.getGroup();
        int floorNumber = group.getFloorNumber(entity.getFloorLevel()), floorCount = group.getFloorCount();
        this.drawOverlayPart(matrixStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, floorNumber < floorCount - 1 ? 64 : 87, 23, 23);
        this.drawOverlayPart(matrixStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 23, 64, 23, 23);
        this.drawOverlayPart(matrixStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 46, floorNumber > 0 ? 64 : 87, 23, 23);

        matrixStack.popPose();
    }

    private void drawOverlayPart(PoseStack matrixStack, VertexConsumer buffer, int combinedLight, int combinedOverlay, Direction facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        Matrix4f matrix = matrixStack.last().pose();
        Matrix3f normalMatrix = matrixStack.last().normal();

        float minU = MovingElevatorsClient.OVERLAY_SPRITE.getU(tX / 8f), maxU = MovingElevatorsClient.OVERLAY_SPRITE.getU((tX + tWidth) / 8f);
        float minV = MovingElevatorsClient.OVERLAY_SPRITE.getV(tY / 8f), maxV = MovingElevatorsClient.OVERLAY_SPRITE.getV((tY + tHeight) / 8f);

        buffer.vertex(matrix, x, y + height, 0).color(255, 255, 255, 255).uv(maxU, minV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(255, 255, 255, 255).uv(minU, minV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(255, 255, 255, 255).uv(minU, maxV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x, y, 0).color(255, 255, 255, 255).uv(maxU, maxV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
    }
}
