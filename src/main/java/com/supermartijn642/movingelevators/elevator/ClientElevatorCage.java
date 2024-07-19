package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Created 23/04/2023 by SuperMartijn642
 */
public class ClientElevatorCage extends ElevatorCage {

    private static ElevatorCabinLevel level;

    public static Level getFakeLevel(){
        return level;
    }

    private BlockPos renderPos = null;
    public final BlockEntity[][][] blockEntities;

    public ClientElevatorCage(int xSize, int ySize, int zSize, BlockState[][][] states, CompoundTag[][][] blockEntityData, Tag[][][] blockEntityStacks, List<AABB> collisionBoxes){
        super(xSize, ySize, zSize, states, blockEntityData, blockEntityStacks, collisionBoxes);
        this.blockEntities = new BlockEntity[xSize][ySize][zSize];
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

                    CompoundTag entityData = this.blockEntityData[x][y][z];
                    String identifier = entityData.getString("id");
                    BlockEntityType<?> entityType = Registries.BLOCK_ENTITY_TYPES.getValue(new ResourceLocation(identifier));
                    if(entityType != null){
                        BlockState state = this.blockStates[x][y][z];
                        BlockPos pos = new BlockPos(renderPos.getX() + x, renderPos.getY() + y, renderPos.getZ() + z);
                        BlockEntity entity = entityType.create(pos, state);
                        if(entity != null){
                            entity.loadWithComponents(entityData, group.level.registryAccess());
                            entity.setLevel(level);
                            this.blockEntities[x][y][z] = entity;
                        }
                    }
                }
            }
        }
    }
}
