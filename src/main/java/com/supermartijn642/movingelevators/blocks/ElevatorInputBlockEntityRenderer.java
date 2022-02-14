package com.supermartijn642.movingelevators.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputBlockEntityRenderer<T extends ElevatorInputBlockEntity> extends TileEntityRenderer<T> {

    public ElevatorInputBlockEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn){
        super(rendererDispatcherIn);
    }

    @Override
    public void render(T entity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
        if(!entity.hasGroup() || entity.getFacing() == null || (entity instanceof ControllerBlockEntity && !((ControllerBlockEntity)entity).shouldShowButtons()))
            return;

        IVertexBuilder buffer = bufferSource.getBuffer(RenderType.cutout());

        // render buttons
        Direction facing = entity.getFacing();
        combinedLight = WorldRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));

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

    private void drawOverlayPart(MatrixStack matrixStack, IVertexBuilder buffer, int combinedLight, int combinedOverlay, Direction facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
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
