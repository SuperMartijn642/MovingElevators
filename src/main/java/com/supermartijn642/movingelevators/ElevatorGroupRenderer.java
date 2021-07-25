package com.supermartijn642.movingelevators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
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

        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        e.getMatrixStack().pushPose();
        Vec3 matrix = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        e.getMatrixStack().translate(-matrix.x, -matrix.y, -matrix.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, group.getCurrentY(), group.z);
            if(elevatorPos.distSqr(Minecraft.getInstance().player.blockPosition()) < RENDER_DISTANCE)
                renderGroup(e.getMatrixStack(), group, buffer, e.getPartialTicks());
        }
        e.getMatrixStack().popPose();
        buffer.endBatch();
    }

    public static void renderGroup(PoseStack matrixStack, ElevatorGroup group, MultiBufferSource.BufferSource buffer, float partialTicks){
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

                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state[x][z]);
                Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(), buffer.getBuffer(RenderType.translucent()), state[x][z], model, 1, 1, 1, currentLight, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

                matrixStack.popPose();
            }
        }
    }

}
