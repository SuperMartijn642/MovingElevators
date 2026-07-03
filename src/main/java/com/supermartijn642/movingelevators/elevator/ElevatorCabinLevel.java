package com.supermartijn642.movingelevators.elevator;

import net.minecraft.client.ClientClockManager;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created 21/04/2023 by SuperMartijn642
 */
public class ElevatorCabinLevel extends ClientLevel {

    private ClientLevel level;
    private ClientElevatorCage cage;
    private ElevatorGroup group;
    private BlockPos minPos, maxPos;

    protected ElevatorCabinLevel(ClientLevel clientLevel){
        super(clientLevel.connection, null, clientLevel.dimension(), clientLevel.dimensionTypeRegistration(), 1, 1, null, false, 0, 0);
        this.level = clientLevel;
    }

    public void setCabinAndPos(ClientLevel clientLevel, ClientElevatorCage cage, ElevatorGroup group, BlockPos anchorPos){
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
    public LevelLightEngine getLightEngine(){
        return this.level.getLightEngine();
    }

    @Override
    public void setRespawnData(LevelData.RespawnData respawnData){
    }

    @Override
    public LevelData.RespawnData getRespawnData(){
        return this.level.getRespawnData();
    }

    @Override
    public void playSeededSound(@Nullable Entity entity, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float pitch, float volume, long l){

    }

    @Override
    public void playSeededSound(@Nullable Entity entity, Entity entity2, Holder<SoundEvent> sound, SoundSource source, float pitch, float volume, long l){

    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState state, BlockState newState, int flags){
    }

    @Override
    public void explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double e, double f, float g, boolean bl, ExplosionInteraction explosionInteraction, ParticleOptions particleOptions, ParticleOptions particleOptions2, WeightedList<ExplosionParticleInfo> weightedList, Holder<SoundEvent> holder){
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

    @Override
    public List<PartEntity<?>> dragonParts(){
        return List.of();
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(MapId id){
        return this.level.getMapData(id);
    }

    @Override
    public void destroyBlockProgress(int i, BlockPos pos, int j){
    }

    @Override
    public Scoreboard getScoreboard(){
        return this.level.getScoreboard();
    }

    @Override
    public RecipeAccess recipeAccess(){
        return this.level.recipeAccess();
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
            public <U extends Entity> void get(EntityTypeTest<Entity,U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer){
            }

            @Override
            public void get(AABB aabb, Consumer<Entity> consumer){
            }

            @Override
            public <U extends Entity> void get(EntityTypeTest<Entity,U> entityTypeTest, AABB aabb, AbortableIterationConsumer<U> abortableIterationConsumer){
            }
        };
    }

    @Override
    public ClientClockManager clockManager(){
        return this.level.clockManager();
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes(){
        return this.level.environmentAttributes();
    }

    @Override
    protected EnvironmentAttributeSystem.Builder addEnvironmentAttributeLayers(EnvironmentAttributeSystem.Builder builder){
        return builder;
    }

    @Override
    public void updateSkyBrightness(){
    }

    @Override
    public PotionBrewing potionBrewing(){
        return null;
    }

    @Override
    public FuelValues fuelValues(){
        return this.level.fuelValues();
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
    public ClientChunkCache getChunkSource(){
        return this.level.getChunkSource();
    }

    @Override
    public void levelEvent(@Nullable Entity entity, int i, BlockPos blockPos, int j){
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 vec3, GameEvent.Context context){
    }

    @Override
    public List<AbstractClientPlayer> players(){
        return Collections.emptyList();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k){
        return this.level.getUncachedNoiseBiome(i, j, k);
    }

    @Override
    public int getSeaLevel(){
        return this.level.getSeaLevel();
    }

    @Override
    public FeatureFlagSet enabledFeatures(){
        return this.level.enabledFeatures();
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int i){
        return false;
    }

    @Override
    public TickRateManager tickRateManager(){
        return this.level.tickRateManager();
    }

    @Override
    public long getGameTime(){
        return this.level.getGameTime();
    }

    @Override
    public WorldBorder getWorldBorder(){
        return this.level.getWorldBorder();
    }
}
