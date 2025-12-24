package com.supermartijn642.movingelevators.gui.preview;

import com.google.common.collect.Maps;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataManager;

import java.util.Map;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class WorldBlockCapture {

    private final Level level;
    private final Map<BlockPos,BlockPos> capturePosToWorldPos = Maps.newHashMap();

    public WorldBlockCapture(Level level){
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
            state.states = new BlockState[state.size];
            state.tintValues = new int[state.size];
            state.entityStates = new BlockEntityRenderState[state.size];
            state.modelData = new ModelData[state.size];
            state.capacity = state.size;
        }
        int index = 0;
        for(Map.Entry<BlockPos,BlockPos> entry : this.capturePosToWorldPos.entrySet()){
            state.positions[index] = entry.getKey();
            BlockPos pos = entry.getValue();
            BlockState block = this.level.getBlockState(pos);
            state.states[index] = block;
            state.tintValues[index] = ClientUtils.getMinecraft().getBlockColors().getColor(block, this.level, pos);
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
            //noinspection UnstableApiUsage
            ModelDataManager modelDataManager = this.level.getModelDataManager();
            //noinspection UnstableApiUsage
            ModelData modelData = modelDataManager == null ? ModelData.EMPTY : modelDataManager.getAtOrEmpty(pos);
            state.modelData[index] = ClientUtils.getBlockRenderer().getBlockModel(block).getModelData(this.level, pos, block, modelData);
            index++;
        }
        state.bounds = this.getBounds();
    }

    public static class RenderState {

        private int size, capacity;
        private BlockPos[] positions;
        private AABB bounds;
        private BlockState[] states;
        private int[] tintValues;
        private BlockEntityRenderState[] entityStates;
        private ModelData[] modelData;

        public int size(){
            return this.size;
        }

        public BlockPos position(int index){
            return this.positions[index];
        }

        public AABB bounds(){
            return this.bounds;
        }

        public BlockState state(int index){
            return this.states[index];
        }

        public int tint(int index){
            return this.tintValues[index];
        }

        public BlockEntityRenderState entityRenderState(int index){
            return this.entityStates[index];
        }

        public ModelData modelData(int index){
            return this.modelData[index];
        }
    }
}
