package com.supermartijn642.movingelevators;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlockTile extends TileEntity implements ITickable {

    private boolean isMoving = false;
    private int targetY;
    private double lastY;
    private double currentY;
    private int size = 3;
    private int nextSize = size;
    private double speed = 0.2;
    private IBlockState[][] platform = new IBlockState[this.size][this.size];
    private int controllerY = -1;
//    private ItemStack camoStack = new ItemStack(Items.IRON_BLOCK);

    public ElevatorBlockTile(){
    }

    @Override
    public void update(){
        if(this.world == null)
            return;
        if(this.isMoving){
            this.lastY = this.currentY;
            if(Math.abs(this.targetY - this.currentY) < speed){
                this.currentY = this.targetY;
                this.moveElevator(this.lastY, this.currentY);
                this.stopElevator();
            }else{
                this.currentY += Math.signum(this.targetY - this.currentY) * speed;
                this.moveElevator(this.lastY, this.currentY);
            }
        }else if(this.nextSize != this.size){
            this.size = this.nextSize;
            this.platform = new IBlockState[this.size][this.size];
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
        }
    }

    private void moveElevator(double oldY, double newY){
        int x = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
        int z = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;

        AxisAlignedBB box = new AxisAlignedBB(x, oldY - speed * 10, z, x + this.size, oldY + speed * 20, z + this.size);

        List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, box, entity -> entity instanceof EntityLivingBase);

        for(Entity entity : entities){
            if(newY < oldY && entity.hasNoGravity())
                continue;
            entity.setPosition(entity.posX, newY + 1, entity.posZ);
            entity.onGround = true;
            entity.fallDistance = 0;
            entity.motionY = 0;
        }
    }

    private void stopElevator(){
        this.isMoving = false;

        int startX = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;
        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                BlockPos pos = new BlockPos(startX + x, this.targetY, startZ + z);
                if(!this.world.isAirBlock(pos))
                    this.world.destroyBlock(pos, true);
                this.world.setBlockState(pos, this.platform[x][z]);
            }
        }

        if(!this.world.isRemote){
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
            double x = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) + 0.5;
            double z = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) + 0.5;
            this.world.playSound(null, x, this.targetY + 2.5, z, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.4f, 0.5f);
        }
    }

    private void startElevator(int currentY, int targetY){
        if(this.world == null || this.world.isRemote || this.isMoving)
            return;

        int startX = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;
        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                BlockPos pos = new BlockPos(startX + x, currentY - 1, startZ + z);
                if(this.world.isAirBlock(pos) || this.world.getTileEntity(pos) != null)
                    return;
                IBlockState state = this.world.getBlockState(pos);
                AxisAlignedBB collisionBox = state.getCollisionBoundingBox(this.world, pos);
                if(collisionBox == null || !(collisionBox.maxY == 1.0 &&
                    collisionBox.minX == 0 && collisionBox.maxX == 1.0 &&
                    collisionBox.minZ == 0 && collisionBox.maxZ == 1.0))
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
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
    }

    private boolean hasPlatform(int y){
        int startX = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;
        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                BlockPos pos = new BlockPos(startX + x, y - 1, startZ + z);
                if(this.world.isAirBlock(pos) || this.world.getTileEntity(pos) != null)
                    return false;
                IBlockState state = this.world.getBlockState(pos);
                AxisAlignedBB collisionBox = state.getCollisionBoundingBox(this.world, pos);
                if(collisionBox == null || !(collisionBox.maxY == 1.0 &&
                    collisionBox.minX == 0 && collisionBox.maxX == 1.0 &&
                    collisionBox.minZ == 0 && collisionBox.maxZ == 1.0))
                    return false;
                this.platform[x][z] = state;
            }
        }
        return true;
    }

    private boolean hasSpaceForPlatform(int y){
        int startX = this.pos.getX() + this.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = this.pos.getZ() + this.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;
        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                BlockPos pos = new BlockPos(startX + x, y - 1, startZ + z);
                if(!this.world.isAirBlock(pos))
                    return false;
            }
        }
        return true;
    }

    private int findController(int excludeY, EnumFacing facing){
        BlockPos pos = new BlockPos(this.pos.getX(), 0, this.pos.getZ());
        int minY = -1;
        for(int y = 0; y <= this.world.getHeight(); y++){
            if(y == excludeY)
                continue;
            TileEntity tile = this.world.getTileEntity(pos.add(0, y, 0));
            if(tile instanceof ElevatorBlockTile){
                ElevatorBlockTile elevator = (ElevatorBlockTile)tile;
                if(elevator.getFacing() == (facing == null ? this.getFacing() : facing)){
                    if(minY == -1)
                        minY = y;
                    elevator.controllerY = minY;
                }
            }
        }
        return minY;
    }

    public void onButtonPress(boolean isUp, boolean isDown, int yLevel){
        if(this.world == null || this.isMoving)
            return;
        if(this.controllerY != this.pos.getY()){
            this.getController().onButtonPress(isUp, isDown, yLevel);
            return;
        }

        if(isUp){
            if(this.hasPlatform(yLevel)){
                for(int y = yLevel + 1; y <= this.world.getHeight(); y++){
                    TileEntity tile = this.world.getTileEntity(new BlockPos(this.pos.getX(), y, this.pos.getZ()));
                    if(tile instanceof ElevatorBlockTile && ((ElevatorBlockTile)tile).getFacing() == this.getFacing()){
                        if(this.hasSpaceForPlatform(y))
                            this.startElevator(yLevel, y);
                        return;
                    }
                }
            }
        }else if(isDown){
            if(this.hasPlatform(yLevel)){
                for(int y = yLevel - 1; y >= 0; y--){
                    TileEntity tile = this.world.getTileEntity(new BlockPos(this.pos.getX(), y, this.pos.getZ()));
                    if(tile instanceof ElevatorBlockTile && ((ElevatorBlockTile)tile).getFacing() == this.getFacing()){
                        if(this.hasSpaceForPlatform(y))
                            this.startElevator(yLevel, y);
                        return;
                    }
                }
            }
        }else{
            if(this.hasSpaceForPlatform(yLevel)){
                for(int offset = 1; yLevel - offset >= 0 || yLevel + offset <= this.world.getHeight(); offset++){
                    TileEntity tile = this.world.getTileEntity(new BlockPos(this.pos.getX(), yLevel - offset, this.pos.getZ()));
                    if(tile instanceof ElevatorBlockTile && ((ElevatorBlockTile)tile).getFacing() == this.getFacing() && this.hasPlatform(yLevel - offset)){
                        this.startElevator(yLevel - offset, yLevel);
                        return;
                    }
                    tile = this.world.getTileEntity(new BlockPos(this.pos.getX(), yLevel + offset, this.pos.getZ()));
                    if(tile instanceof ElevatorBlockTile && ((ElevatorBlockTile)tile).getFacing() == this.getFacing() && this.hasPlatform(yLevel + offset)){
                        this.startElevator(yLevel + offset, yLevel);
                        return;
                    }
                }
            }
        }
    }

    private ElevatorBlockTile getController(){
        TileEntity tile = this.world.getTileEntity(new BlockPos(this.pos.getX(), this.controllerY, this.pos.getZ()));
        if(!(tile instanceof ElevatorBlockTile)){
            this.findController(-1, null);
            tile = this.world.getTileEntity(new BlockPos(this.pos.getX(), this.controllerY, this.pos.getZ()));
        }
        return tile instanceof ElevatorBlockTile ? (ElevatorBlockTile)tile : this;
    }

    public EnumFacing getFacing(){
        if(this.world == null)
            return EnumFacing.NORTH;
        IBlockState state = this.getBlockState();
        return state.getBlock() == MovingElevators.elevator_block ? state.getValue(ElevatorBlock.FACING) : EnumFacing.NORTH;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        return new SPacketUpdateTileEntity(this.pos, 0, this.getDataTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.handleDataTag(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("elevator", this.getDataTag());
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag){
        super.handleUpdateTag(tag);
        this.handleDataTag(tag.getCompoundTag("elevator"));
    }

    private NBTTagCompound getDataTag(){
        NBTTagCompound data = new NBTTagCompound();
        data.setBoolean("moving", this.isMoving);
        data.setInteger("size", this.size);
        if(this.isMoving){
            data.setInteger("targetY", this.targetY);
            data.setDouble("lastY", this.lastY);
            data.setDouble("currentY", this.currentY);
            for(int x = 0; x < this.size; x++){
                for(int z = 0; z < this.size; z++){
                    data.setInteger("platform" + x + "," + z, Block.getStateId(this.platform[x][z]));
                }
            }
        }
        data.setDouble("speed", this.speed);
//        data.put("camo",this.camoStack.write(new NBTTagCompound()));
        return data;
    }

    private void handleDataTag(NBTTagCompound tag){
        if(tag.hasKey("moving"))
            this.isMoving = tag.getBoolean("moving");
        if(tag.hasKey("targetY"))
            this.targetY = tag.getInteger("targetY");
        if(tag.hasKey("lastY"))
            this.lastY = tag.getDouble("lastY");
        if(tag.hasKey("currentY"))
            this.currentY = tag.getDouble("currentY");
        if(tag.hasKey("size")){
            this.size = tag.getInteger("size");
            this.nextSize = this.size;
            this.platform = new IBlockState[this.size][this.size];
        }
        if(tag.hasKey("speed"))
            this.speed = tag.getDouble("speed");
        for(int x = 0; x < this.size; x++){
            for(int z = 0; z < this.size; z++){
                this.platform[x][z] = Block.getStateById(tag.getInteger("platform" + x + "," + z));
            }
        }
//        if(tag.contains("camo"))
//            this.camoStack = ItemStack.read(tag.getCompound("camo"));
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

    public IBlockState[][] getPlatform(){
        return this.platform;
    }

    public int getSize(){
        return this.getController().size;
    }

    public void setSize(int size){
        this.getController().nextSize = size;
    }

    public double getSpeed(){
        return this.getController().speed;
    }

    public void setSpeed(double speed){
        this.getController().speed = speed;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    public void onBreak(EnumFacing facing){
        if(!this.isMoving)
            return;
        this.controllerY = this.findController(this.pos.getY(), facing);
        ElevatorBlockTile tile = this.getController();
        if(tile == this){
            int x = this.pos.getX() + facing.getFrontOffsetX() * (int)Math.ceil(size / 2f);
            int z = this.pos.getZ() + facing.getFrontOffsetZ() * (int)Math.ceil(size / 2f);
            BlockPos pos = new BlockPos(x, this.currentY, z);
            for(IBlockState[] arr : this.platform){
                for(IBlockState state : arr){
                    this.world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(state.getBlock())));
                }
            }
        }else{
            tile.isMoving = true;
            tile.targetY = this.targetY;
            tile.lastY = this.lastY;
            tile.currentY = this.currentY;
            tile.size = this.size;
            tile.speed = this.speed;
            tile.platform = this.platform;
            this.world.notifyBlockUpdate(tile.pos, tile.getBlockState(), tile.getBlockState(), 2);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        compound.setTag("elevator", this.getDataTag());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        this.handleDataTag(compound.getCompoundTag("elevator"));
    }

//    public Block getCamoBlock(){
//        if(this.camoStack == null || this.camoStack.isEmpty() || !(this.camoStack.getItem() instanceof BlockItem))
//            return null;
//        return ((BlockItem)this.camoStack.getItem()).getBlock();
//    }


    @Override
    public double getMaxRenderDistanceSquared(){
        return this.world.getHeight() * this.world.getHeight() * 4;
    }

    private IBlockState getBlockState(){
        return this.world.getBlockState(this.pos);
    }

}
