package com.supermartijn642.movingelevators.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputBlockEntityRenderer<T extends ElevatorInputBlockEntity> implements CustomBlockEntityRenderer<T> {

    @Override
    public void render(T entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(entity.getFacing() == null || (entity instanceof ControllerBlockEntity && !((ControllerBlockEntity)entity).shouldShowButtons()))
            return;

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());

        // render buttons
        Direction facing = entity.getFacing();
        combinedLight = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternionf().setAngleAxis((180 - facing.toYRot()) / 180 * Math.PI, 0, 1, 0));
        poseStack.translate(-0.5, -0.5, -0.51);

        boolean canReceiveInput = entity.canReceiveInput();
        boolean renderUp = canReceiveInput && entity.canMoveUp();
        boolean renderDown = canReceiveInput && entity.canMoveDown();
        this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, renderUp ? 64 : 87, 23, 23);
        this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 23, canReceiveInput ? 64 : 87, 23, 23);
        this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 46, renderDown ? 64 : 87, 23, 23);

        poseStack.popPose();
    }

    private void drawOverlayPart(PoseStack poseStack, VertexConsumer buffer, int combinedLight, int combinedOverlay, Direction facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        TextureAtlasSprite overlaySprite = MovingElevatorsClient.getOverlaySprite();
        float minU = overlaySprite.getU(tX / 8f), maxU = overlaySprite.getU((tX + tWidth) / 128f);
        float minV = overlaySprite.getV(tY / 8f), maxV = overlaySprite.getV((tY + tHeight) / 128f);

        buffer.vertex(matrix, x, y + height, 0).color(255, 255, 255, 255).uv(maxU, minV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(255, 255, 255, 255).uv(minU, minV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(255, 255, 255, 255).uv(minU, maxV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
        buffer.vertex(matrix, x, y, 0).color(255, 255, 255, 255).uv(maxU, maxV).uv2(combinedLight).normal(normalMatrix, facing.getStepX(), facing.getStepY(), facing.getStepZ()).overlayCoords(combinedOverlay).endVertex();
    }
}
