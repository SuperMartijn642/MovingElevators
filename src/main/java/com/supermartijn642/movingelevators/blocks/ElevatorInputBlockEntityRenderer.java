package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ElevatorInputBlockEntityRenderer<T extends ElevatorInputBlockEntity> implements CustomBlockEntityRenderer<T> {

    @Override
    public void render(T entity, float partialTicks, int combinedOverlay, float alpha){
        if(!entity.hasGroup() || entity.getFacing() == null || (entity instanceof ControllerBlockEntity && !((ControllerBlockEntity)entity).shouldShowButtons()))
            return;

        // render buttons
        EnumFacing facing = entity.getFacing();
        int combinedLight = entity.getWorld().getCombinedLight(entity.getPos().offset(facing), entity.getWorld().getBlockState(entity.getPos().offset(facing)).getLightValue(entity.getWorld(), entity.getPos().offset(facing)));

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());

        GlStateManager.pushMatrix();

        GlStateManager.translate(0.5, 0.5, 0.5);
        GlStateManager.rotate(180 - facing.getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translate(-0.5, -0.5, -0.51);
        GlStateManager.glNormal3f(0, 1, 0);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        ElevatorGroup group = entity.getGroup();
        int floorNumber = group.getFloorNumber(entity.getFloorLevel()), floorCount = group.getFloorCount();
        this.drawOverlayPart(buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 0, floorNumber < floorCount - 1 ? 64 : 87, 23, 23);
        this.drawOverlayPart(buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 23, 64, 23, 23);
        this.drawOverlayPart(buffer, combinedLight, combinedOverlay, facing, 0, 0, 1, 1, 46, floorNumber > 0 ? 64 : 87, 23, 23);

        Tessellator.getInstance().draw();

        GlStateManager.popMatrix();
    }

    private void drawOverlayPart(BufferBuilder buffer, int combinedLight, int combinedOverlay, EnumFacing facing, float x, float y, float width, float height, int tX, int tY, int tWidth, int tHeight){
        float minU = MovingElevatorsClient.OVERLAY_SPRITE.getInterpolatedU(tX / 8f), maxU = MovingElevatorsClient.OVERLAY_SPRITE.getInterpolatedU((tX + tWidth) / 8f);
        float minV = MovingElevatorsClient.OVERLAY_SPRITE.getInterpolatedV(tY / 8f), maxV = MovingElevatorsClient.OVERLAY_SPRITE.getInterpolatedV((tY + tHeight) / 8f);
        int k = combinedLight >> 16 & '\uffff';
        int l = combinedLight & '\uffff';

        buffer.pos(x, y + height, 0).color(255, 255, 255, 255).tex(maxU, minV).lightmap(k, l).endVertex();
        buffer.pos(x + width, y + height, 0).color(255, 255, 255, 255).tex(minU, minV).lightmap(k, l).endVertex();
        buffer.pos(x + width, y, 0).color(255, 255, 255, 255).tex(minU, maxV).lightmap(k, l).endVertex();
        buffer.pos(x, y, 0).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(k, l).endVertex();
    }
}
