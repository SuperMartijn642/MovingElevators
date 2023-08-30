package com.supermartijn642.movingelevators.elevator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ITickList;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created 21/04/2023 by SuperMartijn642
 */
public class ElevatorCabinLevel extends World {

    private World level;
    private ClientElevatorCage cage;
    private ElevatorGroup group;
    private BlockPos minPos, maxPos;

    protected ElevatorCabinLevel(World clientLevel){
        super(null, clientLevel.dimension(), clientLevel.dimensionType(), null, true, false, 0);
        this.level = clientLevel;
    }

    public void setCabinAndPos(World clientLevel, ClientElevatorCage cage, ElevatorGroup group, BlockPos anchorPos){
        this.level = clientLevel;
        this.cage = cage;
        this.group = group;
        this.minPos = anchorPos;
        this.maxPos = anchorPos.offset(cage.xSize - 1, cage.ySize - 1, cage.zSize - 1);
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
    public TileEntity getBlockEntity(BlockPos pos){
        return this.isInBounds(pos) ? this.cage.blockEntities[pos.getX() - this.minPos.getX()][pos.getY() - this.minPos.getY()][pos.getZ() - this.minPos.getZ()] : null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos){
        if(this.isInBounds(pos)){
            BlockState state = this.cage.blockStates[pos.getX() - this.minPos.getX()][pos.getY() - this.minPos.getY()][pos.getZ() - this.minPos.getZ()];
            return state == null ? Blocks.AIR.defaultBlockState() : state;
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState state, BlockState newState, int flags){
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, double d, double e, double f, SoundEvent soundEvent, SoundCategory soundSource, float g, float h){
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, Entity entity, SoundEvent soundEvent, SoundCategory soundSource, float f, float g){
    }

    @Override
    public String gatherChunkSourceStats(){
        return "";
    }

    @Nullable
    @Override
    public Entity getEntity(int entityId){
        return null;
    }

    @Nullable
    @Override
    public MapData getMapData(String s){
        return this.level.getMapData(s);
    }

    @Override
    public void setMapData(MapData data){
    }

    @Override
    public int getFreeMapId(){
        return 0;
    }

    @Override
    public void destroyBlockProgress(int i, BlockPos pos, int j){
    }

    @Override
    public Scoreboard getScoreboard(){
        return this.level.getScoreboard();
    }

    @Override
    public RecipeManager getRecipeManager(){
        return this.level.getRecipeManager();
    }

    @Override
    public ITickList<Block> getBlockTicks(){
        return new ITickList<Block>() {
            @Override
            public boolean hasScheduledTick(BlockPos pos, Block block){
                return false;
            }

            @Override
            public void scheduleTick(BlockPos pos, Block block, int i, TickPriority tickPriority){
            }

            @Override
            public boolean willTickThisTick(BlockPos pos, Block block){
                return false;
            }
        };
    }

    @Override
    public ITickList<Fluid> getLiquidTicks(){
        return new ITickList<Fluid>() {
            @Override
            public boolean hasScheduledTick(BlockPos pos, Fluid fluid){
                return false;
            }

            @Override
            public void scheduleTick(BlockPos pos, Fluid fluid, int i, TickPriority tickPriority){
            }

            @Override
            public boolean willTickThisTick(BlockPos pos, Fluid fluid){
                return false;
            }
        };
    }

    @Override
    public AbstractChunkProvider getChunkSource(){
        return this.level.getChunkSource();
    }

    @Override
    public void levelEvent(@Nullable PlayerEntity player, int i, BlockPos pos, int j){
    }

    @Override
    public void levelEvent(int i, BlockPos pos, int j){
    }

    @Override
    public float getShade(Direction side, boolean bl){
        return this.level.getShade(side, bl);
    }

    @Override
    public List<? extends PlayerEntity> players(){
        return Collections.emptyList();
    }

    @Override
    public Biome getUncachedNoiseBiome(int i, int j, int k){
        return this.level.getUncachedNoiseBiome(i, j, k);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int i){
        return false;
    }

    @Override
    public long dayTime(){
        return this.level.dayTime();
    }

    @Override
    public long getDayTime(){
        return this.level.getDayTime();
    }

    @Override
    public long getGameTime(){
        return this.level.getGameTime();
    }

    @Override
    public float getTimeOfDay(float f){
        return this.level.getTimeOfDay(f);
    }

    @Override
    public DynamicRegistries registryAccess(){
        return this.level.registryAccess();
    }

    @Override
    public ITagCollectionSupplier getTagManager(){
        return this.level.getTagManager();
    }
}
