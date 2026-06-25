package com.supermartijn642.movingelevators.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Created 13/02/2022 by SuperMartijn642
 */
public class DisplayBlockEntityRenderer implements CustomBlockEntityRenderer<DisplayBlockEntity,DisplayBlockEntityRenderer.State> {

    private static final double TEXT_RENDER_DISTANCE = 15 * 15;

    @Override
    public State createStateHolder(){
        return new State();
    }

    @Override
    public void updateState(State state, DisplayBlockEntity entity, UpdateContext context){
        if(!entity.isBottomDisplay() || !entity.getInputBlockEntity().hasGroup()){
            state.shouldRender = false;
            return;
        }
        state.shouldRender = true;

        Direction facing = entity.getFacing();
        state.facing = facing;
        int height = entity.hasDisplayOnTop() ? 2 : 1;
        state.displayHeight = height;

        // Figure out lighting for the front of the display
        Level level = entity.getLevel();
        BlockPos frontPos = entity.getBlockPos().relative(facing);
        if(height == 1)
            state.frontLighting = LevelRenderer.getLightCoords(level, frontPos);
        else if(level.getBlockState(frontPos).emissiveRendering(level, frontPos) || level.getBlockState(frontPos.above()).emissiveRendering(level, frontPos.above()))
            state.frontLighting = 15728880;
        else{
            int skyLight = Math.max(level.getBrightness(LightLayer.SKY, frontPos), level.getBrightness(LightLayer.SKY, frontPos.above()));
            int blockLight = Math.max(level.getBrightness(LightLayer.BLOCK, frontPos), level.getBrightness(LightLayer.BLOCK, frontPos.above()));
            int blockStateLight = Math.max(level.getBlockState(frontPos).getLightEmission(), level.getBlockState(frontPos.above()).getLightEmission());
            blockLight = Math.max(blockLight, blockStateLight);
            state.frontLighting = skyLight << 20 | blockLight << 4;
        }
        ModelFeatureRenderer.CrumblingOverlay breakingOverlay = context.breakingOverlay();
        state.combinedOverlay = breakingOverlay == null ? OverlayTexture.NO_OVERLAY : breakingOverlay.progress();

        // Get floors to display
        ElevatorGroup group = entity.getElevatorGroup();
        int index = group.getFloorNumber(entity.getInputBlockEntity().getFloorLevel());
        int button_count = height == 1 ? DisplayBlock.BUTTON_COUNT : DisplayBlock.BUTTON_COUNT_BIG;
        int below = index;
        int above = group.getFloorCount() - index - 1;
        if(below < above){
            below = Math.min(below, button_count);
            above = Math.min(above, button_count * 2 - below);
        }else{
            above = Math.min(above, button_count);
            below = Math.min(below, button_count * 2 - above);
        }
        int startIndex = index - below;
        int total = below + 1 + above;
        state.floorCount = total;
        state.firstFloorIndex = startIndex;
        state.ownFloorIndex = index;
        if(state.floorColors == null || state.floorColors.length < total)
            state.floorColors = new DyeColor[total];
        if(state.floorNames == null || state.floorNames.length < total)
            state.floorNames = new String[total];
        if(state.floorCageAvailable == null || state.floorCageAvailable.length < total)
            state.floorCageAvailable = new boolean[total];
        Vec3 buttonPos = new Vec3(entity.getBlockPos().getX() + 0.5, entity.getBlockPos().getY() + 0.5 * state.displayHeight - state.floorCount * DisplayBlock.BUTTON_HEIGHT / 2d, entity.getBlockPos().getZ() + 0.5);
        Vec3 cameraPos = context.cameraPos();
        for(int i = 0; i < total; i++){
            int floor = startIndex + i;
            state.floorColors[i] = group.getFloorDisplayColor(floor);
            state.floorNames[i] = cameraPos.distanceToSqr(buttonPos) < TEXT_RENDER_DISTANCE ? // text rendering is VERY slow, so only draw it within a certain distance
                MovingElevatorsClient.formatFloorDisplayName(group.getFloorDisplayName(floor), floor) : null;
            state.floorCageAvailable[i] = group.isCageAvailableAt(floor);
            buttonPos = buttonPos.add(0, DisplayBlock.BUTTON_HEIGHT, 0);
        }

        // Get platform dot offset
        double platformY = group.getCurrentY();
        if(group.isMoving() && platformY >= group.getFloorYLevel(0) && platformY < group.getFloorYLevel(group.getFloorCount() - 1)){
            state.showPlatformDot = true;
            double yOffset = 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d;
            for(int i = 0; i < group.getFloorCount() - 1; i++){
                int belowY = group.getFloorYLevel(i);
                int aboveY = group.getFloorYLevel(i + 1);
                if(platformY >= belowY && platformY < aboveY)
                    yOffset += (i + (platformY - belowY) / (aboveY - belowY)) * DisplayBlock.BUTTON_HEIGHT;
            }
            state.platformDotOffset = yOffset;
        }else
            state.showPlatformDot = false;
    }

    @Override
    public void submit(SubmitNodeCollector output, State state, RenderContext context){
        if(!state.shouldRender)
            return;

        Direction facing = state.facing;

        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternionf().setAngleAxis((180 - facing.toYRot()) / 180 * Math.PI, 0, 1, 0));
        poseStack.translate(-0.5, -0.5, -0.51);

        // Submit background
        int lighting = state.frontLighting;
        int overlay = state.combinedOverlay;
        if(state.displayHeight == 1)
            this.drawOverlayPart(poseStack, output, false, lighting, overlay, facing, 0, 0, 1, 1, 0, 0, 32, 32);
        else
            this.drawOverlayPart(poseStack, output, false, lighting, overlay, facing, 0, 0, 1, 2, 32, 0, 32, 64);

        // Submit buttons
        poseStack.pushPose();
        poseStack.translate(0, 0.5 * state.displayHeight - state.floorCount * DisplayBlock.BUTTON_HEIGHT / 2d, -0.002);
        poseStack.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < state.floorCount; i++){
            DyeColor labelColor = state.floorColors[i];
            this.drawOverlayPart(poseStack, output, false, lighting, overlay, facing, 0, 0, 1, 1, state.firstFloorIndex + i == state.ownFloorIndex ? 96 : 64, labelColor.getId() * 4, 32, 4);
            poseStack.translate(0, 1, 0);
        }
        poseStack.popPose();

        // Submit floor cage availability dots
        poseStack.pushPose();
        poseStack.translate(1 - (27.5 / 32d + DisplayBlock.BUTTON_HEIGHT / 2d), 0.5 * state.displayHeight - state.floorCount * DisplayBlock.BUTTON_HEIGHT / 2d, -0.004);
        poseStack.scale(DisplayBlock.BUTTON_HEIGHT, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < state.floorCount; i++){
            if(state.floorCageAvailable[i])
                this.drawOverlayPart(poseStack, output, true, lighting, overlay, facing, 0, 0, 1, 1, 0, state.showPlatformDot ? 42 : 32, 10, 10);
            poseStack.translate(0, 1, 0);
        }
        poseStack.popPose();

        // Submit platform dot
        if(state.showPlatformDot){
            poseStack.pushPose();
            poseStack.translate(1 - (27.5 / 32d + DisplayBlock.BUTTON_HEIGHT / 2d), state.platformDotOffset, -0.006);
            poseStack.scale(DisplayBlock.BUTTON_HEIGHT, DisplayBlock.BUTTON_HEIGHT, 1);
            this.drawOverlayPart(poseStack, output, true, lighting, overlay, facing, 0, 0, 1, 1, 10, 32, 10, 10);
            poseStack.popPose();
        }

        // Submit floor names
        poseStack.pushPose();
        poseStack.translate(18.5 / 32d, 0.5 * state.displayHeight - state.floorCount * DisplayBlock.BUTTON_HEIGHT / 2d, -0.008);
        poseStack.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < state.floorCount; i++){
            String floorName = state.floorNames[i];
            if(floorName == null)
                continue;
            this.drawString(poseStack, output, lighting, floorName);
            poseStack.translate(0, 1, 0);
        }
        poseStack.popPose();

        poseStack.popPose();
    }

    private void drawOverlayPart(PoseStack poseStack, SubmitNodeCollector output, boolean transparent, int combinedLight, int combinedOverlay, Direction facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        RenderType renderType = transparent ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockSheet();
        output.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
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

    private void drawString(PoseStack poseStack, SubmitNodeCollector output, int combinedLight, String s){
        Font fontRenderer = ClientUtils.getMinecraft().font;
        poseStack.pushPose();
        poseStack.translate(0, 0.07, -0.005);
        poseStack.scale(-0.01f, -0.08f, 1);
        output.submitText(poseStack, -fontRenderer.width(s) / 2f, -fontRenderer.lineHeight, FormattedCharSequence.forward(s, Style.EMPTY), false, Font.DisplayMode.NORMAL, combinedLight, 0xffffffff, 0, 0);
        poseStack.popPose();
    }

    public static class State {
        boolean shouldRender;
        Direction facing;
        int displayHeight;
        int frontLighting;
        int combinedOverlay;
        int floorCount, firstFloorIndex, ownFloorIndex;
        DyeColor[] floorColors;
        String[] floorNames;
        boolean[] floorCageAvailable;
        boolean showPlatformDot;
        double platformDotOffset;
    }
}
