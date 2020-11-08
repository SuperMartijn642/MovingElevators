package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        e.getMatrixStack().push();
        Vector3d matrix = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        e.getMatrixStack().translate(-matrix.x, -matrix.y, -matrix.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, group.getCurrentY(), group.z);
            if(elevatorPos.distanceSq(Minecraft.getInstance().player.getPosition()) < RENDER_DISTANCE)
                renderGroup(e.getMatrixStack(), group, buffer, e.getPartialTicks());
        }
        e.getMatrixStack().pop();
        buffer.finish();
    }

    public static void renderGroup(MatrixStack matrixStack, ElevatorGroup group, IRenderTypeBuffer.Impl buffer, float partialTicks){
        if(!group.isMoving() || group.getCurrentY() == group.getLastY())
            return;
        BlockState[][] state = group.getPlatform();
        int size = group.getSize();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double y = lastY + (currentY - lastY) * partialTicks;
        int startX = group.x + group.facing.getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = group.z + group.facing.getZOffset() * (int)Math.ceil(size / 2f) - size / 2;

        BlockPos topPos = new BlockPos(group.x, y, group.z).offset(group.facing, (int)Math.ceil(size / 2f));
        int currentLight = WorldRenderer.getCombinedLight(group.world, topPos);

        for(int x = 0; x < size; x++){
            for(int z = 0; z < size; z++){
                matrixStack.push();

                matrixStack.translate(startX + x, y, startZ + z);

                Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state[x][z], matrixStack, buffer, currentLight, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

                matrixStack.pop();
            }
        }
    }

}
