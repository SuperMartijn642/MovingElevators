package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
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

    public ClientElevatorCage(int xSize, int ySize, int zSize, BlockState[][][] states, CompoundNBT[][][] blockEntityData, CompoundNBT[][][] blockEntityStacks, List<AxisAlignedBB> collisionBoxes){
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

                    CompoundNBT entityData = this.blockEntityData[x][y][z];
                    String identifier = entityData.getString("id");
                    TileEntityType<?> entityType = Registries.BLOCK_ENTITY_TYPES.getValue(new ResourceLocation(identifier));
                    if(entityType != null){
                        BlockState state = this.blockStates[x][y][z];
                        BlockPos pos = new BlockPos(renderPos.getX() + x, renderPos.getY() + y, renderPos.getZ() + z);
                        TileEntity entity = entityType.create();
                        if(entity != null){
                            entity.load(state, entityData);
                            entity.setLevelAndPosition(level, pos);
                            this.blockEntities[x][y][z] = entity;
                        }
                    }
                }
            }
        }
    }
}
