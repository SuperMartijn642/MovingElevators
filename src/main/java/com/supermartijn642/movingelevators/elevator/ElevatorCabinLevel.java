package com.supermartijn642.movingelevators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created 21/04/2023 by SuperMartijn642
 */
public class ElevatorCabinLevel extends Level {

    private Level level;
    private ClientElevatorCage cage;
    private ElevatorGroup group;
    private BlockPos minPos, maxPos;

    protected ElevatorCabinLevel(Level clientLevel){
        super(null, clientLevel.dimension(), clientLevel.dimensionTypeRegistration(), null, true, false, 0);
        this.level = clientLevel;
    }

    public void setCabinAndPos(Level clientLevel, ClientElevatorCage cage, ElevatorGroup group, BlockPos anchorPos){
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
    public BlockEntity getBlockEntity(BlockPos pos){
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
    public void playSound(@Nullable Player player, double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h){

    }

    @Override
    public void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float g){

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
    public MapItemSavedData getMapData(String s){
        return this.level.getMapData(s);
    }

    @Override
    public void setMapData(String s, MapItemSavedData data){
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
    protected LevelEntityGetter<Entity> getEntities(){
        return new LevelEntityGetter<>() {
            @Nullable
            @Override
            public Entity get(int entityId){
                return null;
            }

            @Nullable
            @Override
            public Entity get(UUID id){
                return null;
            }

            @Override
            public Iterable<Entity> getAll(){
                return Collections.emptyList();
            }

            @Override
            public <U extends Entity> void get(EntityTypeTest<Entity,U> entityTypeTest, Consumer<U> consumer){

            }

            @Override
            public void get(AABB aabb, Consumer<Entity> consumer){
            }

            @Override
            public <U extends Entity> void get(EntityTypeTest<Entity,U> entityTypeTest, AABB aabb, Consumer<U> consumer){

            }
        };
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks(){
        return new LevelTickAccess<Block>() {
            @Override
            public boolean willTickThisTick(BlockPos pos, Block block){
                return false;
            }

            @Override
            public void schedule(ScheduledTick<Block> scheduledTick){
            }

            @Override
            public boolean hasScheduledTick(BlockPos pos, Block block){
                return false;
            }

            @Override
            public int count(){
                return 0;
            }
        };
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks(){
        return new LevelTickAccess<>() {
            @Override
            public boolean willTickThisTick(BlockPos pos, Fluid fluid){
                return false;
            }

            @Override
            public void schedule(ScheduledTick<Fluid> scheduledTick){
            }

            @Override
            public boolean hasScheduledTick(BlockPos pos, Fluid fluid){
                return false;
            }

            @Override
            public int count(){
                return 0;
            }
        };
    }

    @Override
    public ChunkSource getChunkSource(){
        return this.level.getChunkSource();
    }

    @Override
    public void levelEvent(@Nullable Player player, int i, BlockPos pos, int j){
    }

    @Override
    public void gameEvent(@Nullable Entity entity, GameEvent gameEvent, BlockPos pos){
    }

    @Override
    public float getShade(Direction side, boolean bl){
        return this.level.getShade(side, bl);
    }

    @Override
    public List<? extends Player> players(){
        return Collections.emptyList();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k){
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
    public RegistryAccess registryAccess(){
        return this.level.registryAccess();
    }
}
