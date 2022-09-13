package com.supermartijn642.movingelevators.elevator;

import net.minecraft.block.*;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 8/6/2021 by SuperMartijn642
 */
public class ElevatorCage {

    public static ElevatorCage createCageAndClear(World world, BlockPos startPos, int xSize, int ySize, int zSize){
        if(!canCreateCage(world, startPos, xSize, ySize, zSize))
            return null;

        BlockState[][][] states = new BlockState[xSize][ySize][zSize];
        VoxelShape shape = VoxelShapes.empty();

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.offset(x, y, z);
                    if(world.isEmptyBlock(pos))
                        continue;
                    states[x][y][z] = world.getBlockState(pos);
                    VoxelShape blockShape = states[x][y][z].getCollisionShape(world, pos);
                    blockShape = blockShape.move(x, y, z);
                    shape = VoxelShapes.joinUnoptimized(shape, blockShape, IBooleanFunction.OR);
                }
            }
        }

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.offset(x, y, z);
                    if(states[x][y][z] == null)
                        continue;
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 4 | 16);
                }
            }
        }

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.offset(x, y, z);
                    if(states[x][y][z] == null)
                        continue;
                    world.markAndNotifyBlock(pos, world.getChunkAt(pos), states[x][y][z], world.getBlockState(pos), 1 | 2);
                }
            }
        }

        shape.optimize();

        return new ElevatorCage(xSize, ySize, zSize, states, shape.toAabbs());
    }

    public static boolean canCreateCage(World level, BlockPos startPos, int xSize, int ySize, int zSize){
        boolean hasBlocks = false;
        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    if(level.isEmptyBlock(startPos.offset(x, y, z)))
                        continue;
                    if(!canBlockBeInCage(level, startPos.offset(x, y, z)))
                        return false;
                    hasBlocks = true;
                }
            }
        }
        return hasBlocks;
    }

    public static boolean canBlockBeInCage(World level, BlockPos pos){
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && state.getDestroySpeed(level, pos) >= 0 && !state.hasTileEntity(); // TODO allow block entities
    }

    public final int xSize, ySize, zSize;
    public final BlockState[][][] blockStates;
    public final VoxelShape shape;
    public final List<AxisAlignedBB> collisionBoxes;
    public final AxisAlignedBB bounds;

    public ElevatorCage(int xSize, int ySize, int zSize, BlockState[][][] states, List<AxisAlignedBB> collisionBoxes){
        if(states.length != xSize || states[0].length != ySize || states[0][0].length != zSize)
            throw new IllegalArgumentException("Given size and block state array do not match!");
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.blockStates = states;
        this.collisionBoxes = Collections.unmodifiableList(collisionBoxes);
        VoxelShape shape = VoxelShapes.empty();
        double minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        for(AxisAlignedBB box : collisionBoxes){
            shape = VoxelShapes.joinUnoptimized(shape, VoxelShapes.create(box), IBooleanFunction.OR);
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        this.shape = shape.optimize();
        this.bounds = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void place(World level, BlockPos startPos){
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    BlockState state = this.blockStates[x][y][z];
                    if(state == null)
                        continue;
                    BlockPos pos = startPos.offset(x, y, z);
                    if(level.isEmptyBlock(pos))
                        level.setBlockAndUpdate(pos, state);
                    else if(level.getBlockState(pos).getDestroySpeed(level, pos) >= 0){
                        level.destroyBlock(pos, true);
                        level.setBlockAndUpdate(pos, state);
                    }else{
                        // TODO account for tile entities vvv
                        InventoryHelper.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(state.getBlock()));
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
                    if(x == 0){
                        Direction direction = Direction.WEST;
                        BlockPos neighbor = pos.relative(direction);
                        BlockState updatedState = state.updateShape(direction, level.getBlockState(neighbor), level, pos, neighbor);
                        Block.updateOrDestroy(state, updatedState, level, pos, 1 | 2);
                    }
                    if(x == this.xSize - 1){
                        Direction direction = Direction.EAST;
                        BlockPos neighbor = pos.relative(direction);
                        BlockState updatedState = state.updateShape(direction, level.getBlockState(neighbor), level, pos, neighbor);
                        Block.updateOrDestroy(state, updatedState, level, pos, 1 | 2);
                    }
                    if(y == 0){
                        Direction direction = Direction.DOWN;
                        BlockPos neighbor = pos.relative(direction);
                        BlockState updatedState = state.updateShape(direction, level.getBlockState(neighbor), level, pos, neighbor);
                        Block.updateOrDestroy(state, updatedState, level, pos, 1 | 2);
                    }
                    if(y == this.ySize - 1){
                        Direction direction = Direction.UP;
                        BlockPos neighbor = pos.relative(direction);
                        BlockState updatedState = state.updateShape(direction, level.getBlockState(neighbor), level, pos, neighbor);
                        Block.updateOrDestroy(state, updatedState, level, pos, 1 | 2);
                    }
                    if(z == 0){
                        Direction direction = Direction.NORTH;
                        BlockPos neighbor = pos.relative(direction);
                        BlockState updatedState = state.updateShape(direction, level.getBlockState(neighbor), level, pos, neighbor);
                        Block.updateOrDestroy(state, updatedState, level, pos, 1 | 2);
                    }
                    if(z == this.zSize - 1){
                        Direction direction = Direction.SOUTH;
                        BlockPos neighbor = pos.relative(direction);
                        BlockState updatedState = state.updateShape(direction, level.getBlockState(neighbor), level, pos, neighbor);
                        Block.updateOrDestroy(state, updatedState, level, pos, 1 | 2);
                    }

                    // Special case for buttons and pressure plates to prevent them getting stuck
                    if(!level.isClientSide
                        && state.getBlock() instanceof AbstractButtonBlock
                        && state.hasProperty(AbstractButtonBlock.POWERED)
                        && state.getValue(AbstractButtonBlock.POWERED))
                        state.tick((ServerWorld)level, pos, level.random);
                    if(!level.isClientSide
                        && state.getBlock() instanceof PressurePlateBlock
                        && state.hasProperty(PressurePlateBlock.POWERED)
                        && state.getValue(PressurePlateBlock.POWERED))
                        state.tick((ServerWorld)level, pos, level.random);
                }
            }
        }
    }

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("xSize", this.xSize);
        compound.putInt("ySize", this.ySize);
        compound.putInt("zSize", this.zSize);
        int[] stateIds = new int[this.xSize * this.ySize * this.zSize];
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    int index = x * this.ySize * this.zSize + y * this.zSize + z;
                    stateIds[index] = Block.getId(this.blockStates[x][y][z]);
                }
            }
        }
        compound.putIntArray("blockStates", stateIds);
        ListNBT collisionBoxList = new ListNBT();
        this.collisionBoxes.forEach(box -> collisionBoxList.add(writeBox(box)));
        compound.put("collisionBoxes", collisionBoxList);
        return compound;
    }

    public static ElevatorCage read(CompoundNBT compound){
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
        ListNBT collisionBoxList = compound.getList("collisionBoxes", 10);
        List<AxisAlignedBB> collisionBoxes = collisionBoxList.stream()
            .map(CompoundNBT.class::cast)
            .map(ElevatorCage::readBox)
            .collect(Collectors.toList());
        return new ElevatorCage(xSize, ySize, zSize, blockStates, collisionBoxes);
    }

    private static CompoundNBT writeBox(AxisAlignedBB box){
        CompoundNBT compound = new CompoundNBT();
        compound.putDouble("x1", box.minX);
        compound.putDouble("y1", box.minY);
        compound.putDouble("z1", box.minZ);
        compound.putDouble("x2", box.maxX);
        compound.putDouble("y2", box.maxY);
        compound.putDouble("z2", box.maxZ);
        return compound;
    }

    private static AxisAlignedBB readBox(CompoundNBT compound){
        return new AxisAlignedBB(
            compound.getDouble("x1"),
            compound.getDouble("y1"),
            compound.getDouble("z1"),
            compound.getDouble("x2"),
            compound.getDouble("y2"),
            compound.getDouble("z2")
        );
    }
}
