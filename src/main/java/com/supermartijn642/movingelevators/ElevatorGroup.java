package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class ElevatorGroup {

    public static final Field floatingTickCount = ObfuscationReflectionHelper.findField(ServerPlayNetHandler.class, "field_147365_f");

    private World world;
    private final int x, z;
    private Direction facing;

    private boolean isMoving = false;
    private int targetY;
    private double lastY;
    private double currentY;
    private int size = 3;
    private int nextSize = size;
    private double speed = 0.2;
    private double nextSpeed = speed;
    private BlockState[][] platform = new BlockState[this.size][this.size];
    private final ArrayList<Integer> floors = new ArrayList<>();

    public ElevatorGroup(World world, int x, int z, Direction facing){
        this.world = world;
        this.x = x;
        this.z = z;
        this.facing = facing;
    }

    public void update(ElevatorBlockTile tile){
        if(this.world == null)
            this.world = tile.getWorld();
        if(this.isMoving){
            this.lastY = this.currentY;
            if(this.currentY == this.targetY)
                this.stopElevator();
            else if(Math.abs(this.targetY - this.currentY) < speed){
                this.currentY = this.targetY;
                this.moveElevator(this.lastY, this.currentY);
            }else{
                this.currentY += Math.signum(this.targetY - this.currentY) * speed;
                this.moveElevator(this.lastY, this.currentY);
            }
        }else if(this.nextSize != this.size || this.nextSpeed != this.speed){
            this.size = this.nextSize;
            this.speed = this.nextSpeed;
            this.platform = new BlockState[this.size][this.size];
            BlockState state = this.world.getBlockState(this.getPos(this.getLowest()));
            this.world.notifyBlockUpdate(this.getPos(this.getLowest()), state, state, 2);
            this.markDirty();
        }
    }

    private void moveElevator(double oldY, double newY){
        int x = this.x + this.facing.getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int z = this.z + this.facing.getZOffset() * (int)Math.ceil(size / 2f) - size / 2;

        AxisAlignedBB box = new AxisAlignedBB(x, Math.min(oldY, newY), z, x + this.size, Math.max(oldY, newY) + 1 + 3 * this.speed, z + this.size);

        List<? extends Entity> entities = this.world.getEntitiesWithinAABB((EntityType<?>)null, box, this::canCollideWith);

        for(Entity entity : entities){
            if((newY < oldY && entity.hasNoGravity()) || (entity instanceof PlayerEntity && entity.getMotion().y >= 0 && entity.getPosY() > Math.min(oldY, newY) + 1))
                continue;
            entity.setPosition(entity.getPosX(), newY + 1, entity.getPosZ());
            entity.onGround = true;
            entity.onLivingFall(entity.fallDistance, 1);
            entity.fallDistance = 0;
            entity.setMotion(entity.getMotion().x, 0, entity.getMotion().z);
            if(entity instanceof PlayerEntity){
                entity.getPersistentData().putLong("elevatorTime", entity.ticksExisted);
                if(entity instanceof ServerPlayerEntity){
                    try{
                        floatingTickCount.setInt(((ServerPlayerEntity)entity).connection, 0);
                    }catch(IllegalAccessException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        if(!this.world.isRemote)
            this.markDirty();
    }

    private boolean canCollideWith(Entity entity){
        return !entity.isSpectator() && !entity.noClip && entity.getPushReaction() == PushReaction.NORMAL;
    }

    private void stopElevator(){
        this.isMoving = false;

        int startX = this.x + this.facing.getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = this.z + this.facing.getZOffset() * (int)Math.ceil(size / 2f) - size / 2;
        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                BlockPos pos = new BlockPos(startX + x, this.targetY, startZ + z);
                if(!this.world.isAirBlock(pos))
                    this.world.destroyBlock(pos, true);
                this.world.setBlockState(pos, this.platform[x][z]);
            }
        }

        AxisAlignedBB box = new AxisAlignedBB(startX, this.currentY, startZ, startX + this.size, this.currentY + 1, startZ + this.size);

        List<? extends Entity> entities = this.world.getEntitiesWithinAABB((EntityType<?>)null, box, this::canCollideWith);

        for(Entity entity : entities){
            entity.setPositionAndUpdate(entity.getPosX(), this.currentY + 1, entity.getPosZ());
            entity.onGround = true;
            entity.fallDistance = 0;
            entity.setMotion(entity.getMotion().x, 0, entity.getMotion().z);
        }

        if(!this.world.isRemote){
            BlockState state = this.world.getBlockState(this.getPos(this.getLowest()));
            this.world.notifyBlockUpdate(this.getPos(this.getLowest()), state, state, 2);
            this.world.updateComparatorOutputLevel(this.getPos(this.targetY + 1), MovingElevators.elevator_block);
            this.markDirty();
            double x = this.x + this.facing.getXOffset() * (int)Math.ceil(size / 2f) + 0.5;
            double z = this.z + this.facing.getZOffset() * (int)Math.ceil(size / 2f) + 0.5;
            this.world.playSound(null, x, this.targetY + 2.5, z, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.4f, 0.5f);
        }
    }

    private void startElevator(int currentY, int targetY){
        if(this.world == null || this.isMoving)
            return;

        int startX = this.x + this.facing.getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = this.z + this.facing.getZOffset() * (int)Math.ceil(size / 2f) - size / 2;
        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                BlockPos pos = new BlockPos(startX + x, currentY - 1, startZ + z);
                if(this.world.isAirBlock(pos) || this.world.getTileEntity(pos) != null)
                    return;
                BlockState state = this.world.getBlockState(pos);
                if(state.getBlockHardness(this.world, pos) < 0)
                    return;
                if(!(state.getShape(this.world, pos).getEnd(Direction.Axis.Y) == 1.0 &&
                    state.getShape(this.world, pos).getStart(Direction.Axis.X) == 0 && state.getShape(this.world, pos).getEnd(Direction.Axis.X) == 1.0 &&
                    state.getShape(this.world, pos).getStart(Direction.Axis.Z) == 0 && state.getShape(this.world, pos).getEnd(Direction.Axis.Z) == 1.0))
                    return;
                this.platform[x][z] = state;
            }
        }

        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                this.world.setBlockState(new BlockPos(startX + x, currentY - 1, startZ + z), Blocks.AIR.getDefaultState());
            }
        }

        this.isMoving = true;
        this.targetY = targetY - 1;
        this.currentY = currentY - 1;
        this.lastY = this.currentY;
        if(!this.world.isRemote){
            BlockState state = this.world.getBlockState(this.getPos(this.getLowest()));
            this.world.notifyBlockUpdate(this.getPos(this.getLowest()), state, state, 2);
            this.world.updateComparatorOutputLevel(this.getPos(currentY), MovingElevators.elevator_block);
            this.markDirty();
        }
    }

    public void onButtonPress(boolean isUp, boolean isDown, int yLevel){
        if(this.isMoving || !this.floors.contains(yLevel))
            return;

        ElevatorBlockTile tile = this.getTile(yLevel);
        if(tile == null)
            return;

        if(isUp){
            if(tile.hasPlatform()){
                for(int floor = this.floors.indexOf(yLevel) + 1; floor < this.floors.size(); floor++){
                    ElevatorBlockTile tile2 = this.getTile(this.floors.get(floor));
                    if(tile2 != null){
                        if(tile2.hasSpaceForPlatform())
                            this.startElevator(yLevel, this.floors.get(floor));
                        return;
                    }
                }
            }
        }else if(isDown){
            if(tile.hasPlatform()){
                for(int floor = this.floors.indexOf(yLevel) - 1; floor >= 0; floor--){
                    ElevatorBlockTile tile2 = this.getTile(this.floors.get(floor));
                    if(tile2 != null){
                        if(tile2.hasSpaceForPlatform())
                            this.startElevator(yLevel, this.floors.get(floor));
                        return;
                    }
                }
            }
        }else{
            if(tile.hasSpaceForPlatform()){
                this.floors.sort(Comparator.comparingInt(a -> Math.abs(a - yLevel)));
                for(int y : this.floors){
                    if(y != yLevel){
                        ElevatorBlockTile tile2 = this.getTile(y);
                        if(tile2 != null && tile2.hasPlatform()){
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

        ElevatorBlockTile tile = this.getTile(yLevel);
        int toY = this.floors.get(toFloor);
        ElevatorBlockTile toTile = this.getTile(toY);
        if(tile != null && toTile != null && tile.hasPlatform() && toTile.hasSpaceForPlatform())
            this.startElevator(yLevel, toY);
    }

    public void remove(int y){
        this.floors.remove((Integer)y);
        if(this.floors.isEmpty()){
            if(this.isMoving){
                BlockPos spawnPos = this.getPos(y).offset(this.facing, this.size / 2 + 1);
                for(BlockState[] arr : this.platform){
                    for(BlockState state : arr){
                        ItemEntity entity = new ItemEntity(this.world, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, new ItemStack(state.getBlock()));
                        this.world.addEntity(entity);
                    }
                }
            }
        }else if(!this.world.isRemote){
            BlockState state = this.world.getBlockState(this.getPos(this.getLowest()));
            this.world.notifyBlockUpdate(this.getPos(this.getLowest()), state, state, 2);
            this.markDirty();
        }
    }

    public void add(ElevatorBlockTile tile){
        if(tile == null)
            return;
        if(this.world == null)
            this.world = tile.getWorld();
        tile.setGroup(this);
        int y = tile.getPos().getY();
        if(this.floors.contains(y))
            return;
        if(this.floors.isEmpty())
            this.floors.add(y);
        for(int i = 0; i < this.floors.size(); i++){
            if(y < this.floors.get(i)){
                this.floors.add(i, y);
                break;
            }
        }
        if(!this.floors.contains(y))
            this.floors.add(y);
        if(!this.world.isRemote){
            BlockState state = this.world.getBlockState(this.getPos(this.getLowest()));
            this.world.notifyBlockUpdate(this.getPos(this.getLowest()), state, state, 2);
            this.markDirty();
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

    public BlockState[][] getPlatform(){
        return this.platform;
    }

    public int getSize(){
        return this.size;
    }

    public void setSize(int size){
        this.nextSize = size;
    }

    public double getSpeed(){
        return this.speed;
    }

    public void setSpeed(double speed){
        this.nextSpeed = speed;
    }

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("moving", this.isMoving);
        tag.putInt("size", this.size);
        if(this.isMoving){
            tag.putInt("targetY", this.targetY);
            tag.putDouble("lastY", this.lastY);
            tag.putDouble("currentY", this.currentY);
            for(int x = 0; x < this.size; x++){
                for(int z = 0; z < this.size; z++){
                    tag.putInt("platform" + x + "," + z, Block.getStateId(this.platform[x][z]));
                }
            }
        }
        tag.putDouble("speed", this.speed);
        tag.putIntArray("floors", this.floors);
        return tag;
    }

    public void read(CompoundNBT tag){
        if(tag.contains("moving"))
            this.isMoving = tag.getBoolean("moving");
        if(tag.contains("targetY"))
            this.targetY = tag.getInt("targetY");
        if(tag.contains("lastY"))
            this.lastY = tag.getDouble("lastY");
        if(tag.contains("currentY"))
            this.currentY = tag.getDouble("currentY");
        if(tag.contains("size")){
            this.size = tag.getInt("size");
            this.nextSize = this.size;
            this.platform = new BlockState[this.size][this.size];
        }
        if(tag.contains("speed")){
            this.speed = tag.getDouble("speed");
            this.nextSpeed = this.speed;
        }
        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                this.platform[x][z] = Block.getStateById(tag.getInt("platform" + x + "," + z));
            }
        }
        if(tag.contains("floors")){
            this.floors.clear();
            for(int y : tag.getIntArray("floors")){
                this.floors.add(y);
                ElevatorBlockTile tile = this.getTile(y);
                if(tile != null)
                    tile.setGroup(this);
            }
        }
    }

    private BlockPos getPos(int y){
        return new BlockPos(this.x, y, this.z);
    }

    private ElevatorBlockTile getTile(int y){
        if(this.world == null)
            return null;
        TileEntity tile = this.world.getTileEntity(this.getPos(y));
        return tile instanceof ElevatorBlockTile ? (ElevatorBlockTile)tile : null;
    }

    public int getLowest(){
        return this.floors.get(0);
    }

    public void setFacing(Direction facing){
        this.facing = facing;
    }

    public List<ElevatorBlockTile> getTiles(){
        ArrayList<ElevatorBlockTile> tiles = new ArrayList<>(this.floors.size());
        for(int y : this.floors){
            ElevatorBlockTile tile = this.getTile(y);
            if(tile != null)
                tiles.add(tile);
        }
        return tiles;
    }

    public int getFloorNumber(int y){
        return this.floors.indexOf(y);
    }

    public ElevatorBlockTile getTileForFloor(int floor){
        if(floor < 0 || floor >= this.floors.size())
            return null;
        return this.getTile(this.floors.get(floor));
    }

    private void markDirty(){
        ElevatorBlockTile tile = this.getTile(this.getLowest());
        if(tile != null)
            tile.markDirty();
    }
}
