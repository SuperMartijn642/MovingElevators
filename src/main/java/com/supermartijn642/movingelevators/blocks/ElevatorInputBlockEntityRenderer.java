package com.supermartijn642.movingelevators.blocks;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import org.lwjgl.opengl.GL11;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputBlockEntityRenderer<T extends ElevatorInputBlockEntity> implements CustomBlockEntityRenderer<T> {

    @Override
    public void render(T entity, float partialTicks, int combinedOverlay){
        if(!entity.hasGroup() || entity.getFacing() == null || (entity instanceof ControllerBlockEntity && !((ControllerBlockEntity)entity).shouldShowButtons()))
            return;

        // render buttons
        Direction facing = entity.getFacing();
        int combinedLight = entity.getLevel().getLightColor(entity.getBlockPos().relative(facing), entity.getLevel().getBlockState(entity.getBlockPos().relative(facing)).getLightValue(entity.getLevel(), entity.getBlockPos().relative(facing)));

        ScreenUtils.bindTexture(AtlasTexture.LOCATION_BLOCKS);

        GlStateManager.pushMatrix();

        GlStateManager.translated(0.5, 0.5, 0.5);
        GlStateManager.rotated(180 - facing.toYRot(), 0, 1, 0);
        GlStateManager.translated(-0.5, -0.5, -0.51);
        GlStateManager.normal3f(0, 1, 0);

        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        ElevatorGroup group = entity.getGroup();
        int floorNumber = group.getFloorNumber(entity.getFloorLevel()), floorCount = group.getFloorCount();
        this.drawOverlayPart(buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, floorNumber < floorCount - 1 ? 64 : 87, 23, 23);
        this.drawOverlayPart(buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 23, 64, 23, 23);
        this.drawOverlayPart(buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 46, floorNumber > 0 ? 64 : 87, 23, 23);

        Tessellator.getInstance().end();

        GlStateManager.popMatrix();
    }

    private void drawOverlayPart(BufferBuilder buffer, int combinedLight, int combinedOverlay, Direction facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        float minU = MovingElevatorsClient.OVERLAY_SPRITE.getU(tX / 8f), maxU = MovingElevatorsClient.OVERLAY_SPRITE.getU((tX + tWidth) / 8f);
        float minV = MovingElevatorsClient.OVERLAY_SPRITE.getV(tY / 8f), maxV = MovingElevatorsClient.OVERLAY_SPRITE.getV((tY + tHeight) / 8f);
        int k = combinedLight >> 16 & '\uffff';
        int l = combinedLight & '\uffff';

        buffer.vertex(x, y + height, 0).color(255, 255, 255, 255).uv(maxU, minV).uv2(k, l).endVertex();
        buffer.vertex(x + width, y + height, 0).color(255, 255, 255, 255).uv(minU, minV).uv2(k, l).endVertex();
        buffer.vertex(x + width, y, 0).color(255, 255, 255, 255).uv(minU, maxV).uv2(k, l).endVertex();
        buffer.vertex(x, y, 0).color(255, 255, 255, 255).uv(maxU, maxV).uv2(k, l).endVertex();
    }
}
