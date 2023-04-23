package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 23/04/2023 by SuperMartijn642
 */
public class ClientElevatorCage extends ElevatorCage {

    private static ElevatorCabinLevel level;

    public static World getFakeLevel(){
        return level;
    }

    private BlockPos renderPos = null;
    public final TileEntity[][][] blockEntities;

    public ClientElevatorCage(int xSize, int ySize, int zSize, IBlockState[][][] states, NBTTagCompound[][][] blockEntityData, NBTTagCompound[][][] blockEntityStacks, List<AxisAlignedBB> collisionBoxes){
        super(xSize, ySize, zSize, states, blockEntityData, blockEntityStacks, collisionBoxes);
        this.blockEntities = new TileEntity[xSize][ySize][zSize];
    }

    public void loadRenderInfo(BlockPos renderPos, ElevatorGroup group){
        if(level == null)
            level = new ElevatorCabinLevel(ClientUtils.getWorld());
        level.setCabinAndPos(ClientUtils.getWorld(), this, group, renderPos);
        if(renderPos.equals(this.renderPos))
            return;

        this.renderPos = renderPos;
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    this.blockEntities[x][y][z] = null;
                    if(this.blockStates[x][y][z] == null || this.blockEntityData[x][y][z] == null)
                        continue;

                    NBTTagCompound entityData = this.blockEntityData[x][y][z];
                    TileEntity entity = TileEntity.create(level, entityData);
                    if(entity != null){
                        BlockPos pos = new BlockPos(renderPos.getX() + x, renderPos.getY() + y, renderPos.getZ() + z);
                        entity.setPos(pos);
                        entity.setWorld(level);
                        this.blockEntities[x][y][z] = entity;
                    }
                }
            }
        }
    }
}
