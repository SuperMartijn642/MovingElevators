package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.MovingElevatorsConfig;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.packets.PacketSyncElevatorMovement;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class ElevatorGroup {

    private static final int RE_SYNC_INTERVAL = 10;
    private static final double ACCELERATION = 0.05;

    public final World level;
    public final int x, z;
    public final EnumFacing facing;

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
    private final Map<Integer,Set<BlockPos>> comparatorListeners = new Int2ObjectArrayMap<>();

    private int syncCounter = 0;

    public ElevatorGroup(World level, int x, int z, EnumFacing facing){
        this.level = level;
        this.x = x;
        this.z = z;
        this.facing = facing;
    }

    public void update(){
        if(!this.level.isRemote && this.shouldBeSynced){
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
        ElevatorCollisionHandler.handleEntityCollisions(this.level, this.cage.bounds, this.cage.collisionBoxes, this.getCageAnchorPos(oldY), new Vec3d(this.x, newY - oldY, 0));
    }

    private void stopElevator(){
        this.isMoving = false;

        this.cage.place(this.level, this.getCageAnchorBlockPos(this.targetY));

        this.moveElevator(this.lastY, this.currentY);

        if(!this.level.isRemote){
            this.level.updateComparatorOutputLevel(this.getPos(this.targetY), MovingElevators.elevator_block);
            for(BlockPos pos : this.comparatorListeners.getOrDefault(this.targetY, Collections.emptySet()))
                if(this.level.isBlockLoaded(pos))
                    this.level.updateComparatorOutputLevel(pos, this.level.getBlockState(pos).getBlock());
            this.shouldBeSynced = true;
            Vec3d soundPos = this.getCageAnchorPos(this.targetY).addVector(this.cageSizeX / 2d, this.cageSizeY / 2d, this.cageSizeZ / 2d);
            this.level.playSound(null, soundPos.x, soundPos.y, soundPos.z, MovingElevators.arrive_sound, SoundCategory.BLOCKS, 0.4f, 0.5f);
            this.syncCounter = 0;
        }
    }

    private void startElevator(int currentY, int targetY){
        if(this.level == null || this.isMoving)
            return;

        ElevatorCage cage = ElevatorCage.createCageAndClear(this.level, this.getCageAnchorBlockPos(currentY), this.cageSizeX, this.cageSizeY, this.cageSizeZ);
        if(cage == null)
            return;

        this.cage = cage;
        this.isMoving = true;
        this.targetY = targetY;
        this.currentY = currentY;
        this.lastY = this.currentY;
        this.speed = 0;

        if(!this.level.isRemote){
            this.level.updateComparatorOutputLevel(this.getPos(currentY), MovingElevators.elevator_block);
            for(BlockPos pos : this.comparatorListeners.getOrDefault(currentY, Collections.emptySet()))
                if(this.level.isBlockLoaded(pos))
                    this.level.updateComparatorOutputLevel(pos, this.level.getBlockState(pos).getBlock());
            this.updateGroup();
        }
    }

    public void onButtonPress(boolean isUp, boolean isDown, int yLevel){
        if(this.isMoving || !this.floors.contains(yLevel))
            return;

        ControllerBlockEntity entity = this.getEntity(yLevel);
        if(entity == null)
            return;

        if(isUp){
            if(this.isCageAvailableAt(entity)){
                for(int floor = this.floors.indexOf(yLevel) + 1; floor < this.floors.size(); floor++){
                    ControllerBlockEntity entity2 = this.getEntity(this.floors.get(floor));
                    if(entity2 != null){
                        if(this.canCageBePlacedAt(entity2))
                            this.startElevator(yLevel, this.floors.get(floor));
                        return;
                    }
                }
            }
        }else if(isDown){
            if(this.isCageAvailableAt(entity)){
                for(int floor = this.floors.indexOf(yLevel) - 1; floor >= 0; floor--){
                    ControllerBlockEntity entity2 = this.getEntity(this.floors.get(floor));
                    if(entity2 != null){
                        if(this.canCageBePlacedAt(entity2))
                            this.startElevator(yLevel, this.floors.get(floor));
                        return;
                    }
                }
            }
        }else{
            if(this.canCageBePlacedAt(entity)){
                this.floors.sort(Comparator.comparingInt(a -> Math.abs(a - yLevel)));
                for(int y : this.floors){
                    if(y != yLevel){
                        ControllerBlockEntity entity2 = this.getEntity(y);
                        if(entity2 != null && this.isCageAvailableAt(entity2)){
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

        ControllerBlockEntity entity = this.getEntity(yLevel);
        int toY = this.floors.get(toFloor);
        ControllerBlockEntity toEntity = this.getEntity(toY);
        if(entity != null && toEntity != null && this.isCageAvailableAt(entity) && this.canCageBePlacedAt(toEntity))
            this.startElevator(yLevel, toY);
    }

    public void remove(ControllerBlockEntity entity){
        int floor = this.getFloorNumber(entity.getPos().getY());
        this.floors.remove(floor);
        this.floorData.remove(floor);
        if(this.floors.isEmpty()){
            if(this.isMoving){
                Vec3d spawnPos = this.getCageAnchorPos(this.targetY).addVector(this.cageSizeX / 2d, this.cageSizeY / 2d, this.cageSizeZ / 2d);
                this.cage.getDrops().forEach(stack -> {
                    EntityItem itemEntity = new EntityItem(this.level, spawnPos.x, spawnPos.y, spawnPos.z, stack);
                    this.level.spawnEntity(itemEntity);
                });
            }
        }else
            this.shouldBeSynced = true;
    }

    public void add(ControllerBlockEntity entity){
        if(entity == null)
            return;
        int y = entity.getPos().getY();
        if(this.floors.contains(y))
            return;
        FloorData floorData = new FloorData(entity.getFloorName(), entity.getDisplayLabelColor());
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

    public void updateFloorData(ControllerBlockEntity entity, String name, EnumDyeColor color){
        int floor = this.getFloorNumber(entity.getPos().getY());
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
        return !this.isMoving() && this.cageSideOffset < 2 + (this.facing == EnumFacing.NORTH || this.facing == EnumFacing.WEST ? (this.getCageWidth() - 1) / 2 : (int)Math.ceil((this.getCageWidth() - 1) / 2f));
    }

    public void increaseCageSideOffset(){
        if(this.canIncreaseCageSideOffset()){
            this.cageSideOffset++;
            this.shouldBeSynced = true;
        }
    }

    public boolean canDecreaseCageSideOffset(){
        return !this.isMoving() && this.cageSideOffset > -2 - (this.facing == EnumFacing.NORTH || this.facing == EnumFacing.WEST ? (int)Math.ceil((this.getCageWidth() - 1) / 2f) : (this.getCageWidth() - 1) / 2);
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
        return this.facing.getAxis() == EnumFacing.Axis.X ? this.cageSizeZ : this.cageSizeX;
    }

    public boolean canIncreaseCageWidth(){
        return !this.isMoving && this.getCageWidth() < MovingElevatorsConfig.maxCabinHorizontalSize.get();
    }

    public void increaseCageWidth(){
        if(!this.isMoving && this.canIncreaseCageWidth()){
            if(this.facing.getAxis() == EnumFacing.Axis.X)
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
            if(this.facing.getAxis() == EnumFacing.Axis.X)
                this.cageSizeZ--;
            else
                this.cageSizeX--;
            if(this.cageSideOffset > 2 + (this.facing == EnumFacing.NORTH || this.facing == EnumFacing.WEST ? (this.getCageWidth() - 1) / 2 : (int)Math.ceil((this.getCageWidth() - 1) / 2f)))
                this.cageSideOffset = 2 + (this.facing == EnumFacing.NORTH || this.facing == EnumFacing.WEST ? (this.getCageWidth() - 1) / 2 : (int)Math.ceil((this.getCageWidth() - 1) / 2f));
            else if(this.cageSideOffset < -2 - (this.facing == EnumFacing.NORTH || this.facing == EnumFacing.WEST ? (int)Math.ceil((this.getCageWidth() - 1) / 2f) : (this.getCageWidth() - 1) / 2))
                this.cageSideOffset = -2 - (this.facing == EnumFacing.NORTH || this.facing == EnumFacing.WEST ? (int)Math.ceil((this.getCageWidth() - 1) / 2f) : (this.getCageWidth() - 1) / 2);
            this.shouldBeSynced = true;
        }
    }

    public int getCageDepth(){
        return this.facing.getAxis() == EnumFacing.Axis.X ? this.cageSizeX : this.cageSizeZ;
    }

    public boolean canIncreaseCageDepth(){
        return !this.isMoving && this.getCageDepth() < MovingElevatorsConfig.maxCabinHorizontalSize.get();
    }

    public void increaseCageDepth(){
        if(!this.isMoving && this.canIncreaseCageDepth()){
            if(this.facing.getAxis() == EnumFacing.Axis.X)
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
            if(this.facing.getAxis() == EnumFacing.Axis.X)
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

    public EnumDyeColor getFloorDisplayColor(int floor){
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
        if(this.facing == EnumFacing.NORTH){
            x = this.x - this.cageSizeX / 2 - this.cageSideOffset;
            z = this.z - this.cageSizeZ - this.cageDepthOffset;
        }else if(this.facing == EnumFacing.SOUTH){
            x = this.x - this.cageSizeX / 2 + this.cageSideOffset;
            z = this.z + 1 + this.cageDepthOffset;
        }else if(this.facing == EnumFacing.WEST){
            x = this.x - this.cageSizeX - this.cageDepthOffset;
            z = this.z - this.cageSizeZ / 2 + this.cageSideOffset;
        }else if(this.facing == EnumFacing.EAST){
            x = this.x + 1 + this.cageDepthOffset;
            z = this.z - this.cageSizeZ / 2 - this.cageSideOffset;
        }
        y += this.cageHeightOffset;
        return new BlockPos(x, y, z);
    }

    public Vec3d getCageAnchorPos(double y){
        BlockPos pos = this.getCageAnchorBlockPos(0);
        return new Vec3d(pos.getX(), y + this.cageHeightOffset, pos.getZ());
    }

    /**
     * @return whether the blocks in front of the given {@code entity} are suitable
     * for a cage
     */
    public boolean isCageAvailableAt(ControllerBlockEntity entity){
        return ElevatorCage.canCreateCage(this.level, this.getCageAnchorBlockPos(entity.getPos().getY()), this.cageSizeX, this.cageSizeY, this.cageSizeZ);
    }

    /**
     * @return whether there is enough space for the cage to be placed in front
     * of the given {@code entity}
     */
    public boolean canCageBePlacedAt(ControllerBlockEntity entity){
        BlockPos startPos = this.getCageAnchorBlockPos(entity.getPos().getY());
        for(int x = 0; x < this.cageSizeX; x++){
            for(int y = 0; y < this.cageSizeY; y++){
                for(int z = 0; z < this.cageSizeZ; z++){
                    if(!this.level.isAirBlock(startPos.add(x, y, z)))
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
            if(positions.remove(blockPos))
                removed = true;
        }
        return removed;
    }

    public NBTTagCompound write(){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("isMoving", this.isMoving);
        if(this.isMoving){
            compound.setInteger("targetY", this.targetY);
            compound.setDouble("lastY", this.lastY);
            compound.setDouble("currentY", this.currentY);
            compound.setTag("cage", this.cage.write());
        }
        compound.setDouble("targetSpeed", this.targetSpeed);
        compound.setDouble("speed", this.speed);
        compound.setInteger("cageSideOffset", this.cageSideOffset);
        compound.setInteger("cageDepthOffset", this.cageDepthOffset);
        compound.setInteger("cageHeightOffset", this.cageHeightOffset);
        compound.setInteger("cageSizeX", this.cageSizeX);
        compound.setInteger("cageSizeY", this.cageSizeY);
        compound.setInteger("cageSizeZ", this.cageSizeZ);
        int[] arr = new int[this.floors.size()];
        for(int i = 0; i < this.floors.size(); i++)
            arr[i] = this.floors.get(i);
        compound.setIntArray("floors", arr);
        NBTTagList floorDataTag = new NBTTagList();
        for(FloorData floorDatum : this.floorData)
            floorDataTag.appendTag(floorDatum.write());
        compound.setTag("floorData", floorDataTag);
        return compound;
    }

    public void read(NBTTagCompound compound){
        if(compound.hasKey("moving")){ // old version stuff
            this.isMoving = compound.getBoolean("moving");
            int size = compound.getInteger("size");
            if(this.isMoving){
                this.targetY = compound.getInteger("targetY");
                this.lastY = compound.getDouble("lastY");
                this.currentY = compound.getDouble("currentY");
                IBlockState[][][] blockStates = new IBlockState[size][1][size];
                BlockShape shape = BlockShape.empty();
                for(int x = 0; x < size; x++){
                    for(int z = 0; z < size; z++){
                        IBlockState state = Block.getStateById(compound.getInteger("platform" + x + "," + z));
                        if(state.getBlock() != Blocks.AIR){
                            blockStates[x][0][z] = state;
                            shape = BlockShape.or(shape, BlockShape.create(state.getCollisionBoundingBox(this.level, this.getPos((int)this.currentY))));
                        }
                    }
                }
                // TODO reduce the number of collision boxes
//                shape.optimize();
                this.cage = this.level.isRemote ?
                    new ClientElevatorCage(size, 1, size, blockStates, new NBTTagCompound[size][1][size], new NBTTagCompound[size][1][size], shape.toBoxes()) :
                    new ElevatorCage(size, 1, size, blockStates, new NBTTagCompound[size][1][size], new NBTTagCompound[size][1][size], shape.toBoxes());
            }
            this.targetSpeed = compound.getDouble("speed");
            this.speed = this.targetSpeed;
            this.cageSizeX = this.cageSizeZ = size;
            this.cageSizeY = 1;
        }else{
            this.isMoving = compound.getBoolean("isMoving");
            if(this.isMoving){
                this.targetY = compound.getInteger("targetY");
                this.lastY = compound.getDouble("lastY");
                this.currentY = compound.getDouble("currentY");
                this.cage = ElevatorCage.read(compound.getCompoundTag("cage"), this.level.isRemote);
            }
            this.targetSpeed = compound.getDouble("targetSpeed");
            this.speed = compound.getDouble("speed");
            this.cageSideOffset = compound.getInteger("cageSideOffset");
            this.cageDepthOffset = compound.getInteger("cageDepthOffset");
            this.cageHeightOffset = compound.getInteger("cageHeightOffset");
            this.cageSizeX = compound.getInteger("cageSizeX");
            this.cageSizeY = compound.getInteger("cageSizeY");
            this.cageSizeZ = compound.getInteger("cageSizeZ");
        }
        this.floors.clear();
        for(int y : compound.getIntArray("floors"))
            this.floors.add(y);
        this.floorData.clear();
        if(compound.hasKey("floorData", Constants.NBT.TAG_LIST)){
            NBTBase base = compound.getTag("floorData");
            if(base instanceof NBTTagList){
                NBTTagList floorDataTag = (NBTTagList)base;
                for(NBTBase tag : floorDataTag)
                    this.floorData.add(FloorData.read((NBTTagCompound)tag));
            }
        }
    }

    private BlockPos getPos(int y){
        return new BlockPos(this.x, y, this.z);
    }

    private ControllerBlockEntity getEntity(int y){
        if(this.level == null)
            return null;
        TileEntity entity = this.level.getTileEntity(this.getPos(y));
        return entity instanceof ControllerBlockEntity ? (ControllerBlockEntity)entity : null;
    }

    public int getFloorCount(){
        return this.floors.size();
    }

    public int getFloorNumber(int y){
        return this.floors.indexOf(y);
    }

    public int getClosestFloorNumber(int y){
        if(y < this.floors.get(0))
            return 0;
        for(int floor = 1; floor < this.floors.size(); floor++){
            if(y < (this.floors.get(floor - 1) + this.floors.get(floor)) / 2)
                return floor - 1;
        }
        return this.floors.size() - 1; // this should never be reached
    }

    public int getFloorYLevel(int floor){
        return this.floors.get(floor);
    }

    public ControllerBlockEntity getEntityForFloor(int floor){
        if(floor < 0 || floor >= this.floors.size())
            return null;
        return this.getEntity(this.floors.get(floor));
    }

    public boolean hasControllerAt(int yLevel){
        return this.floors.contains(yLevel);
    }

    private void updateGroup(){
        ElevatorGroupCapability.get(this.level).updateGroup(this);
    }

    private void syncMovement(){
        if(!this.level.isRemote)
            MovingElevators.CHANNEL.sendToDimension(this.level, new PacketSyncElevatorMovement(this.x, this.z, this.facing, this.currentY, this.speed));
    }

    private static class FloorData {

        public String name;
        public EnumDyeColor color;

        public FloorData(String name, EnumDyeColor color){
            this.name = name;
            this.color = color;
        }

        public NBTTagCompound write(){
            NBTTagCompound tag = new NBTTagCompound();
            if(this.name != null)
                tag.setString("name", this.name);
            tag.setInteger("color", this.color.getDyeDamage());
            return tag;
        }

        public static FloorData read(NBTTagCompound tag){
            return new FloorData(tag.hasKey("name") ? tag.getString("name") : null, EnumDyeColor.byDyeDamage(tag.getInteger("color")));
        }
    }
}
