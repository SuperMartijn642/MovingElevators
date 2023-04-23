package com.supermartijn642.movingelevators.elevator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

/**
 * Created 21/04/2023 by SuperMartijn642
 */
public class ElevatorCabinLevel extends World {

    private World level;
    private ClientElevatorCage cage;
    private ElevatorGroup group;
    private BlockPos minPos, maxPos;

    protected ElevatorCabinLevel(World clientLevel){
        super(null, clientLevel.getWorldInfo(), clientLevel.provider, null, true);
        this.level = clientLevel;
        this.chunkProvider = clientLevel.getChunkProvider();
    }

    public void setCabinAndPos(World clientLevel, ClientElevatorCage cage, ElevatorGroup group, BlockPos anchorPos){
        this.level = clientLevel;
        this.worldInfo = clientLevel.getWorldInfo();
        this.chunkProvider = clientLevel.getChunkProvider();
        this.cage = cage;
        this.group = group;
        this.minPos = anchorPos;
        this.maxPos = anchorPos.add(cage.xSize - 1, cage.ySize - 1, cage.zSize - 1);
    }

    public ElevatorGroup getElevatorGroup(){
        return this.group;
    }

    private boolean isInBounds(BlockPos pos){
        return pos.getX() >= this.minPos.getX() && pos.getX() <= this.maxPos.getX()
            && pos.getY() >= this.minPos.getY() && pos.getY() <= this.maxPos.getY()
            && pos.getZ() >= this.minPos.getZ() && pos.getZ() <= this.maxPos.getZ();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos){
        return this.isInBounds(pos) ? this.cage.blockEntities[pos.getX() - this.minPos.getX()][pos.getY() - this.minPos.getY()][pos.getZ() - this.minPos.getZ()] : null;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos){
        if(this.isInBounds(pos)){
            IBlockState state = this.cage.blockStates[pos.getX() - this.minPos.getX()][pos.getY() - this.minPos.getY()][pos.getZ() - this.minPos.getZ()];
            return state == null ? Blocks.AIR.getDefaultState() : state;
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch){
    }

    @Nullable
    @Override
    public Entity getEntityByID(int entityId){
        return null;
    }

    @Nullable
    @Override
    public MapStorage getMapStorage(){
        return this.level.getMapStorage();
    }

    @Override
    public MapStorage getPerWorldStorage(){
        return super.getPerWorldStorage();
    }

    @Override
    public void setData(String dataID, WorldSavedData worldSavedDataIn){
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress){
    }

    @Override
    public Scoreboard getScoreboard(){
        return this.level.getScoreboard();
    }

    @Override
    public IChunkProvider getChunkProvider(){
        return this.level.getChunkProvider();
    }

    @Override
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam){
    }

    @Override
    public void playEvent(@Nullable EntityPlayer player, int i, BlockPos pos, int j){
    }

    @Override
    public void playEvent(int i, BlockPos pos, int j){
    }

    @Override
    public Biome getBiome(BlockPos pos){
        return this.level.getBiome(pos);
    }

    @Override
    protected IChunkProvider createChunkProvider(){
        return null;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty){
        return allowEmpty || !this.getChunkProvider().provideChunk(x, z).isEmpty();
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags){
        return false;
    }

    @Override
    public long getTotalWorldTime(){
        return this.level.getTotalWorldTime();
    }

    @Override
    public long getWorldTime(){
        return this.level.getWorldTime();
    }

    @Override
    public boolean isDaytime(){
        return this.level.isDaytime();
    }
}
