package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElevatorGroupRenderer {

    public static final double RENDER_DISTANCE = 255 * 255 * 4;

    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent e){
        ElevatorGroupCapability groups = Minecraft.getInstance().world.getCapability(ElevatorGroupCapability.CAPABILITY).orElse(null);
        if(groups == null)
            return;

        GlStateManager.pushMatrix();
        Vec3d matrix = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        GlStateManager.translated(-matrix.x, -matrix.y, -matrix.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, group.getCurrentY(), group.z);
            if(elevatorPos.distanceSq(Minecraft.getInstance().player.getPosition()) < RENDER_DISTANCE)
                renderGroup(group, e.getPartialTicks());
        }
        GlStateManager.popMatrix();
    }

    public static void renderGroup(ElevatorGroup group, float partialTicks){
        if(!group.isMoving() || group.getCurrentY() == group.getLastY())
            return;
        BlockState[][] state = group.getPlatform();
        int size = group.getSize();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double y = lastY + (currentY - lastY) * partialTicks;
        int startX = group.x + group.facing.getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = group.z + group.facing.getZOffset() * (int)Math.ceil(size / 2f) - size / 2;

        for(int x = 0; x < size; x++){
            for(int z = 0; z < size; z++){
                BlockPos pos = new BlockPos(startX + x, y, startZ + z);

                GlStateManager.pushMatrix();

                GlStateManager.translated(startX + x, y, startZ + z);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                GlStateManager.disableLighting();

                Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

                try{
                    BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
                    IBakedModel model = brd.getModelForState(state[x][z]);
                    brd.getBlockModelRenderer().renderModel(group.world, model, state[x][z], BlockPos.ZERO, buffer, false, new Random(), 0, EmptyModelData.INSTANCE);
                }catch(Exception e){
                    e.printStackTrace();
                }

                tessellator.draw();

                GlStateManager.popMatrix();
            }
        }
    }

}
