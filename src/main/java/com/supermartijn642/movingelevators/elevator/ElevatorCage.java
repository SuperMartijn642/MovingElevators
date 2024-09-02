package com.supermartijn642.movingelevators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 8/6/2021 by SuperMartijn642
 */
public class ElevatorCage {

    public static ElevatorCage createCageAndClear(Level level, BlockPos startPos, int xSize, int ySize, int zSize){
        if(!canCreateCage(level, startPos, xSize, ySize, zSize))
            return null;

        BlockState[][][] states = new BlockState[xSize][ySize][zSize];
        CompoundTag[][][] entities = new CompoundTag[xSize][ySize][zSize];
        CompoundTag[][][] entityItemStacks = new CompoundTag[xSize][ySize][zSize];
        VoxelShape shape = Shapes.empty();

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.offset(x, y, z);
                    if(canBlockBeIgnored(level, pos))
                        continue;
                    states[x][y][z] = level.getBlockState(pos);
                    VoxelShape blockShape = states[x][y][z].getCollisionShape(level, pos);
                    blockShape = blockShape.move(x, y, z);
                    shape = Shapes.joinUnoptimized(shape, blockShape, BooleanOp.OR);
                    BlockEntity entity = level.getBlockEntity(pos);
                    if(entity != null){
                        CompoundTag tag = entity.saveWithFullMetadata();
                        tag.putInt("x", x);
                        tag.putInt("y", y);
                        tag.putInt("z", z);
                        entities[x][y][z] = tag;
                        // Create an item to drop in case the block can't be placed back
                        ItemStack stack = new ItemStack(states[x][y][z].getBlock());
                        tag = tag.copy();
                        tag.remove("x");
                        tag.remove("y");
                        tag.remove("z");
                        stack.addTagElement("BlockEntityTag", tag);
                        CompoundTag displayTag = new CompoundTag();
                        ListTag loreTag = new ListTag();
                        loreTag.add(StringTag.valueOf("\"(+NBT)\""));
                        displayTag.put("Lore", loreTag);
                        stack.addTagElement("display", displayTag);
                        entityItemStacks[x][y][z] = stack.save(new CompoundTag());
                    }
                }
            }
        }

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.offset(x, y, z);
                    if(states[x][y][z] == null)
                        continue;
                    BlockEntity entity = level.getBlockEntity(pos);
                    if(entity != null){
                        Clearable.tryClear(entity);
                        level.removeBlockEntity(pos);
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4 | 16);
                }
            }
        }

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.offset(x, y, z);
                    if(states[x][y][z] == null)
                        continue;
                    level.markAndNotifyBlock(pos, level.getChunkAt(pos), states[x][y][z], level.getBlockState(pos), 1 | 2, 512);
                }
            }
        }

        shape.optimize();

        return level.isClientSide ?
            new ClientElevatorCage(xSize, ySize, zSize, states, entities, entityItemStacks, shape.toAabbs()) :
            new ElevatorCage(xSize, ySize, zSize, states, entities, entityItemStacks, shape.toAabbs());
    }

    public static boolean canCreateCage(Level level, BlockPos startPos, int xSize, int ySize, int zSize){
        boolean hasBlocks = false;
        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    if(canBlockBeIgnored(level, startPos.offset(x, y, z)))
                        continue;
                    if(!canBlockBeInCage(level, startPos.offset(x, y, z)))
                        return false;
                    hasBlocks = true;
                }
            }
        }
        return hasBlocks;
    }

    public static boolean canBlockBeIgnored(Level level, BlockPos pos){
        return level.isEmptyBlock(pos) || level.getBlockState(pos).is(Blocks.LIGHT);
    }

    public static boolean canBlockBeInCage(Level level, BlockPos pos){
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && state.getDestroySpeed(level, pos) >= 0;
    }

    public final int xSize, ySize, zSize;
    public final BlockState[][][] blockStates;
    public final CompoundTag[][][] blockEntityData;
    public final CompoundTag[][][] blockEntityStacks;
    public final VoxelShape shape;
    public final List<AABB> collisionBoxes;
    public final AABB bounds;

    public ElevatorCage(int xSize, int ySize, int zSize, BlockState[][][] states, CompoundTag[][][] blockEntityData, CompoundTag[][][] blockEntityStacks, List<AABB> collisionBoxes){
        this.blockEntityData = blockEntityData;
        this.blockEntityStacks = blockEntityStacks;
        if(states.length != xSize || states[0].length != ySize || states[0][0].length != zSize)
            throw new IllegalArgumentException("Given size and block state array do not match!");
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.blockStates = states;
        this.collisionBoxes = Collections.unmodifiableList(collisionBoxes);
        VoxelShape shape = Shapes.empty();
        double minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        for(AABB box : collisionBoxes){
            shape = Shapes.joinUnoptimized(shape, Shapes.create(box), BooleanOp.OR);
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        this.shape = shape.optimize();
        this.bounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void place(Level level, BlockPos startPos){
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    BlockState state = this.blockStates[x][y][z];
                    if(state == null)
                        continue;
                    BlockPos pos = startPos.offset(x, y, z);
                    if(canBlockBeIgnored(level, pos) || level.getBlockState(pos).getDestroySpeed(level, pos) >= 0){
                        if(!level.isEmptyBlock(pos))
                            level.destroyBlock(pos, true);
                        level.setBlock(pos, state, 2);
                        if(this.blockEntityData[x][y][z] != null){
                            BlockEntity entity = BlockEntity.loadStatic(pos, state, this.blockEntityData[x][y][z]);
                            if(entity != null)
                                level.setBlockEntity(entity);
                        }
                    }else{
                        CompoundTag itemTag = this.blockEntityStacks[x][y][z];
                        ItemStack stack = itemTag == null ? new ItemStack(state.getBlock()) : ItemStack.of(itemTag);
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    }
                }
            }
        }
        // Now update all block on the edges of the cage
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    BlockPos pos = startPos.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    boolean[] updateDirections = new boolean[6];
                    if(x == 0)
                        updateDirections[4] = true;
                    if(x == this.xSize - 1)
                        updateDirections[5] = true;
                    if(y == 0)
                        updateDirections[0] = true;
                    if(y == this.ySize - 1)
                        updateDirections[1] = true;
                    if(z == 0)
                        updateDirections[2] = true;
                    if(z == this.zSize - 1)
                        updateDirections[3] = true;
                    for(int i = 0; i < updateDirections.length; i++){
                        if(updateDirections[i]){
                            Direction direction = Direction.values()[i];
                            BlockPos neighbor = pos.relative(direction);
                            BlockState updatedState = state.updateShape(direction, level.getBlockState(neighbor), level, pos, neighbor);
                            Block.updateOrDestroy(state, updatedState, level, pos, 1 | 2, 512);
                            level.neighborChanged(pos.relative(direction), updatedState.getBlock(), pos);
                        }
                    }

                    // Special case for buttons and pressure plates to prevent them getting stuck
                    if(!level.isClientSide
                        && state.getBlock() instanceof ButtonBlock
                        && state.hasProperty(ButtonBlock.POWERED)
                        && state.getValue(ButtonBlock.POWERED))
                        state.tick((ServerLevel)level, pos, level.random);
                    if(!level.isClientSide
                        && state.getBlock() instanceof PressurePlateBlock
                        && state.hasProperty(PressurePlateBlock.POWERED)
                        && state.getValue(PressurePlateBlock.POWERED))
                        state.tick((ServerLevel)level, pos, level.random);
                }
            }
        }
    }

    public List<ItemStack> getDrops(){
        List<ItemStack> drops = new ArrayList<>();
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    if(this.blockEntityStacks[x][y][z] != null)
                        drops.add(ItemStack.of(this.blockEntityStacks[x][y][z]));
                    else
                        drops.add(new ItemStack(this.blockStates[x][y][z].getBlock()));
                }
            }
        }
        return drops;
    }

    public CompoundTag write(){
        CompoundTag compound = new CompoundTag();
        compound.putInt("xSize", this.xSize);
        compound.putInt("ySize", this.ySize);
        compound.putInt("zSize", this.zSize);
        int[] stateIds = new int[this.xSize * this.ySize * this.zSize];
        ListTag entityData = new ListTag();
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    int index = x * this.ySize * this.zSize + y * this.zSize + z;
                    BlockState state = this.blockStates[x][y][z];
                    stateIds[index] = Block.getId(state == null || state.getBlock() == Blocks.AIR ? Blocks.AIR.defaultBlockState() : state);
                    if(this.blockEntityData[x][y][z] != null){
                        CompoundTag tag = new CompoundTag();
                        tag.putInt("x", x);
                        tag.putInt("y", y);
                        tag.putInt("z", z);
                        tag.put("data", this.blockEntityData[x][y][z]);
                        tag.put("stack", this.blockEntityStacks[x][y][z]);
                        entityData.add(tag);
                    }
                }
            }
        }
        compound.putIntArray("blockStates", stateIds);
        compound.put("entityData", entityData);
        ListTag collisionBoxList = new ListTag();
        this.collisionBoxes.forEach(box -> collisionBoxList.add(writeBox(box)));
        compound.put("collisionBoxes", collisionBoxList);
        return compound;
    }

    public static ElevatorCage read(CompoundTag compound, boolean isClientSide){
        int xSize = compound.getInt("xSize");
        int ySize = compound.getInt("ySize");
        int zSize = compound.getInt("zSize");
        int[] stateIds = compound.getIntArray("blockStates");
        BlockState[][][] blockStates = new BlockState[xSize][ySize][zSize];
        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    int index = x * ySize * zSize + y * zSize + z;
                    BlockState state = Block.stateById(stateIds[index]);
                    blockStates[x][y][z] = state.getBlock() == Blocks.AIR ? null : state;
                }
            }
        }
        CompoundTag[][][] entityTags = new CompoundTag[xSize][ySize][zSize];
        CompoundTag[][][] stackTags = new CompoundTag[xSize][ySize][zSize];
        if(compound.contains("entityData", Tag.TAG_LIST)){
            ListTag entityData = compound.getList("entityData", Tag.TAG_COMPOUND);
            for(Tag tag : entityData){
                int x = ((CompoundTag)tag).getInt("x");
                int y = ((CompoundTag)tag).getInt("y");
                int z = ((CompoundTag)tag).getInt("z");
                entityTags[x][y][z] = ((CompoundTag)tag).getCompound("data");
                stackTags[x][y][z] = ((CompoundTag)tag).getCompound("stack");
            }
        }
        ListTag collisionBoxList = compound.getList("collisionBoxes", 10);
        List<AABB> collisionBoxes = collisionBoxList.stream()
            .map(CompoundTag.class::cast)
            .map(ElevatorCage::readBox)
            .collect(Collectors.toList());
        return isClientSide ?
            new ClientElevatorCage(xSize, ySize, zSize, blockStates, entityTags, stackTags, collisionBoxes) :
            new ElevatorCage(xSize, ySize, zSize, blockStates, entityTags, stackTags, collisionBoxes);
    }

    private static CompoundTag writeBox(AABB box){
        CompoundTag compound = new CompoundTag();
        compound.putDouble("x1", box.minX);
        compound.putDouble("y1", box.minY);
        compound.putDouble("z1", box.minZ);
        compound.putDouble("x2", box.maxX);
        compound.putDouble("y2", box.maxY);
        compound.putDouble("z2", box.maxZ);
        return compound;
    }

    private static AABB readBox(CompoundTag compound){
        return new AABB(
            compound.getDouble("x1"),
            compound.getDouble("y1"),
            compound.getDouble("z1"),
            compound.getDouble("x2"),
            compound.getDouble("y2"),
            compound.getDouble("z2")
        );
    }
}
