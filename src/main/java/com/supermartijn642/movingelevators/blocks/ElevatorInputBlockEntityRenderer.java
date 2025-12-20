package com.supermartijn642.movingelevators.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputBlockEntityRenderer<T extends ElevatorInputBlockEntity> implements CustomBlockEntityRenderer<T,ElevatorInputBlockEntityRenderer.State> {

    @Override
    public State createStateHolder(){
        return new State();
    }

    @Override
    public void updateState(State state, T entity, UpdateContext context){
        if(entity.getFacing() == null || (entity instanceof ControllerBlockEntity && !((ControllerBlockEntity)entity).shouldShowButtons())){
            state.shouldRender = false;
            return;
        }
        state.shouldRender = true;

        Direction facing = entity.getFacing();
        state.facing = facing;
        state.frontLighting = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));
        state.showCenter = entity.canReceiveInput();
        state.showUp = state.showCenter && entity.canMoveUp();
        state.showDown = state.showCenter && entity.canMoveDown();
    }

    @Override
    public void submit(SubmitNodeCollector output, State state, RenderContext context){
        if(!state.shouldRender)
            return;

        // render buttons
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternionf().setAngleAxis((180 - state.facing.toYRot()) / 180 * Math.PI, 0, 1, 0));
        poseStack.translate(-0.5, -0.5, -0.51);

        ModelFeatureRenderer.CrumblingOverlay breakingOverlay = context.breakingOverlay();
        int overlay = breakingOverlay == null ? OverlayTexture.NO_OVERLAY : breakingOverlay.progress();
        this.drawOverlayPart(poseStack, output, state.frontLighting, overlay, state.facing, 0, 0, 1, 1, 0, state.showUp ? 64 : 87, 23, 23);
        this.drawOverlayPart(poseStack, output, state.frontLighting, overlay, state.facing, 0, 0, 1, 1, 23, state.showCenter ? 64 : 87, 23, 23);
        this.drawOverlayPart(poseStack, output, state.frontLighting, overlay, state.facing, 0, 0, 1, 1, 46, state.showDown ? 64 : 87, 23, 23);

        poseStack.popPose();
    }

    private void drawOverlayPart(PoseStack poseStack, SubmitNodeCollector output, int combinedLight, int combinedOverlay, Direction facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        output.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(), (pose, buffer) -> {
            Matrix4f matrix = pose.pose();
            TextureAtlasSprite overlaySprite = MovingElevatorsClient.getOverlaySprite();
            float minU = overlaySprite.getU(tX / 128f), maxU = overlaySprite.getU((tX + tWidth) / 128f);
            float minV = overlaySprite.getV(tY / 128f), maxV = overlaySprite.getV((tY + tHeight) / 128f);

            buffer.addVertex(matrix, x, y + height, 0).setColor(255, 255, 255, 255).setUv(maxU, minV).setLight(combinedLight).setNormal(poseStack.last(), facing.getStepX(), facing.getStepY(), facing.getStepZ()).setOverlay(combinedOverlay);
            buffer.addVertex(matrix, x + width, y + height, 0).setColor(255, 255, 255, 255).setUv(minU, minV).setLight(combinedLight).setNormal(poseStack.last(), facing.getStepX(), facing.getStepY(), facing.getStepZ()).setOverlay(combinedOverlay);
            buffer.addVertex(matrix, x + width, y, 0).setColor(255, 255, 255, 255).setUv(minU, maxV).setLight(combinedLight).setNormal(poseStack.last(), facing.getStepX(), facing.getStepY(), facing.getStepZ()).setOverlay(combinedOverlay);
            buffer.addVertex(matrix, x, y, 0).setColor(255, 255, 255, 255).setUv(maxU, maxV).setLight(combinedLight).setNormal(poseStack.last(), facing.getStepX(), facing.getStepY(), facing.getStepZ()).setOverlay(combinedOverlay);
        });
    }

    public static class State {
        boolean shouldRender;
        Direction facing;
        int frontLighting;
        boolean showUp, showCenter, showDown;
    }
}
