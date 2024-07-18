package com.supermartijn642.movingelevators.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Created 13/02/2022 by SuperMartijn642
 */
public class DisplayBlockEntityRenderer implements CustomBlockEntityRenderer<DisplayBlockEntity> {

    private static final double TEXT_RENDER_DISTANCE = 15 * 15;

    @Override
    public void render(DisplayBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(!entity.isBottomDisplay() || !entity.getInputBlockEntity().hasGroup())
            return;

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());

        int height = entity.hasDisplayOnTop() ? 2 : 1;
        Level level = entity.getLevel();
        Direction facing = entity.getFacing();
        ElevatorGroup group = entity.getElevatorGroup();
        BlockPos frontPos = entity.getBlockPos().relative(facing);
        if(height == 1)
            combinedLight = LevelRenderer.getLightColor(level, frontPos);
        else if(level.getBlockState(frontPos).emissiveRendering(level, frontPos) || level.getBlockState(frontPos.above()).emissiveRendering(level, frontPos.above()))
            combinedLight = 15728880;
        else{
            int skyLight = Math.max(level.getBrightness(LightLayer.SKY, frontPos), level.getBrightness(LightLayer.SKY, frontPos.above()));
            int blockLight = Math.max(level.getBrightness(LightLayer.BLOCK, frontPos), level.getBrightness(LightLayer.BLOCK, frontPos.above()));
            int blockStateLight = Math.max(level.getBlockState(frontPos).getLightEmission(), level.getBlockState(frontPos.above()).getLightEmission());
            blockLight = Math.max(blockLight, blockStateLight);
            combinedLight = skyLight << 20 | blockLight << 4;
        }

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternionf().setAngleAxis((180 - facing.toYRot()) / 180 * Math.PI, 0, 1, 0));
        poseStack.translate(-0.5, -0.5, -0.51);

        // render background
        if(height == 1)
            this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, 0, 32, 32);
        else
            this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 2, 32, 0, 32, 64);

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

        // render buttons
        poseStack.pushPose();
        poseStack.translate(0, 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, -0.002);
        poseStack.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < total; i++){
            DyeColor labelColor = group.getFloorDisplayColor(startIndex + i);
            this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, startIndex + i == index ? 96 : 64, labelColor.getId() * 4, 32, 4);
            poseStack.translate(0, 1, 0);
        }
        poseStack.popPose();

        // render platform dot
        if(group.isMoving()){
            double platformY = group.getCurrentY();
            if(platformY >= group.getFloorYLevel(0) && platformY < group.getFloorYLevel(group.getFloorCount() - 1)){
                double yOffset = 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d;
                for(int i = 0; i < group.getFloorCount() - 1; i++){
                    int belowY = group.getFloorYLevel(i);
                    int aboveY = group.getFloorYLevel(i + 1);
                    if(platformY >= belowY && platformY < aboveY)
                        yOffset += (i + (platformY - belowY) / (aboveY - belowY)) * DisplayBlock.BUTTON_HEIGHT;
                }
                poseStack.pushPose();
                poseStack.translate(1 - (27.5 / 32d + DisplayBlock.BUTTON_HEIGHT / 2d), yOffset, -0.003);
                poseStack.scale(DisplayBlock.BUTTON_HEIGHT, DisplayBlock.BUTTON_HEIGHT, 1);
                this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, 32, 10, 10);
                poseStack.popPose();
            }
        }

        // Render floor names
        Vec3 buttonPos = new Vec3(entity.getBlockPos().getX() + 0.5, entity.getBlockPos().getY() + 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, entity.getBlockPos().getZ() + 0.5);
        Vec3 cameraPos = RenderUtils.getCameraPosition();
        poseStack.pushPose();
        poseStack.translate(0, 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, -0.002);
        poseStack.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < total; i++){
            boolean drawText = cameraPos.distanceToSqr(buttonPos) < TEXT_RENDER_DISTANCE; // text rendering is VERY slow apparently, so only draw it within a certain distance
            if(drawText){
                poseStack.pushPose();
                poseStack.translate(18.5 / 32d, 0, 0);
                this.drawString(poseStack, bufferSource, combinedLight, MovingElevatorsClient.formatFloorDisplayName(group.getFloorDisplayName(startIndex + i), startIndex + i));
                poseStack.popPose();
            }
            poseStack.translate(0, 1, 0);
            buttonPos = buttonPos.add(0, DisplayBlock.BUTTON_HEIGHT, 0);
        }
        poseStack.popPose();

        poseStack.popPose();
    }

    private void drawOverlayPart(PoseStack poseStack, VertexConsumer buffer, int combinedLight, int combinedOverlay, Direction facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        Matrix4f matrix = poseStack.last().pose();

        TextureAtlasSprite overlaySprite = MovingElevatorsClient.getOverlaySprite();
        float minU = overlaySprite.getU(tX / 128f), maxU = overlaySprite.getU((tX + tWidth) / 128f);
        float minV = overlaySprite.getV(tY / 128f), maxV = overlaySprite.getV((tY + tHeight) / 128f);

        buffer.addVertex(matrix, x, y + height, 0).setColor(255, 255, 255, 255).setUv(maxU, minV).setLight(combinedLight).setNormal(poseStack.last(), facing.getStepX(), facing.getStepY(), facing.getStepZ()).setOverlay(combinedOverlay);
        buffer.addVertex(matrix, x + width, y + height, 0).setColor(255, 255, 255, 255).setUv(minU, minV).setLight(combinedLight).setNormal(poseStack.last(), facing.getStepX(), facing.getStepY(), facing.getStepZ()).setOverlay(combinedOverlay);
        buffer.addVertex(matrix, x + width, y, 0).setColor(255, 255, 255, 255).setUv(minU, maxV).setLight(combinedLight).setNormal(poseStack.last(), facing.getStepX(), facing.getStepY(), facing.getStepZ()).setOverlay(combinedOverlay);
        buffer.addVertex(matrix, x, y, 0).setColor(255, 255, 255, 255).setUv(maxU, maxV).setLight(combinedLight).setNormal(poseStack.last(), facing.getStepX(), facing.getStepY(), facing.getStepZ()).setOverlay(combinedOverlay);
    }

    private void drawString(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, String s){
        if(s == null)
            return;
        Font fontRenderer = ClientUtils.getMinecraft().font;
        poseStack.pushPose();
        poseStack.translate(0, 0.07, -0.005);
        poseStack.scale(-0.01f, -0.08f, 1);
        fontRenderer.drawInBatch(s, -fontRenderer.width(s) / 2f, -fontRenderer.lineHeight, 0xffffffff, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();
    }
}
