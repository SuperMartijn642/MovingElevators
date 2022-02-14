package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.MovingElevatorsConfig;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.packets.PacketSyncElevatorMovement;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.*;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class ElevatorGroup {

    private static final int RE_SYNC_INTERVAL = 10;
    private static final double ACCELERATION = 0.05;

    public final World world;
    public final int x, z;
    public final Direction facing;

    private boolean isMoving = false;
    private int targetY;
    private double lastY;
    private double currentY;
    private double syncCurrentY = Integer.MAX_VALUE;
    private double targetSpeed = 0.2;
    private double speed = 0;
    private int cageSideOffset = 0, cageDepthOffset = 0, cageHeightOffset = -1;
    private int cageSizeX = 3, cageSizeY = 4, cageSizeZ = 3;
    private ElevatorCage cage = null;
    /**
     * The y coordinates of the controllers
     */
    private final ArrayList<Integer> floors = new ArrayList<>();
    private final ArrayList<FloorData> floorData = new ArrayList<>();
    private boolean shouldBeSynced = false;
    private Map<Integer,Set<BlockPos>> comparatorListeners = new Int2ObjectArrayMap<>();

    private int syncCounter = 0;

    public ElevatorGroup(World world, int x, int z, Direction facing){
        this.world = world;
        this.x = x;
        this.z = z;
        this.facing = facing;
    }

    public void update(){
        if(!this.world.isClientSide && this.shouldBeSynced){
            this.shouldBeSynced = false;
            this.updateGroup();
        }

        if(this.isMoving){
            if(this.currentY != this.targetY)
                this.lastY = this.currentY;
            if(this.speed < this.targetSpeed)
                this.speed = Math.min(this.targetSpeed, this.speed + ACCELERATION);
            if(this.currentY == this.targetY)
                this.stopElevator();
            else if(Math.abs(this.targetY - this.currentY) < this.speed){
                this.currentY = this.targetY;
                this.moveElevator(this.lastY, this.currentY);
            }else{
                if(this.syncCurrentY != Integer.MAX_VALUE){
                    this.currentY = this.syncCurrentY;
                    this.syncCurrentY = Integer.MAX_VALUE;
                }else
                    this.currentY += Math.signum(this.targetY - this.currentY) * this.speed;
                this.moveElevator(this.lastY, this.currentY);
            }

            if(this.syncCounter >= RE_SYNC_INTERVAL){
                this.syncMovement();
                this.syncCounter = 0;
            }
            this.syncCounter++;
        }
    }

    private void moveElevator(double oldY, double newY){
        ElevatorCollisionHandler.handleEntityCollisions(this.world, this.cage.bounds, this.cage.collisionBoxes, this.getCageAnchorPos(oldY), new Vector3d(this.x, newY - oldY, 0));
    }

    private void stopElevator(){
        this.isMoving = false;

        this.cage.place(this.world, this.getCageAnchorBlockPos(this.targetY));

        this.moveElevator(this.lastY, this.currentY);

        if(!this.world.isClientSide){
            this.world.updateNeighbourForOutputSignal(this.getPos(this.targetY), MovingElevators.elevator_block);
            for(BlockPos pos : this.comparatorListeners.getOrDefault(this.targetY, Collections.emptySet()))
                if(this.world.isLoaded(pos))
                    this.world.updateNeighbourForOutputSignal(pos, this.world.getBlockState(pos).getBlock());
            this.shouldBeSynced = true;
            Vector3d soundPos = this.getCageAnchorPos(this.targetY).add(this.cageSizeX / 2d, this.cageSizeY / 2d, this.cageSizeZ / 2d);
            this.world.playSound(null, soundPos.x, soundPos.y, soundPos.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.4f, 0.5f);
            this.syncCounter = 0;
        }
    }

    private void startElevator(int currentY, int targetY){
        if(this.world == null || this.isMoving)
            return;

        ElevatorCage cage = ElevatorCage.createCageAndClear(this.world, this.getCageAnchorBlockPos(currentY), this.cageSizeX, this.cageSizeY, this.cageSizeZ);
        if(cage == null)
            return;

        this.cage = cage;
        this.isMoving = true;
        this.targetY = targetY;
        this.currentY = currentY;
        this.lastY = this.currentY;
        this.speed = 0;

        if(!this.world.isClientSide){
            this.world.updateNeighbourForOutputSignal(this.getPos(currentY), MovingElevators.elevator_block);
            for(BlockPos pos : this.comparatorListeners.getOrDefault(currentY, Collections.emptySet()))
                if(this.world.isLoaded(pos))
                    this.world.updateNeighbourForOutputSignal(pos, this.world.getBlockState(pos).getBlock());
            this.updateGroup();
        }
    }

    public void onButtonPress(boolean isUp, boolean isDown, int yLevel){
        if(this.isMoving || !this.floors.contains(yLevel))
            return;

        ControllerBlockEntity tile = this.getTile(yLevel);
        if(tile == null)
            return;

        if(isUp){
            if(this.isCageAvailableAt(tile)){
                for(int floor = this.floors.indexOf(yLevel) + 1; floor < this.floors.size(); floor++){
                    ControllerBlockEntity tile2 = this.getTile(this.floors.get(floor));
                    if(tile2 != null){
                        if(this.canCageBePlacedAt(tile2))
                            this.startElevator(yLevel, this.floors.get(floor));
                        return;
                    }
                }
            }
        }else if(isDown){
            if(this.isCageAvailableAt(tile)){
                for(int floor = this.floors.indexOf(yLevel) - 1; floor >= 0; floor--){
                    ControllerBlockEntity tile2 = this.getTile(this.floors.get(floor));
                    if(tile2 != null){
                        if(this.canCageBePlacedAt(tile2))
                            this.startElevator(yLevel, this.floors.get(floor));
                        return;
                    }
                }
            }
        }else{
            if(this.canCageBePlacedAt(tile)){
                this.floors.sort(Comparator.comparingInt(a -> Math.abs(a - yLevel)));
                for(int y : this.floors){
                    if(y != yLevel){
                        ControllerBlockEntity tile2 = this.getTile(y);
                        if(tile2 != null && this.isCageAvailableAt(tile2)){
                            this.floors.sort(Integer::compare);
                            this.startElevator(y, yLevel);
                            return;
                        }
                    }
                }
                this.floors.sort(Integer::compare);
            }
        }
    }

    public void onDisplayPress(int yLevel, int floorOffset){
        if(this.isMoving || !this.floors.contains(yLevel))
            return;

        int floor = this.floors.indexOf(yLevel);
        if(floorOffset == 0){
            this.onButtonPress(false, false, yLevel);
            return;
        }

        int toFloor = floor + floorOffset;
        if(toFloor < 0 || toFloor >= this.floors.size())
            return;

        ControllerBlockEntity tile = this.getTile(yLevel);
        int toY = this.floors.get(toFloor);
        ControllerBlockEntity toTile = this.getTile(toY);
        if(tile != null && toTile != null && this.isCageAvailableAt(tile) && this.canCageBePlacedAt(toTile))
            this.startElevator(yLevel, toY);
    }

    public void remove(ControllerBlockEntity tile){
        int floor = this.getFloorNumber(tile.getBlockPos().getY());
        this.floors.remove(floor);
        this.floorData.remove(floor);
        if(this.floors.isEmpty()){
            if(this.isMoving){
                Vector3d spawnPos = this.getCageAnchorPos(this.targetY).add(this.cageSizeX / 2d, this.cageSizeY / 2d, this.cageSizeZ / 2d);
                for(BlockState[][] arr : this.cage.blockStates){
                    for(BlockState[] arr2 : arr){
                        for(BlockState state : arr2){
                            ItemEntity entity = new ItemEntity(this.world, spawnPos.x, spawnPos.y, spawnPos.z, new ItemStack(state.getBlock()));
                            this.world.addFreshEntity(entity);
                        }
                    }
                }
            }
        }else
            this.shouldBeSynced = true;
    }

    public void add(ControllerBlockEntity tile){
        if(tile == null)
            return;
        int y = tile.getBlockPos().getY();
        if(this.floors.contains(y))
            return;
        FloorData floorData = new FloorData(tile.getFloorName(), tile.getDisplayLabelColor());
        for(int i = 0; i < this.floors.size(); i++){
            if(y < this.floors.get(i)){
                this.floors.add(i, y);
                this.floorData.add(i, floorData);
                break;
            }
        }
        if(!this.floors.contains(y)){
            this.floors.add(y);
            this.floorData.add(floorData);
        }
        this.shouldBeSynced = true;
    }

    public void updateFloorData(ControllerBlockEntity tile, String name, DyeColor color){
        int floor = this.getFloorNumber(tile.getBlockPos().getY());
        if(floor == -1)
            return;
        FloorData data = this.floorData.get(floor);
        if(!Objects.equals(name, data.name) || color != data.color){
            data.name = name;
            data.color = color;
            this.shouldBeSynced = true;
        }
    }

    public boolean isMoving(){
        return this.isMoving;
    }

    public double getLastY(){
        return this.lastY;
    }

    public double getCurrentY(){
        return this.currentY;
    }

    public void updateCurrentY(double y, double speed){
        if(this.isMoving && (this.currentY < this.lastY ? y < this.currentY : y > this.currentY) && speed >= this.speed){
            this.syncCurrentY = y;
            this.speed = speed;
        }
    }

    public ElevatorCage getCage(){
        return this.cage;
    }

    public double getTargetSpeed(){
        return this.targetSpeed;
    }

    public void setTargetSpeed(double targetSpeed){
        this.targetSpeed = targetSpeed;
        this.shouldBeSynced = true;
    }

    public int getCageSideOffset(){
        return this.cageSideOffset;
    }

    public boolean canIncreaseCageSideOffset(){
        return !this.isMoving() && this.cageSideOffset < 2 + (this.facing == Direction.NORTH || this.facing == Direction.WEST ? (this.getCageWidth() - 1) / 2 : (int)Math.ceil((this.getCageWidth() - 1) / 2f));
    }

    public void increaseCageSideOffset(){
        if(this.canIncreaseCageSideOffset()){
            this.cageSideOffset++;
            this.shouldBeSynced = true;
        }
    }

    public boolean canDecreaseCageSideOffset(){
        return !this.isMoving() && this.cageSideOffset > -2 - (this.facing == Direction.NORTH || this.facing == Direction.WEST ? (int)Math.ceil((this.getCageWidth() - 1) / 2f) : (this.getCageWidth() - 1) / 2);
    }

    public void decreaseCageSideOffset(){
        if(this.canDecreaseCageSideOffset()){
            this.cageSideOffset--;
            this.shouldBeSynced = true;
        }
    }

    public int getCageDepthOffset(){
        return this.cageDepthOffset;
    }

    public boolean canIncreaseCageDepthOffset(){
        return !this.isMoving() && this.cageDepthOffset < 2;
    }

    public void increaseCageDepthOffset(){
        if(this.canIncreaseCageDepthOffset()){
            this.cageDepthOffset++;
            this.shouldBeSynced = true;
        }
    }

    public boolean canDecreaseCageDepthOffset(){
        return !this.isMoving() && this.cageDepthOffset > 0;
    }

    public void decreaseCageDepthOffset(){
        if(this.canDecreaseCageDepthOffset()){
            this.cageDepthOffset--;
            this.shouldBeSynced = true;
        }
    }

    public int getCageHeightOffset(){
        return this.cageHeightOffset;
    }

    public boolean canIncreaseCageHeightOffset(){
        return !this.isMoving() && this.cageHeightOffset < 3;
    }

    public void increaseCageHeightOffset(){
        if(this.canIncreaseCageHeightOffset()){
            this.cageHeightOffset++;
            this.shouldBeSynced = true;
        }
    }

    public boolean canDecreaseCageHeightOffset(){
        return !this.isMoving() && this.cageHeightOffset > -this.cageSizeY;
    }

    public void decreaseCageHeightOffset(){
        if(this.canDecreaseCageHeightOffset()){
            this.cageHeightOffset--;
            this.shouldBeSynced = true;
        }
    }

    public int getCageWidth(){
        return this.facing.getAxis() == Direction.Axis.X ? this.cageSizeZ : this.cageSizeX;
    }

    public boolean canIncreaseCageWidth(){
        return !this.isMoving && this.getCageWidth() < MovingElevatorsConfig.maxCabinHorizontalSize.get();
    }

    public void increaseCageWidth(){
        if(!this.isMoving && this.canIncreaseCageWidth()){
            if(this.facing.getAxis() == Direction.Axis.X)
                this.cageSizeZ++;
            else
                this.cageSizeX++;
            this.shouldBeSynced = true;
        }
    }

    public boolean canDecreaseCageWidth(){
        return !this.isMoving && this.getCageWidth() > 1;
    }

    public void decreaseCageWidth(){
        if(!this.isMoving && this.canDecreaseCageWidth()){
            if(this.facing.getAxis() == Direction.Axis.X)
                this.cageSizeZ--;
            else
                this.cageSizeX--;
            if(this.cageSideOffset > 2 + (this.facing == Direction.NORTH || this.facing == Direction.WEST ? (this.getCageWidth() - 1) / 2 : (int)Math.ceil((this.getCageWidth() - 1) / 2f)))
                this.cageSideOffset = 2 + (this.facing == Direction.NORTH || this.facing == Direction.WEST ? (this.getCageWidth() - 1) / 2 : (int)Math.ceil((this.getCageWidth() - 1) / 2f));
            else if(this.cageSideOffset < -2 - (this.facing == Direction.NORTH || this.facing == Direction.WEST ? (int)Math.ceil((this.getCageWidth() - 1) / 2f) : (this.getCageWidth() - 1) / 2))
                this.cageSideOffset = -2 - (this.facing == Direction.NORTH || this.facing == Direction.WEST ? (int)Math.ceil((this.getCageWidth() - 1) / 2f) : (this.getCageWidth() - 1) / 2);
            this.shouldBeSynced = true;
        }
    }

    public int getCageDepth(){
        return this.facing.getAxis() == Direction.Axis.X ? this.cageSizeX : this.cageSizeZ;
    }

    public boolean canIncreaseCageDepth(){
        return !this.isMoving && this.getCageDepth() < MovingElevatorsConfig.maxCabinHorizontalSize.get();
    }

    public void increaseCageDepth(){
        if(!this.isMoving && this.canIncreaseCageDepth()){
            if(this.facing.getAxis() == Direction.Axis.X)
                this.cageSizeX++;
            else
                this.cageSizeZ++;
            this.shouldBeSynced = true;
        }
    }

    public boolean canDecreaseCageDepth(){
        return !this.isMoving && this.getCageDepth() > 1;
    }

    public void decreaseCageDepth(){
        if(!this.isMoving && this.canDecreaseCageDepth()){
            if(this.facing.getAxis() == Direction.Axis.X)
                this.cageSizeX--;
            else
                this.cageSizeZ--;
            this.shouldBeSynced = true;
        }
    }

    public int getCageHeight(){
        return this.getCageSizeY();
    }

    public boolean canIncreaseCageHeight(){
        return !this.isMoving && this.cageSizeY < MovingElevatorsConfig.maxCabinVerticalSize.get();
    }

    public void increaseCageHeight(){
        if(!this.isMoving && this.canIncreaseCageHeight()){
            this.cageSizeY++;
            this.shouldBeSynced = true;
        }
    }

    public boolean canDecreaseCageHeight(){
        return !this.isMoving && this.cageSizeY > 1;
    }

    public void decreaseCageHeight(){
        if(!this.isMoving && this.canDecreaseCageHeight()){
            this.cageSizeY--;
            if(this.cageHeightOffset < -this.cageSizeY)
                this.cageHeightOffset = -this.cageSizeY;
            this.shouldBeSynced = true;
        }
    }

    public int getCageSizeX(){
        return this.cageSizeX;
    }

    public int getCageSizeY(){
        return this.cageSizeY;
    }

    public int getCageSizeZ(){
        return this.cageSizeZ;
    }

    public DyeColor getFloorDisplayColor(int floor){
        return this.floorData.get(floor).color;
    }

    public String getFloorDisplayName(int floor){
        return this.floorData.get(floor).name;
    }

    /**
     * @param y y-level of the elevator controller
     */
    public BlockPos getCageAnchorBlockPos(int y){
        int x = 0, z = 0;
        if(this.facing == Direction.NORTH){
            x = this.x - this.cageSizeX / 2 - this.cageSideOffset;
            z = this.z - this.cageSizeZ - this.cageDepthOffset;
        }else if(this.facing == Direction.SOUTH){
            x = this.x - this.cageSizeX / 2 + this.cageSideOffset;
            z = this.z + 1 + this.cageDepthOffset;
        }else if(this.facing == Direction.WEST){
            x = this.x - this.cageSizeX - this.cageDepthOffset;
            z = this.z - this.cageSizeZ / 2 + this.cageSideOffset;
        }else if(this.facing == Direction.EAST){
            x = this.x + 1 + this.cageDepthOffset;
            z = this.z - this.cageSizeZ / 2 - this.cageSideOffset;
        }
        y += this.cageHeightOffset;
        return new BlockPos(x, y, z);
    }

    public Vector3d getCageAnchorPos(double y){
        BlockPos pos = this.getCageAnchorBlockPos(0);
        return new Vector3d(pos.getX(), y + this.cageHeightOffset, pos.getZ());
    }

    /**
     * @return whether the blocks in front of the given {@code tile} are suitable
     * for a cage
     */
    public boolean isCageAvailableAt(ControllerBlockEntity tile){
        return ElevatorCage.canCreateCage(this.world, this.getCageAnchorBlockPos(tile.getBlockPos().getY()), this.cageSizeX, this.cageSizeY, this.cageSizeZ);
    }

    /**
     * @return whether there is enough space for the cage to be placed in front
     * of the given {@code tile}
     */
    public boolean canCageBePlacedAt(ControllerBlockEntity tile){
        BlockPos startPos = this.getCageAnchorBlockPos(tile.getBlockPos().getY());
        for(int x = 0; x < this.cageSizeX; x++){
            for(int y = 0; y < this.cageSizeY; y++){
                for(int z = 0; z < this.cageSizeZ; z++){
                    if(!this.world.isEmptyBlock(startPos.offset(x, y, z)))
                        return false;
                }
            }
        }
        return true;
    }

    public void addComparatorListener(int floorYLevel, BlockPos blockPos){
        this.comparatorListeners.putIfAbsent(floorYLevel, new HashSet<>());
        this.comparatorListeners.get(floorYLevel).add(blockPos);
    }

    public boolean removeComparatorListener(BlockPos blockPos){
        boolean removed = false;
        Iterator<Set<BlockPos>> iterator = this.comparatorListeners.values().iterator();
        while(iterator.hasNext()){
            Set<BlockPos> positions = iterator.next();
            if(positions.remove(blockPos)){
                removed = true;
                if(positions.isEmpty())
                    iterator.remove();
            }
        }
        return removed;
    }

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();
        compound.putBoolean("isMoving", this.isMoving);
        if(this.isMoving){
            compound.putInt("targetY", this.targetY);
            compound.putDouble("lastY", this.lastY);
            compound.putDouble("currentY", this.currentY);
            compound.put("cage", this.cage.write());
        }
        compound.putDouble("targetSpeed", this.targetSpeed);
        compound.putDouble("speed", this.speed);
        compound.putInt("cageSideOffset", this.cageSideOffset);
        compound.putInt("cageDepthOffset", this.cageDepthOffset);
        compound.putInt("cageHeightOffset", this.cageHeightOffset);
        compound.putInt("cageSizeX", this.cageSizeX);
        compound.putInt("cageSizeY", this.cageSizeY);
        compound.putInt("cageSizeZ", this.cageSizeZ);
        compound.putIntArray("floors", this.floors);
        ListNBT floorDataTag = new ListNBT();
        for(FloorData floorDatum : this.floorData)
            floorDataTag.add(floorDatum.write());
        compound.put("floorData", floorDataTag);
        return compound;
    }

    public void read(CompoundNBT compound){
        if(compound.contains("moving")){ // old version stuff
            this.isMoving = compound.getBoolean("moving");
            int size = compound.getInt("size");
            if(this.isMoving){
                this.targetY = compound.getInt("targetY");
                this.lastY = compound.getDouble("lastY");
                this.currentY = compound.getDouble("currentY");
                BlockState[][][] blockStates = new BlockState[size][1][size];
                VoxelShape shape = VoxelShapes.empty();
                for(int x = 0; x < size; x++){
                    for(int z = 0; z < size; z++){
                        BlockState state = Block.stateById(compound.getInt("platform" + x + "," + z));
                        if(state.getBlock() != Blocks.AIR){
                            blockStates[x][0][z] = state;
                            shape = VoxelShapes.joinUnoptimized(shape, state.getCollisionShape(this.world, this.getPos((int)this.currentY)), IBooleanFunction.OR);
                        }
                    }
                }
                shape.optimize();
                this.cage = new ElevatorCage(size, 1, size, blockStates, shape.toAabbs());
            }
            this.targetSpeed = compound.getDouble("speed");
            this.speed = this.targetSpeed;
            this.cageSizeX = this.cageSizeZ = size;
            this.cageSizeY = 1;
        }else{
            this.isMoving = compound.getBoolean("isMoving");
            if(this.isMoving){
                this.targetY = compound.getInt("targetY");
                this.lastY = compound.getDouble("lastY");
                this.currentY = compound.getDouble("currentY");
                this.cage = ElevatorCage.read(compound.getCompound("cage"));
            }
            this.targetSpeed = compound.getDouble("targetSpeed");
            this.speed = compound.getDouble("speed");
            this.cageSideOffset = compound.getInt("cageSideOffset");
            this.cageDepthOffset = compound.getInt("cageDepthOffset");
            this.cageHeightOffset = compound.getInt("cageHeightOffset");
            this.cageSizeX = compound.getInt("cageSizeX");
            this.cageSizeY = compound.getInt("cageSizeY");
            this.cageSizeZ = compound.getInt("cageSizeZ");
        }
        this.floors.clear();
        for(int y : compound.getIntArray("floors"))
            this.floors.add(y);
        this.floorData.clear();
        ListNBT floorDataTag = (ListNBT)compound.get("floorData");
        for(INBT data : floorDataTag)
            this.floorData.add(FloorData.read((CompoundNBT)data));
    }

    private BlockPos getPos(int y){
        return new BlockPos(this.x, y, this.z);
    }

    private ControllerBlockEntity getTile(int y){
        if(this.world == null)
            return null;
        TileEntity tile = this.world.getBlockEntity(this.getPos(y));
        return tile instanceof ControllerBlockEntity ? (ControllerBlockEntity)tile : null;
    }

    public int getFloorCount(){
        return this.floors.size();
    }

    public int getFloorNumber(int y){
        return this.floors.indexOf(y);
    }

    public int getFloorYLevel(int floor){
        return this.floors.get(floor);
    }

    public ControllerBlockEntity getTileForFloor(int floor){
        if(floor < 0 || floor >= this.floors.size())
            return null;
        return this.getTile(this.floors.get(floor));
    }

    private void updateGroup(){
        this.world.getCapability(ElevatorGroupCapability.CAPABILITY).ifPresent(groups -> groups.updateGroup(this));
    }

    private void syncMovement(){
        if(!this.world.isClientSide)
            MovingElevators.CHANNEL.sendToDimension(this.world.dimension(), new PacketSyncElevatorMovement(this.x, this.z, this.facing, this.currentY, this.speed));
    }

    private static class FloorData {

        public String name;
        public DyeColor color;

        public FloorData(String name, DyeColor color){
            this.name = name;
            this.color = color;
        }

        public CompoundNBT write(){
            CompoundNBT tag = new CompoundNBT();
            if(this.name != null)
                tag.putString("name", this.name);
            tag.putInt("color", this.color.getId());
            return tag;
        }

        public static FloorData read(CompoundNBT tag){
            return new FloorData(tag.contains("name") ? tag.getString("name") : null, DyeColor.byId(tag.getInt("color")));
        }
    }
}
