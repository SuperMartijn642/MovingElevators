package com.supermartijn642.movingelevators.gui.preview;

import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class WorldBlockCapture {

    private final World world;
    private final Map<BlockPos,BlockPos> blocks = Maps.newHashMap();

    public WorldBlockCapture(World world){
        this.world = world;
    }

    public void putBlock(BlockPos capturePos, BlockPos worldPos){
        this.blocks.put(capturePos, worldPos);
    }

    public IBlockState getBlockState(BlockPos pos){
        BlockPos worldPos = this.blocks.get(pos);
        return worldPos == null ? Blocks.AIR.getDefaultState() : this.world.getBlockState(worldPos);
    }

    public TileEntity getBlockEntity(BlockPos pos){
        BlockPos worldPos = this.blocks.get(pos);
        return worldPos == null ? null : this.world.getTileEntity(worldPos);
    }

    public Iterable<BlockPos> getBlockLocations(){
        return this.blocks.values();
    }

    public AxisAlignedBB getBounds(){
        if(this.blocks.isEmpty())
            return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
        AxisAlignedBB bounds = new AxisAlignedBB(this.blocks.keySet().stream().findFirst().get());
        for(BlockPos pos : this.blocks.keySet())
            bounds = bounds.union(new AxisAlignedBB(pos));
        return bounds;
    }

    @Deprecated
    public World getWorld(){
        return this.world;
    }
}
