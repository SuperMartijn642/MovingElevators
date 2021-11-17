package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
        ElevatorGroupCapability groups = Minecraft.getInstance().level.getCapability(ElevatorGroupCapability.CAPABILITY).orElse(null);
        if(groups == null)
            return;

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        e.getMatrixStack().pushPose();
        Vec3 matrix = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        e.getMatrixStack().translate(-matrix.x, -matrix.y, -matrix.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, group.getCurrentY(), group.z);
            if(elevatorPos.distSqr(Minecraft.getInstance().player.blockPosition()) < RENDER_DISTANCE)
                renderGroup(e.getMatrixStack(), group, buffer, e.getPartialTicks());
        }
        e.getMatrixStack().popPose();
    }

    public static void renderGroup(PoseStack matrixStack, ElevatorGroup group, MultiBufferSource buffer, float partialTicks){
        if(!group.isMoving() || group.getCurrentY() == group.getLastY())
            return;
        BlockState[][] state = group.getPlatform();
        int size = group.getSize();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double y = lastY + (currentY - lastY) * partialTicks;
        int startX = group.x + group.facing.getStepX() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = group.z + group.facing.getStepZ() * (int)Math.ceil(size / 2f) - size / 2;

        BlockPos topPos = new BlockPos(group.x, y, group.z).relative(group.facing, (int)Math.ceil(size / 2f));
        int currentLight = LevelRenderer.getLightColor(group.world, topPos);

        for(int x = 0; x < size; x++){
            for(int z = 0; z < size; z++){
                matrixStack.pushPose();

                matrixStack.translate(startX + x, y, startZ + z);

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state[x][z], matrixStack, buffer, currentLight, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

                matrixStack.popPose();
            }
        }
    }

}
