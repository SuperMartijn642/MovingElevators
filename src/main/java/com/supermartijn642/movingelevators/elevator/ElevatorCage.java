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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
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
        NBTTagCompound[][][] entities = new NBTTagCompound[xSize][ySize][zSize];
        NBTTagCompound[][][] entityItemStacks = new NBTTagCompound[xSize][ySize][zSize];
        BlockShape shape = BlockShape.empty();

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.add(x, y, z);
                    if(canBlockBeIgnored(world, pos))
                        continue;
                    states[x][y][z] = world.getBlockState(pos);
                    AxisAlignedBB boundingBox = states[x][y][z].getCollisionBoundingBox(world, pos);
                    BlockShape blockShape = boundingBox == null ? BlockShape.empty() : BlockShape.create(boundingBox);
                    blockShape = blockShape.offset(x, y, z);
                    shape = BlockShape.or(shape, blockShape);
                    TileEntity entity = world.getTileEntity(pos);
                    if(entity != null){
                        NBTTagCompound tag = entity.serializeNBT();
                        tag.setInteger("x", x);
                        tag.setInteger("y", y);
                        tag.setInteger("z", z);
                        entities[x][y][z] = tag;
                        // Create an item to drop in case the block can't be placed back
                        ItemStack stack = new ItemStack(states[x][y][z].getBlock());
                        tag = tag.copy();
                        tag.removeTag("x");
                        tag.removeTag("y");
                        tag.removeTag("z");
                        stack.setTagInfo("BlockEntityTag", tag);
                        NBTTagCompound displayTag = new NBTTagCompound();
                        NBTTagList loreTag = new NBTTagList();
                        loreTag.appendTag(new NBTTagString("(+NBT)"));
                        displayTag.setTag("Lore", loreTag);
                        stack.setTagInfo("display", displayTag);
                        entityItemStacks[x][y][z] = stack.serializeNBT();
                    }
                }
            }
        }

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    BlockPos pos = startPos.add(x, y, z);
                    if(states[x][y][z] == null)
                        continue;
                    TileEntity entity = world.getTileEntity(pos);
                    if(entity != null)
                        world.removeTileEntity(pos);
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

        return world.isRemote ?
            new ClientElevatorCage(xSize, ySize, zSize, states, entities, entityItemStacks, shape.toBoxes()) :
            new ElevatorCage(xSize, ySize, zSize, states, entities, entityItemStacks, shape.toBoxes());
    }

    public static boolean canCreateCage(World level, BlockPos startPos, int xSize, int ySize, int zSize){
        boolean hasBlocks = false;
        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                for(int z = 0; z < zSize; z++){
                    if(canBlockBeIgnored(level, startPos.add(x, y, z)))
                        continue;
                    if(!canBlockBeInCage(level, startPos.add(x, y, z)))
                        return false;
                    hasBlocks = true;
                }
            }
        }
        return hasBlocks;
    }

    public static boolean canBlockBeIgnored(World level, BlockPos pos){
        return level.isAirBlock(pos);
    }

    public static boolean canBlockBeInCage(World level, BlockPos pos){
        IBlockState state = level.getBlockState(pos);
        return !(state.getBlock() instanceof IFluidBlock) && state.getBlockHardness(level, pos) >= 0;
    }

    public final int xSize, ySize, zSize;
    public final IBlockState[][][] blockStates;
    public final NBTTagCompound[][][] blockEntityData;
    public final NBTTagCompound[][][] blockEntityStacks;
    public final BlockShape shape;
    public final List<AxisAlignedBB> collisionBoxes;
    public final AxisAlignedBB bounds;

    public ElevatorCage(int xSize, int ySize, int zSize, IBlockState[][][] states, NBTTagCompound[][][] blockEntityData, NBTTagCompound[][][] blockEntityStacks, List<AxisAlignedBB> collisionBoxes){
        this.blockEntityData = blockEntityData;
        this.blockEntityStacks = blockEntityStacks;
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

    public void place(World level, BlockPos startPos){
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    IBlockState state = this.blockStates[x][y][z];
                    if(state == null)
                        continue;
                    BlockPos pos = startPos.add(x, y, z);
                    if(canBlockBeIgnored(level, pos) || level.getBlockState(pos).getBlockHardness(level, pos) >= 0){
                        if(!level.isAirBlock(pos))
                            level.destroyBlock(pos, true);
                        level.setBlockState(pos, state, 2);
                        if(this.blockEntityData[x][y][z] != null){
                            TileEntity entity = TileEntity.create(level, this.blockEntityData[x][y][z]);
                            if(entity != null)
                                level.setTileEntity(pos, entity);
                        }
                    }else{
                        NBTTagCompound itemTag = this.blockEntityStacks[x][y][z];
                        ItemStack stack = itemTag == null ? new ItemStack(state.getBlock()) : new ItemStack(itemTag);
                        InventoryHelper.spawnItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    }
                }
            }
        }
        // Now update all block on the edges of the cage
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    BlockPos pos = startPos.add(x, y, z);
                    IBlockState state = level.getBlockState(pos);
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
                            EnumFacing direction = EnumFacing.values()[i];
                            BlockPos neighbor = pos.offset(direction);
                            state.neighborChanged(level, pos, state.getBlock(), neighbor);
                            level.neighborChanged(pos.offset(direction), state.getBlock(), pos);
                        }
                    }

                    // Special case for buttons and pressure plates to prevent them getting stuck
                    if(!level.isRemote
                        && state.getBlock() instanceof BlockButton
                        && state.getValue(BlockButton.POWERED))
                        state.getBlock().updateTick(level, pos, state, level.rand);
                    if(!level.isRemote
                        && state.getBlock() instanceof BlockPressurePlate
                        && state.getValue(BlockPressurePlate.POWERED))
                        state.getBlock().updateTick(level, pos, state, level.rand);
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
                        drops.add(new ItemStack(this.blockEntityStacks[x][y][z]));
                    else
                        drops.add(new ItemStack(this.blockStates[x][y][z].getBlock()));
                }
            }
        }
        return drops;
    }

    public NBTTagCompound write(){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("xSize", this.xSize);
        compound.setInteger("ySize", this.ySize);
        compound.setInteger("zSize", this.zSize);
        int[] stateIds = new int[this.xSize * this.ySize * this.zSize];
        NBTTagList entityData = new NBTTagList();
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    int index = x * this.ySize * this.zSize + y * this.zSize + z;
                    IBlockState state = this.blockStates[x][y][z];
                    stateIds[index] = Block.getStateId(state == null ? Blocks.AIR.getDefaultState() : state);
                    if(this.blockEntityData[x][y][z] != null){
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setInteger("x", x);
                        tag.setInteger("y", y);
                        tag.setInteger("z", z);
                        tag.setTag("data", this.blockEntityData[x][y][z]);
                        tag.setTag("stack", this.blockEntityStacks[x][y][z]);
                        entityData.appendTag(tag);
                    }
                }
            }
        }
        compound.setIntArray("blockStates", stateIds);
        compound.setTag("entityData", entityData);
        NBTTagList collisionBoxList = new NBTTagList();
        this.collisionBoxes.forEach(box -> collisionBoxList.appendTag(writeBox(box)));
        compound.setTag("collisionBoxes", collisionBoxList);
        return compound;
    }

    public static ElevatorCage read(NBTTagCompound compound, boolean isClientSide){
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
        NBTTagCompound[][][] entityTags = new NBTTagCompound[xSize][ySize][zSize];
        NBTTagCompound[][][] stackTags = new NBTTagCompound[xSize][ySize][zSize];
        if(compound.hasKey("entityData", Constants.NBT.TAG_LIST)){
            NBTTagList entityData = compound.getTagList("entityData", Constants.NBT.TAG_COMPOUND);
            for(NBTBase tag : entityData){
                int x = ((NBTTagCompound)tag).getInteger("x");
                int y = ((NBTTagCompound)tag).getInteger("y");
                int z = ((NBTTagCompound)tag).getInteger("z");
                entityTags[x][y][z] = ((NBTTagCompound)tag).getCompoundTag("data");
                stackTags[x][y][z] = ((NBTTagCompound)tag).getCompoundTag("stack");
            }
        }
        NBTTagList collisionBoxList = compound.getTagList("collisionBoxes", 10);
        List<AxisAlignedBB> collisionBoxes = Streams.stream(collisionBoxList)
            .map(NBTTagCompound.class::cast)
            .map(ElevatorCage::readBox)
            .collect(Collectors.toList());
        return isClientSide ?
            new ClientElevatorCage(xSize, ySize, zSize, blockStates, entityTags, stackTags, collisionBoxes) :
            new ElevatorCage(xSize, ySize, zSize, blockStates, entityTags, stackTags, collisionBoxes);
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
