package com.supermartijn642.movingelevators.elevator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class ElevatorGroupRenderer {

    public static final double RENDER_DISTANCE = 255 * 255 * 4;
    /**
     * Don't render anything when Iris is rendering shadows. For some reason that *sometimes* leads to issues
     */
    public static boolean isIrisRenderingShadows = false;

    public static void registerEventListeners(){
        RenderWorldEvent.EVENT.register(ElevatorGroupRenderer::onRender);
    }

    public static void onRender(RenderWorldEvent e){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        e.getPoseStack().pushPose();
        Vec3 camera = RenderUtils.getCameraPosition();
        e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, (int)group.getCurrentY(), group.z);
            if(elevatorPos.distSqr(ClientUtils.getPlayer().blockPosition()) < RENDER_DISTANCE
                && ClientUtils.getMinecraft().getEntityRenderDispatcher().shouldRenderHitBoxes())
                renderGroupCageOutlines(e.getPoseStack(), group);
        }
        e.getPoseStack().popPose();
    }

    public static void renderBlocks(PoseStack poseStack, RenderType renderType, MultiBufferSource bufferSource){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        poseStack.pushPose();
        Vec3 camera = RenderUtils.getCameraPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            if(group.isMoving()){
                BlockPos elevatorPos = new BlockPos(group.x, (int)group.getCurrentY(), group.z);
                if(elevatorPos.distSqr(ClientUtils.getPlayer().blockPosition()) < RENDER_DISTANCE)
                    renderGroupBlocks(poseStack, group, renderType, buffer, ClientUtils.getPartialTicks());
            }
        }
        poseStack.popPose();

        // For some reason this is needed ¯\(o_o)/¯
        if(renderType == RenderType.translucent())
            ((MultiBufferSource.BufferSource)bufferSource).endBatch(renderType);
    }

    public static void renderBlockEntities(PoseStack poseStack, float partialTicks, MultiBufferSource bufferSource){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        poseStack.pushPose();
        Vec3 camera = RenderUtils.getCameraPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            if(group.isMoving()){
                BlockPos elevatorPos = new BlockPos(group.x, (int)group.getCurrentY(), group.z);
                if(elevatorPos.distSqr(ClientUtils.getPlayer().blockPosition()) < RENDER_DISTANCE)
                    renderGroupBlockEntities(poseStack, group, bufferSource, partialTicks);
            }
        }
        poseStack.popPose();
    }

    public static void renderGroupBlocks(PoseStack poseStack, ElevatorGroup group, RenderType renderType, VertexConsumer buffer, float partialTicks){
        ClientElevatorCage cage = (ClientElevatorCage)group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3 startPos = group.getCageAnchorPos(renderY);
        BlockPos anchorPos = new BlockPos((int)startPos.x, (int)startPos.y, (int)startPos.z);
        cage.loadRenderInfo(anchorPos, group);
        Level level = ClientElevatorCage.getFakeLevel();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockStates[x][y][z] == null)
                        continue;

                    poseStack.pushPose();
                    poseStack.translate(startPos.x + x, startPos.y + y, startPos.z + z);

                    BlockState state = cage.blockStates[x][y][z];
                    if(state.getRenderShape() == RenderShape.MODEL && ItemBlockRenderTypes.getChunkRenderType(state) == renderType){
                        pos.set(anchorPos.getX() + x, anchorPos.getY() + y, anchorPos.getZ() + z);
                        ClientUtils.getBlockRenderer().renderBatched(state, pos, level, poseStack, buffer, true, level.random);
                    }
                    poseStack.popPose();
                }
            }
        }
    }

    public static void renderGroupBlockEntities(PoseStack poseStack, ElevatorGroup group, MultiBufferSource buffer, float partialTicks){
        ClientElevatorCage cage = (ClientElevatorCage)group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3 startPos = group.getCageAnchorPos(renderY);
        BlockPos anchorPos = new BlockPos((int)startPos.x, (int)startPos.y, (int)startPos.z);
        cage.loadRenderInfo(anchorPos, group);

        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockEntities[x][y][z] == null)
                        continue;

                    poseStack.pushPose();
                    poseStack.translate(startPos.x + x, startPos.y + y, startPos.z + z);

                    BlockEntity entity = cage.blockEntities[x][y][z];
                    ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().render(entity, partialTicks, poseStack, buffer);

                    poseStack.popPose();
                }
            }
        }
    }

    public static void renderGroupCageOutlines(PoseStack poseStack, ElevatorGroup group){
        for(int floor = 0; floor < group.getFloorCount(); floor++){
            BlockPos anchorPos = group.getCageAnchorBlockPos(group.getFloorYLevel(floor));
            AABB cageArea = new AABB(anchorPos, anchorPos.offset(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ()));
            cageArea.inflate(0.01);
            RenderUtils.renderBox(poseStack, cageArea, 1, 1, 1, true);
        }
        if(group.isMoving()){
            ElevatorCage cage = group.getCage();
            double lastY = group.getLastY(), currentY = group.getCurrentY();
            double renderY = lastY + (currentY - lastY) * ClientUtils.getPartialTicks();
            Vec3 startPos = group.getCageAnchorPos(renderY);
            RenderUtils.renderBox(poseStack, new AABB(startPos, startPos.add(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ())), 1, 0, 0, true);
            RenderUtils.renderShape(poseStack, cage.shape.move(startPos.x, startPos.y, startPos.z), 49 / 255f, 224 / 255f, 219 / 255f, true);
        }
    }
}
