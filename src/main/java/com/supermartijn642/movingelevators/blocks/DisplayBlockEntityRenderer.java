package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * Created 13/02/2022 by SuperMartijn642
 */
public class DisplayBlockEntityRenderer implements CustomBlockEntityRenderer<DisplayBlockEntity> {

    private static final double TEXT_RENDER_DISTANCE = 15 * 15;

    @Override
    public void render(DisplayBlockEntity entity, float partialTicks, int combinedOverlay, float alpha){
        if(!entity.isBottomDisplay() || !entity.getInputBlockEntity().hasGroup())
            return;

        int height = entity.hasDisplayOnTop() ? 2 : 1;
        World level = entity.getWorld();
        EnumFacing facing = entity.getFacing();
        ElevatorGroup group = entity.getElevatorGroup();
        BlockPos frontPos = entity.getPos().offset(facing);
        int combinedLight;
        if(height == 1)
            combinedLight = entity.getWorld().getCombinedLight(frontPos, entity.getWorld().getBlockState(frontPos).getLightValue(entity.getWorld(), frontPos));
        else{
            int skyLight = Math.max(level.getLightFor(EnumSkyBlock.SKY, frontPos), level.getLightFor(EnumSkyBlock.SKY, frontPos.up()));
            int blockLight = Math.max(level.getLightFor(EnumSkyBlock.BLOCK, frontPos), level.getLightFor(EnumSkyBlock.BLOCK, frontPos.up()));
            int blockStateLight = Math.max(level.getBlockState(frontPos).getLightValue(level, frontPos), level.getBlockState(frontPos.up()).getLightValue(level, frontPos.up()));
            blockLight = Math.max(blockLight, blockStateLight);
            combinedLight = skyLight << 20 | blockLight << 4;
        }

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());

        GlStateManager.pushMatrix();

        GlStateManager.translate(0.5, 0.5, 0.5);
        GlStateManager.rotate(180 - facing.getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translate(-0.5, -0.5, -0.51);

        // render background
        if(height == 1)
            this.drawOverlayPart(combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, 0, 32, 32);
        else
            this.drawOverlayPart(combinedLight, combinedOverlay, facing, 0, 0, 1, 2, 32, 0, 32, 64);

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
        Vec3d buttonPos = new Vec3d(entity.getPos().getX() + 0.5, entity.getPos().getY() + 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, entity.getPos().getZ() + 0.5);
        Vec3d cameraPos = RenderUtils.getCameraPosition();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.5 * height - total * DisplayBlock.BUTTON_HEIGHT / 2d, -0.002);
        GlStateManager.scale(1, DisplayBlock.BUTTON_HEIGHT, 1);
        for(int i = 0; i < total; i++){
            EnumDyeColor labelColor = group.getFloorDisplayColor(startIndex + i);
            this.drawOverlayPart(combinedLight, combinedOverlay, facing, 0, 0, 1, 1, startIndex + i == index ? 96 : 64, labelColor.getMetadata() * 4, 32, 4);

            boolean drawText = cameraPos.squareDistanceTo(buttonPos) < TEXT_RENDER_DISTANCE; // text rendering is VERY slow apparently, so only draw it within a certain distance
            if(drawText){
                GlStateManager.pushMatrix();
                GlStateManager.translate(18.5 / 32d, 0, 0);
                this.drawString(combinedLight, MovingElevatorsClient.formatFloorDisplayName(group.getFloorDisplayName(startIndex + i), startIndex + i));
                GlStateManager.popMatrix();
                ScreenUtils.bindTexture(TextureAtlases.getBlocks());
            }
            GlStateManager.translate(0, 1, 0);
            buttonPos = buttonPos.addVector(0, DisplayBlock.BUTTON_HEIGHT, 0);
        }
        GlStateManager.popMatrix();

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
                GlStateManager.translate(1 - (27.5 / 32d + DisplayBlock.BUTTON_HEIGHT / 2d), yOffset, -0.003);
                GlStateManager.scale(DisplayBlock.BUTTON_HEIGHT, DisplayBlock.BUTTON_HEIGHT, 1);
                this.drawOverlayPart(combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, 32, 10, 10);
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawOverlayPart(int combinedLight, int combinedOverlay, EnumFacing facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        float minU = MovingElevatorsClient.OVERLAY_SPRITE.getInterpolatedU(tX / 8f), maxU = MovingElevatorsClient.OVERLAY_SPRITE.getInterpolatedU((tX + tWidth) / 8f);
        float minV = MovingElevatorsClient.OVERLAY_SPRITE.getInterpolatedV(tY / 8f), maxV = MovingElevatorsClient.OVERLAY_SPRITE.getInterpolatedV((tY + tHeight) / 8f);
        int k = combinedLight >> 16 & '\uffff';
        int l = combinedLight & '\uffff';

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.pos(x, y + height, 0).color(255, 255, 255, 255).tex(maxU, minV).lightmap(k, l).endVertex();
        buffer.pos(x + width, y + height, 0).color(255, 255, 255, 255).tex(minU, minV).lightmap(k, l).endVertex();
        buffer.pos(x + width, y, 0).color(255, 255, 255, 255).tex(minU, maxV).lightmap(k, l).endVertex();
        buffer.pos(x, y, 0).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(k, l).endVertex();
        Tessellator.getInstance().draw();
    }

    private void drawString(int combinedLight, String s){
        if(s == null)
            return;
        FontRenderer fontRenderer = ClientUtils.getFontRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.07, -0.005);
        GlStateManager.scale(-0.01f, -0.08f, 1);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        fontRenderer.drawString(s, -fontRenderer.getStringWidth(s) / 2, -fontRenderer.FONT_HEIGHT, EnumDyeColor.WHITE.getColorValue());
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
