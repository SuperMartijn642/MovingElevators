package com.supermartijn642.movingelevators.gui.preview;

import com.google.common.collect.Maps;
import com.supermartijn642.core.ClientUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.Map;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class WorldBlockCapture {

    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    private final ClientLevel level;
    private final Map<BlockPos,BlockPos> capturePosToWorldPos = Maps.newHashMap();

    public WorldBlockCapture(ClientLevel level){
        this.level = level;
    }

    public void putBlock(BlockPos capturePos, BlockPos worldPos){
        this.capturePosToWorldPos.put(capturePos, worldPos);
    }

    public BlockState getBlockState(BlockPos pos){
        BlockPos worldPos = this.capturePosToWorldPos.get(pos);
        return worldPos == null ? Blocks.AIR.defaultBlockState() : this.level.getBlockState(worldPos);
    }

    public BlockEntity getBlockEntity(BlockPos pos){
        BlockPos worldPos = this.capturePosToWorldPos.get(pos);
        return worldPos == null ? null : this.level.getBlockEntity(worldPos);
    }

    public Iterable<BlockPos> getBlockLocations(){
        return this.capturePosToWorldPos.keySet();
    }

    public AABB getBounds(){
        if(this.capturePosToWorldPos.isEmpty())
            return new AABB(0, 0, 0, 0, 0, 0);
        AABB bounds = new AABB(this.capturePosToWorldPos.keySet().stream().findFirst().get());
        for(BlockPos pos : this.capturePosToWorldPos.keySet())
            bounds = bounds.minmax(new AABB(pos));
        return bounds;
    }

    @Deprecated
    public Level getLevel(){
        return this.level;
    }

    public void updateRenderState(RenderState state){
        state.size = this.capturePosToWorldPos.size();
        if(state.capacity < state.size){
            state.positions = new BlockPos[state.size];
            state.blockRenderStates = new BlockModelRenderState[state.size];
            state.blockLighting = new int[state.size];
            state.entityStates = new BlockEntityRenderState[state.size];
            state.capacity = state.size;
        }
        int index = 0;
        for(Map.Entry<BlockPos,BlockPos> entry : this.capturePosToWorldPos.entrySet()){
            state.positions[index] = entry.getKey();
            BlockPos pos = entry.getValue();
            BlockState block = this.level.getBlockState(pos);
            BlockModelRenderState blockRenderState = state.blockRenderStates[index];
            if(blockRenderState == null)
                blockRenderState = state.blockRenderStates[index] = new BlockModelRenderState();
            BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(block);
            QuadEmitter emitter = blockRenderState.setupMesh(IDENTITY_MATRIX, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
            RANDOM_SOURCE.setSeed(block.getSeed(pos));
            model.emitQuads(emitter, this.level, pos, block, RANDOM_SOURCE, _ -> false);
            IntList tintLayers = blockRenderState.tintLayers();
            for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(block))
                tintLayers.add(tintSource.colorInWorld(block, this.level, pos));
            state.blockLighting[index] = LightCoordsUtil.pack(
                this.level.getBrightness(LightLayer.BLOCK, pos),
                this.level.getBrightness(LightLayer.SKY, pos)
            );
            BlockEntityRenderState entityRenderState = null;
            BlockEntity entity = this.level.getBlockEntity(pos);
            if(entity != null){
                BlockEntityRenderer<BlockEntity,BlockEntityRenderState> renderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(entity);
                if(renderer != null){
                    entityRenderState = renderer.createRenderState();
                    renderer.extractRenderState(entity, entityRenderState, ClientUtils.getPartialTicks(), Vec3.ZERO, null);
                }
            }
            state.entityStates[index] = entityRenderState;
            index++;
        }
        state.bounds = this.getBounds();
    }

    public static class RenderState {

        private int size, capacity;
        private BlockPos[] positions;
        private AABB bounds;
        private BlockModelRenderState[] blockRenderStates;
        private int[] blockLighting;
        private BlockEntityRenderState[] entityStates;

        public int size(){
            return this.size;
        }

        public BlockPos position(int index){
            return this.positions[index];
        }

        public AABB bounds(){
            return this.bounds;
        }

        public BlockModelRenderState blockRenderState(int index){
            return this.blockRenderStates[index];
        }

        public int lighting(int index){
            return this.blockLighting[index];
        }

        public BlockEntityRenderState entityRenderState(int index){
            return this.entityStates[index];
        }
    }
}
