package com.supermartijn642.movingelevators.elevator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
public class ElevatorGroupRenderer {

    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    private static int groupsToRender = 0;
    private static final List<GroupRenderState> groupRenderStates = new ArrayList<>();

    public static void registerEventListeners(){
        RenderWorldEvent.EVENT.register(ElevatorGroupRenderer::onRender);
    }

    private static boolean isWithinRenderDistance(ElevatorGroup group){
        int renderDistance = ClientUtils.getMinecraft().options.getEffectiveRenderDistance() * 16;
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
        Level level = ClientUtils.getWorld();

        // Block models
        state.blockCount = group.getCageSizeX() * group.getCageSizeY() * group.getCageSizeZ();
        if(state.blockPositions == null || state.blockCount > state.blockPositions.length){
            if(state.blockPositions == null){
                state.blockPositions = new Vector3f[state.blockCount];
                state.blockRenderStates = new BlockModelRenderState[state.blockCount];
            }else{
                state.blockPositions = Arrays.copyOf(state.blockPositions, state.blockCount);
                state.blockRenderStates = Arrays.copyOf(state.blockRenderStates, state.blockCount);
            }
            state.blockLighting = new int[state.blockCount];
        }
        BlockPos.MutableBlockPos dummyBlockPos = new BlockPos.MutableBlockPos();
        int index = 0;
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    BlockState block = cage.blockStates[x][y][z];
                    if(block == null)
                        continue;
                    BlockModelRenderState blockRenderState = state.blockRenderStates[index];
                    if(blockRenderState == null)
                        blockRenderState = state.blockRenderStates[index] = new BlockModelRenderState();
                    else
                        blockRenderState.clear();
                    BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(block);
                    QuadEmitter emitter = blockRenderState.setupMesh(IDENTITY_MATRIX, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
                    dummyBlockPos.set(anchorPos.getX() + x, anchorPos.getY() + y, anchorPos.getZ() + z);
                    RANDOM_SOURCE.setSeed(block.getSeed(dummyBlockPos));
                    model.emitQuads(emitter, ClientElevatorCage.getFakeLevel(), dummyBlockPos, block, RANDOM_SOURCE, _ -> false);
                    IntList tintLayers = blockRenderState.tintLayers();
                    for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(block))
                        tintLayers.add(tintSource.colorInWorld(block, ClientElevatorCage.getFakeLevel(), dummyBlockPos));
                    state.blockLighting[index] = LightCoordsUtil.pack(
                        level.getBrightness(LightLayer.BLOCK, dummyBlockPos),
                        level.getBrightness(LightLayer.SKY, dummyBlockPos)
                    );
                    if(state.blockPositions[index] == null)
                        state.blockPositions[index] = new Vector3f();
                    state.blockPositions[index].set(startPos.x + x, startPos.y + y, startPos.z + z);
                    state.blockRenderStates[index] = blockRenderState;
                    index++;
                }
            }
        }
        state.blockCount = index;

        // Block entities
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
        index = 0;
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockEntities[x][y][z] == null)
                        continue;
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

    public static void submit(PoseStack poseStack, float partialTicks, CameraRenderState cameraRenderState, SubmitNodeStorage submitNodeStorage){
        if(groupsToRender == 0)
            return;

        poseStack.pushPose();
        Vec3 cameraPosition = cameraRenderState.pos;
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        for(int i = 0; i < groupsToRender; i++)
            submitGroup(poseStack, groupRenderStates.get(i), cameraRenderState, submitNodeStorage);
        poseStack.popPose();
    }

    private static void submitGroup(PoseStack poseStack, GroupRenderState group, CameraRenderState cameraRenderState, SubmitNodeStorage submitNodeStorage){
        // Block models
        for(int i = 0; i < group.blockCount; i++){
            Vector3f position = group.blockPositions[i];
            poseStack.pushPose();
            poseStack.translate(position.x, position.y, position.z);
            BlockModelRenderState blockRenderState = group.blockRenderStates[i];
            blockRenderState.submit(poseStack, submitNodeStorage, group.blockLighting[i], OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Block entities
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
        int blockCount;
        Vector3f[] blockPositions;
        BlockModelRenderState[] blockRenderStates;
        int[] blockLighting;
        int entityCount;
        Vector3f[] entityPositions;
        BlockEntityRenderState[] entityRenderStates;
    }
}
