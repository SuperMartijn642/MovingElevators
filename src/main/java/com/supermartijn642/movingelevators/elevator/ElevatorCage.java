package com.supermartijn642.movingelevators.elevator;

import com.google.common.collect.Streams;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

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

        IBlockState[][][] states = new IBlockState[xSize][ySize][zSize];
        BlockShape shape = BlockShape.empty();

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.add(x, y, z);
                    if(world.isAirBlock(pos))
                        continue;
                    states[x][y][z] = world.getBlockState(pos);
                    AxisAlignedBB boundingBox = states[x][y][z].getCollisionBoundingBox(world, pos);
                    BlockShape blockShape = boundingBox == null ? BlockShape.empty() : BlockShape.create(boundingBox);
                    blockShape = blockShape.offset(x, y, z);
                    shape = BlockShape.or(shape, blockShape);
                }
            }
        }

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.add(x, y, z);
                    if(states[x][y][z] == null)
                        continue;
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4 | 16);
                }
            }
        }

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.add(x, y, z);
                    if(states[x][y][z] == null)
                        continue;
                    world.markAndNotifyBlock(pos, world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4), states[x][y][z], world.getBlockState(pos), 1 | 2);
                }
            }
        }

        // TODO reduce the number of bounding boxes
//        shape.optimize();

        return new ElevatorCage(xSize, ySize, zSize, states, shape.toBoxes());
    }

    public static boolean canCreateCage(World world, BlockPos startPos, int xSize, int ySize, int zSize){
        boolean hasBlocks = false;
        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    if(world.isAirBlock(startPos.add(x, y, z)))
                        continue;
                    if(!canBlockBeInCage(world, startPos.add(x, y, z)))
                        return false;
                    hasBlocks = true;
                }
            }
        }
        return hasBlocks;
    }

    public static boolean canBlockBeInCage(World world, BlockPos pos){
        IBlockState state = world.getBlockState(pos);
        return !(state.getBlock() instanceof IFluidBlock) && state.getBlockHardness(world, pos) >= 0 && !state.getBlock().hasTileEntity(state); // TODO allow block entities
    }

    public final int xSize, ySize, zSize;
    public final IBlockState[][][] blockStates;
    public final BlockShape shape;
    public final List<AxisAlignedBB> collisionBoxes;
    public final AxisAlignedBB bounds;

    public ElevatorCage(int xSize, int ySize, int zSize, IBlockState[][][] states, List<AxisAlignedBB> collisionBoxes){
        if(states.length != xSize || states[0].length != ySize || states[0][0].length != zSize)
            throw new IllegalArgumentException("Given size and block state array do not match!");
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.blockStates = states;
        this.collisionBoxes = Collections.unmodifiableList(collisionBoxes);
        BlockShape shape = BlockShape.empty();
        double minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        for(AxisAlignedBB box : collisionBoxes){
            shape = BlockShape.or(shape, BlockShape.create(box));
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        // TODO reduce the number of bounding boxes
//        this.shape = shape.optimize();
        this.shape = shape;
        this.bounds = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void place(World world, BlockPos startPos){
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    IBlockState state = this.blockStates[x][y][z];
                    if(state == null)
                        continue;
                    BlockPos pos = startPos.add(x, y, z);
                    if(world.isAirBlock(pos))
                        world.setBlockState(pos, state);
                    else if(world.getBlockState(pos).getBlockHardness(world, pos) >= 0){
                        world.destroyBlock(pos, true);
                        world.setBlockState(pos, state);
                    }else{
                        // TODO account for tile entities vvv
                        InventoryHelper.spawnItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(state.getBlock()));
                    }
                }
            }
        }
        // Now update all block on the edges of the cage
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    BlockPos pos = startPos.add(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if(x == 0){
                        EnumFacing direction = EnumFacing.WEST;
                        BlockPos neighbor = pos.offset(direction);
                        state.neighborChanged(world, pos, state.getBlock(), neighbor);
                        world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
                    }
                    if(x == this.xSize - 1){
                        EnumFacing direction = EnumFacing.EAST;
                        BlockPos neighbor = pos.offset(direction);
                        state.neighborChanged(world, pos, state.getBlock(), neighbor);
                        world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
                    }
                    if(y == 0){
                        EnumFacing direction = EnumFacing.DOWN;
                        BlockPos neighbor = pos.offset(direction);
                        state.neighborChanged(world, pos, state.getBlock(), neighbor);
                        world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
                    }
                    if(y == this.ySize - 1){
                        EnumFacing direction = EnumFacing.UP;
                        BlockPos neighbor = pos.offset(direction);
                        state.neighborChanged(world, pos, state.getBlock(), neighbor);
                        world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
                    }
                    if(z == 0){
                        EnumFacing direction = EnumFacing.NORTH;
                        BlockPos neighbor = pos.offset(direction);
                        state.neighborChanged(world, pos, state.getBlock(), neighbor);
                        world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
                    }
                    if(z == this.zSize - 1){
                        EnumFacing direction = EnumFacing.SOUTH;
                        BlockPos neighbor = pos.offset(direction);
                        state.neighborChanged(world, pos, state.getBlock(), neighbor);
                        world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
                    }

                    // Special case for buttons and pressure plates to prevent them getting stuck
                    if(!world.isRemote
                        && state.getBlock() instanceof BlockButton
                        && state.getValue(BlockButton.POWERED))
                        state.getBlock().updateTick(world, pos, state, world.rand);
                    if(!world.isRemote
                        && state.getBlock() instanceof BlockPressurePlate
                        && state.getValue(BlockPressurePlate.POWERED))
                        state.getBlock().updateTick(world, pos, state, world.rand);
                }
            }
        }
    }

    public NBTTagCompound write(){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("xSize", this.xSize);
        compound.setInteger("ySize", this.ySize);
        compound.setInteger("zSize", this.zSize);
        int[] stateIds = new int[this.xSize * this.ySize * this.zSize];
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    int index = x * this.ySize * this.zSize + y * this.zSize + z;
                    IBlockState state = this.blockStates[x][y][z];
                    stateIds[index] = Block.getStateId(state == null ? Blocks.AIR.getDefaultState() : state);
                }
            }
        }
        compound.setIntArray("blockStates", stateIds);
        NBTTagList collisionBoxList = new NBTTagList();
        this.collisionBoxes.forEach(box -> collisionBoxList.appendTag(writeBox(box)));
        compound.setTag("collisionBoxes", collisionBoxList);
        return compound;
    }

    public static ElevatorCage read(NBTTagCompound compound){
        int xSize = compound.getInteger("xSize");
        int ySize = compound.getInteger("ySize");
        int zSize = compound.getInteger("zSize");
        int[] stateIds = compound.getIntArray("blockStates");
        IBlockState[][][] blockStates = new IBlockState[xSize][ySize][zSize];
        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    int index = x * ySize * zSize + y * zSize + z;
                    IBlockState state = Block.getStateById(stateIds[index]);
                    blockStates[x][y][z] = state.getBlock() == Blocks.AIR ? null : state;
                }
            }
        }
        NBTTagList collisionBoxList = compound.getTagList("collisionBoxes", 10);
        List<AxisAlignedBB> collisionBoxes = Streams.stream(collisionBoxList)
            .map(NBTTagCompound.class::cast)
            .map(ElevatorCage::readBox)
            .collect(Collectors.toList());
        return new ElevatorCage(xSize, ySize, zSize, blockStates, collisionBoxes);
    }

    private static NBTTagCompound writeBox(AxisAlignedBB box){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setDouble("x1", box.minX);
        compound.setDouble("y1", box.minY);
        compound.setDouble("z1", box.minZ);
        compound.setDouble("x2", box.maxX);
        compound.setDouble("y2", box.maxY);
        compound.setDouble("z2", box.maxZ);
        return compound;
    }

    private static AxisAlignedBB readBox(NBTTagCompound compound){
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
