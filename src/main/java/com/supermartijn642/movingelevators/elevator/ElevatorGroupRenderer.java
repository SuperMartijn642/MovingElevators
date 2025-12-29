package com.supermartijn642.movingelevators.elevator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class ElevatorGroupRenderer {

    private static Vec3 cameraPosition = Vec3.ZERO;
    private static int groupsToRender = 0;
    private static List<GroupRenderState> groupRenderStates = new ArrayList<>();

    public static void registerEventListeners(){
        RenderWorldEvent.EVENT.register(ElevatorGroupRenderer::onRender);
    }

    private static boolean isWithinRenderDistance(ElevatorGroup group){
        GameRenderer renderer = ClientUtils.getMinecraft().gameRenderer;
        if(renderer == null)
            return false;
        float renderDistance = renderer.getRenderDistance() + 8 + group.getCageSizeX() / 2f + group.getCageSizeZ() / 2f;
        BlockPos playerPos = ClientUtils.getPlayer().blockPosition();
        float distance = (group.x - playerPos.getX()) * (group.x - playerPos.getX()) + (group.z - playerPos.getZ()) * (group.z - playerPos.getZ());
        return distance < renderDistance * renderDistance;
    }

    public static void onRender(RenderWorldEvent e){
        if(!ClientUtils.getMinecraft().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES))
            return;
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        e.getPoseStack().pushPose();
        Vec3 camera = RenderUtils.getCameraPosition();
        e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            if(isWithinRenderDistance(group))
                renderGroupCageOutlines(e.getPoseStack(), group);
        }
        e.getPoseStack().popPose();
    }

    public static void extractRenderState(){
        cameraPosition = RenderUtils.getCameraPosition();
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());
        float partialTicks = ClientUtils.getPartialTicks();
        int index = 0;
        for(ElevatorGroup group : groups.getGroups()){
            if(group.isMoving() && isWithinRenderDistance(group)){
                if(index >= groupRenderStates.size())
                    groupRenderStates.add(new GroupRenderState());
                extractGroupRenderState(group, groupRenderStates.get(index), partialTicks);
                index++;
            }
        }
        groupsToRender = index;
    }

    private static void extractGroupRenderState(ElevatorGroup group, GroupRenderState state, float partialTicks){
        ClientElevatorCage cage = (ClientElevatorCage)group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3 startPos = group.getCageAnchorPos(renderY);
        BlockPos anchorPos = new BlockPos((int)startPos.x, (int)startPos.y, (int)startPos.z);
        cage.loadRenderInfo(anchorPos, group);

        state.entityCount = 0;
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockEntities[x][y][z] != null)
                        state.entityCount++;
                }
            }
        }
        if(state.entityPositions == null || state.entityCount > state.entityPositions.length){
            if(state.entityPositions == null){
                state.entityPositions = new Vector3f[state.entityCount];
                state.entityRenderStates = new BlockEntityRenderState[state.entityCount];
            }else{
                state.entityPositions = Arrays.copyOf(state.entityPositions, state.entityCount);
                state.entityRenderStates = Arrays.copyOf(state.entityRenderStates, state.entityCount);
            }
        }
        int index = 0;
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockEntities[x][y][z] == null)
                        continue;
                    state.entityCount++;
                    BlockEntity entity = cage.blockEntities[x][y][z];
                    BlockEntityRenderState entityRenderState = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().tryExtractRenderState(entity, partialTicks, null);
                    if(entityRenderState == null)
                        continue;
                    if(state.entityPositions[index] == null)
                        state.entityPositions[index] = new Vector3f();
                    state.entityPositions[index].set(startPos.x + x, startPos.y + y, startPos.z + z);
                    state.entityRenderStates[index] = entityRenderState;
                    index++;
                }
            }
        }
        state.entityCount = index;
    }

    public static void renderBlocks(PoseStack poseStack, ChunkSectionLayerGroup layers, MultiBufferSource bufferSource){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        poseStack.pushPose();
        Vec3 camera = RenderUtils.getCameraPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer buffer = null;
        Set<ChunkSectionLayer> layersSet = EnumSet.noneOf(ChunkSectionLayer.class);
        layersSet.addAll(Arrays.asList(layers.layers()));
        for(ElevatorGroup group : groups.getGroups()){
            if(group.isMoving() && isWithinRenderDistance(group)){
                if(buffer == null)
                    buffer = bufferSource.getBuffer(layers == ChunkSectionLayerGroup.TRANSLUCENT ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockSheet());
                renderGroupBlocks(poseStack, group, layersSet, buffer, ClientUtils.getPartialTicks());
            }
        }
        poseStack.popPose();
    }

    public static void renderBlockEntities(PoseStack poseStack, float partialTicks, CameraRenderState cameraRenderState, SubmitNodeStorage submitNodeStorage){
        if(groupsToRender == 0)
            return;

        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        for(int i = 0; i < groupsToRender; i++)
            renderGroupBlockEntities(poseStack, groupRenderStates.get(i), partialTicks, cameraRenderState, submitNodeStorage);
        poseStack.popPose();
    }

    public static void renderGroupBlocks(PoseStack poseStack, ElevatorGroup group, Set<ChunkSectionLayer> layers, VertexConsumer buffer, float partialTicks){
        ClientElevatorCage cage = (ClientElevatorCage)group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3 startPos = group.getCageAnchorPos(renderY);
        BlockPos anchorPos = new BlockPos((int)startPos.x, (int)startPos.y, (int)startPos.z);
        cage.loadRenderInfo(anchorPos, group);
        Level level = ClientElevatorCage.getFakeLevel();

        BlockRenderDispatcher blockRenderer = ClientUtils.getBlockRenderer();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockStates[x][y][z] == null)
                        continue;

                    poseStack.pushPose();
                    poseStack.translate(startPos.x + x, startPos.y + y, startPos.z + z);

                    BlockState state = cage.blockStates[x][y][z];
                    if(state.getRenderShape() == RenderShape.MODEL && layers.contains(ItemBlockRenderTypes.getChunkRenderType(state))){
                        pos.set(anchorPos.getX() + x, anchorPos.getY() + y, anchorPos.getZ() + z);
                        blockRenderer.renderBatched(state, pos, level, poseStack, buffer, true, blockRenderer.getBlockModel(state).collectParts(level.random));
                    }
                    poseStack.popPose();
                }
            }
        }
    }

    public static void renderGroupBlockEntities(PoseStack poseStack, GroupRenderState group, float partialTicks, CameraRenderState cameraRenderState, SubmitNodeStorage submitNodeStorage){
        if(group.entityCount == 0)
            return;
        for(int i = 0; i < group.entityCount; i++){
            Vector3f position = group.entityPositions[i];
            poseStack.pushPose();
            poseStack.translate(position.x, position.y, position.z);
            BlockEntityRenderState entityRenderState = group.entityRenderStates[i];
            ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().submit(entityRenderState, poseStack, submitNodeStorage, cameraRenderState);
            poseStack.popPose();
        }
    }

    public static void renderGroupCageOutlines(PoseStack poseStack, ElevatorGroup group){
        for(int floor = 0; floor < group.getFloorCount(); floor++){
            BlockPos anchorPos = group.getCageAnchorBlockPos(group.getFloorYLevel(floor));
            AABB cageArea = new AABB(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ(), anchorPos.getX() + group.getCageSizeX(), anchorPos.getY() + group.getCageSizeY(), anchorPos.getZ() + group.getCageSizeZ());
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

    private static class GroupRenderState {
        int entityCount;
        Vector3f[] entityPositions;
        BlockEntityRenderState[] entityRenderStates;
    }
}
