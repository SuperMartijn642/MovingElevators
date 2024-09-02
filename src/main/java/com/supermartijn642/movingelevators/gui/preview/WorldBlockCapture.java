package com.supermartijn642.movingelevators.gui.preview;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Map;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class WorldBlockCapture {

    private final Level level;
    private final Map<BlockPos,BlockPos> blocks = Maps.newHashMap();

    public WorldBlockCapture(Level level){
        this.level = level;
    }

    public void putBlock(BlockPos capturePos, BlockPos worldPos){
        this.blocks.put(capturePos, worldPos);
    }

    public BlockState getBlockState(BlockPos pos){
        BlockPos worldPos = this.blocks.get(pos);
        return worldPos == null ? Blocks.AIR.defaultBlockState() : this.level.getBlockState(worldPos);
    }

    public BlockEntity getBlockEntity(BlockPos pos){
        BlockPos worldPos = this.blocks.get(pos);
        return worldPos == null ? null : this.level.getBlockEntity(worldPos);
    }

    public Iterable<BlockPos> getBlockLocations(){
        return this.blocks.values();
    }

    public AABB getBounds(){
        if(this.blocks.isEmpty())
            return new AABB(0, 0, 0, 0, 0, 0);
        AABB bounds = new AABB(this.blocks.keySet().stream().findFirst().get());
        for(BlockPos pos : this.blocks.keySet())
            bounds = bounds.minmax(new AABB(pos));
        return bounds;
    }

    @Deprecated
    public Level getLevel(){
        return this.level;
    }
}
