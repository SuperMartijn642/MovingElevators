package com.supermartijn642.movingelevators.elevator;

import net.minecraft.block.*;
import net.minecraft.inventory.IClearable;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

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

        BlockState[][][] states = new BlockState[xSize][ySize][zSize];
        CompoundNBT[][][] entities = new CompoundNBT[xSize][ySize][zSize];
        CompoundNBT[][][] entityItemStacks = new CompoundNBT[xSize][ySize][zSize];
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
                    TileEntity entity = world.getBlockEntity(pos);
                    if(entity != null){
                        CompoundNBT tag = entity.save(new CompoundNBT());
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
                        CompoundNBT displayTag = new CompoundNBT();
                        ListNBT loreTag = new ListNBT();
                        loreTag.add(StringNBT.valueOf("\"(+NBT)\""));
                        displayTag.put("Lore", loreTag);
                        stack.addTagElement("display", displayTag);
                        entityItemStacks[x][y][z] = stack.serializeNBT();
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
                    TileEntity entity = world.getBlockEntity(pos);
                    if(entity != null){
                        IClearable.tryClear(entity);
                        world.removeBlockEntity(pos);
                    }
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
                    world.markAndNotifyBlock(pos, world.getChunkAt(pos), states[x][y][z], world.getBlockState(pos), 1 | 2, 512);
                }
            }
        }

        shape.optimize();

        return world.isClientSide ?
            new ClientElevatorCage(xSize, ySize, zSize, states, entities, entityItemStacks, shape.toAabbs()) :
            new ElevatorCage(xSize, ySize, zSize, states, entities, entityItemStacks, shape.toAabbs());
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
        return state.getFluidState().isEmpty() && state.getDestroySpeed(level, pos) >= 0;
    }

    public final int xSize, ySize, zSize;
    public final BlockState[][][] blockStates;
    public final CompoundNBT[][][] blockEntityData;
    public final CompoundNBT[][][] blockEntityStacks;
    public final VoxelShape shape;
    public final List<AxisAlignedBB> collisionBoxes;
    public final AxisAlignedBB bounds;

    public ElevatorCage(int xSize, int ySize, int zSize, BlockState[][][] states, CompoundNBT[][][] blockEntityData, CompoundNBT[][][] blockEntityStacks, List<AxisAlignedBB> collisionBoxes){
        this.blockEntityData = blockEntityData;
        this.blockEntityStacks = blockEntityStacks;
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
                    boolean isEmpty = level.isEmptyBlock(pos);
                    if(isEmpty || level.getBlockState(pos).getDestroySpeed(level, pos) >= 0){
                        if(!isEmpty)
                            level.destroyBlock(pos, true);
                        level.setBlock(pos, state, 2);
                        if(this.blockEntityData[x][y][z] != null){
                            TileEntity entity = TileEntity.loadStatic(state, this.blockEntityData[x][y][z]);
                            if(entity != null)
                                level.setBlockEntity(pos, entity);
                        }
                    }else{
                        CompoundNBT itemTag = this.blockEntityStacks[x][y][z];
                        ItemStack stack = itemTag == null ? new ItemStack(state.getBlock()) : ItemStack.of(itemTag);
                        InventoryHelper.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
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

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("xSize", this.xSize);
        compound.putInt("ySize", this.ySize);
        compound.putInt("zSize", this.zSize);
        int[] stateIds = new int[this.xSize * this.ySize * this.zSize];
        ListNBT entityData = new ListNBT();
        for(int x = 0; x < this.xSize; x++){
            for(int y = 0; y < this.ySize; y++){
                for(int z = 0; z < this.zSize; z++){
                    int index = x * this.ySize * this.zSize + y * this.zSize + z;
                    stateIds[index] = Block.getId(this.blockStates[x][y][z]);
                    if(this.blockEntityData[x][y][z] != null){
                        CompoundNBT tag = new CompoundNBT();
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
        ListNBT collisionBoxList = new ListNBT();
        this.collisionBoxes.forEach(box -> collisionBoxList.add(writeBox(box)));
        compound.put("collisionBoxes", collisionBoxList);
        return compound;
    }

    public static ElevatorCage read(CompoundNBT compound, boolean isClientSide){
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
        CompoundNBT[][][] entityTags = new CompoundNBT[xSize][ySize][zSize];
        CompoundNBT[][][] stackTags = new CompoundNBT[xSize][ySize][zSize];
        if(compound.contains("entityData", Constants.NBT.TAG_LIST)){
            ListNBT entityData = compound.getList("entityData", Constants.NBT.TAG_COMPOUND);
            for(INBT tag : entityData){
                int x = ((CompoundNBT)tag).getInt("x");
                int y = ((CompoundNBT)tag).getInt("y");
                int z = ((CompoundNBT)tag).getInt("z");
                entityTags[x][y][z] = ((CompoundNBT)tag).getCompound("data");
                stackTags[x][y][z] = ((CompoundNBT)tag).getCompound("stack");
            }
        }
        ListNBT collisionBoxList = compound.getList("collisionBoxes", 10);
        List<AxisAlignedBB> collisionBoxes = collisionBoxList.stream()
            .map(CompoundNBT.class::cast)
            .map(ElevatorCage::readBox)
            .collect(Collectors.toList());
        return isClientSide ?
            new ClientElevatorCage(xSize, ySize, zSize, blockStates, entityTags, stackTags, collisionBoxes) :
            new ElevatorCage(xSize, ySize, zSize, blockStates, entityTags, stackTags, collisionBoxes);
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
