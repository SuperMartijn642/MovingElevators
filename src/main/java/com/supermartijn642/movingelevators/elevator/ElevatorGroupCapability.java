package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.extensions.MovingElevatorsLevel;
import com.supermartijn642.movingelevators.packets.PacketAddElevatorGroup;
import com.supermartijn642.movingelevators.packets.PacketRemoveElevatorGroup;
import com.supermartijn642.movingelevators.packets.PacketUpdateElevatorGroups;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created 11/7/2020 by SuperMartijn642
 */
public class ElevatorGroupCapability {

    public static ElevatorGroupCapability get(Level level){
        return ((MovingElevatorsLevel)level).getElevatorGroupCapability();
    }

    public static void registerEventListeners(){
        NeoForge.EVENT_BUS.addListener((Consumer<LevelTickEvent.Post>)event -> {
            if(!event.getLevel().isClientSide)
                tickWorldCapability(event.getLevel());
        });
        NeoForge.EVENT_BUS.addListener((Consumer<PlayerEvent.PlayerChangedDimensionEvent>)event -> onJoinWorld(event.getEntity(), event.getEntity().level()));
        NeoForge.EVENT_BUS.addListener((Consumer<PlayerEvent.PlayerLoggedInEvent>)event -> onJoin(event.getEntity()));
    }

    public static void tickWorldCapability(Level level){
        get(level).tick();
    }

    public static void onJoinWorld(Player player, Level level){
        MovingElevators.CHANNEL.sendToPlayer(player, new PacketUpdateElevatorGroups(get(level).write()));
    }

    public static void onJoin(Player player){
        MovingElevators.CHANNEL.sendToPlayer(player, new PacketUpdateElevatorGroups(get(player.level()).write()));
    }

    private final Level level;
    private final Map<ElevatorGroupPosition,ElevatorGroup> groups = new HashMap<>();

    public ElevatorGroupCapability(Level level){
        this.level = level;
    }

    public ElevatorGroupCapability(){
        this.level = null;
    }

    public ElevatorGroup get(int x, int z, Direction facing){
        return this.groups.get(new ElevatorGroupPosition(x, z, facing));
    }

    public void add(ControllerBlockEntity controller){
        ElevatorGroupPosition pos = new ElevatorGroupPosition(controller.getBlockPos(), controller.getFacing());
        this.groups.putIfAbsent(pos, new ElevatorGroup(this.level, pos.x, pos.z, pos.facing));
        this.groups.get(pos).add(controller);
    }

    public void remove(ControllerBlockEntity controller){
        ElevatorGroupPosition pos = new ElevatorGroupPosition(controller.getBlockPos(), controller.getFacing());
        ElevatorGroup group = this.groups.get(pos);
        group.remove(controller);
        if(group.getFloorCount() == 0){
            this.groups.remove(pos);
            MovingElevators.CHANNEL.sendToDimension(this.level.dimension(), new PacketRemoveElevatorGroup(group));
        }
    }

    public void tick(){
        for(ElevatorGroup group : this.groups.values())
            group.update();
    }

    public void updateGroup(ElevatorGroup group){
        if(!this.level.isClientSide && group != null)
            MovingElevators.CHANNEL.sendToDimension(this.level.dimension(), new PacketAddElevatorGroup(this.writeGroup(group)));
    }

    /**
     * This should only be called client-side from the {@link PacketRemoveElevatorGroup}
     */
    public void removeGroup(int x, int z, Direction facing){
        if(this.level.isClientSide)
            this.groups.remove(new ElevatorGroupPosition(x, z, facing));
    }

    public ElevatorGroup getGroup(ControllerBlockEntity entity){
        return this.groups.get(new ElevatorGroupPosition(entity.getBlockPos().getX(), entity.getBlockPos().getZ(), entity.getFacing()));
    }

    public Collection<ElevatorGroup> getGroups(){
        return this.groups.values();
    }

    public CompoundTag write(){
        CompoundTag compound = new CompoundTag();
        for(Map.Entry<ElevatorGroupPosition,ElevatorGroup> entry : this.groups.entrySet()){
            CompoundTag groupTag = new CompoundTag();
            groupTag.put("group", entry.getValue().write());
            groupTag.put("pos", entry.getKey().write());
            compound.put(entry.getKey().x + ";" + entry.getKey().z, groupTag);
        }
        return compound;
    }

    public void read(Tag tag){
        if(tag instanceof CompoundTag){
            CompoundTag compound = (CompoundTag)tag;
            this.groups.clear();
            for(String key : compound.getAllKeys()){
                CompoundTag groupTag = compound.getCompound(key);
                if(groupTag.contains("group") && groupTag.contains("pos")){
                    ElevatorGroupPosition pos = ElevatorGroupPosition.read(groupTag.getCompound("pos"));
                    ElevatorGroup group = new ElevatorGroup(this.level, pos.x, pos.z, pos.facing);
                    group.read(groupTag.getCompound("group"));
                    this.groups.put(pos, group);
                }
            }
        }
    }

    private CompoundTag writeGroup(ElevatorGroup group){
        CompoundTag tag = new CompoundTag();
        tag.put("group", group.write());
        tag.put("pos", new ElevatorGroupPosition(group.x, group.z, group.facing).write());
        return tag;
    }

    public void readGroup(CompoundTag tag){
        if(tag.contains("group") && tag.contains("pos")){
            ElevatorGroupPosition pos = ElevatorGroupPosition.read(tag.getCompound("pos"));
            ElevatorGroup group = new ElevatorGroup(this.level, pos.x, pos.z, pos.facing);
            group.read(tag.getCompound("group"));
            this.groups.put(pos, group);
        }
    }

    private static class ElevatorGroupPosition {

        public final int x, z;
        public final Direction facing;

        private ElevatorGroupPosition(int x, int z, Direction facing){
            this.x = x;
            this.z = z;
            this.facing = facing;
        }

        public ElevatorGroupPosition(BlockPos pos, Direction facing){
            this(pos.getX(), pos.getZ(), facing);
        }

        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            ElevatorGroupPosition that = (ElevatorGroupPosition)o;

            if(x != that.x) return false;
            if(z != that.z) return false;
            return facing == that.facing;
        }

        @Override
        public int hashCode(){
            int result = x;
            result = 31 * result + z;
            result = 31 * result + (facing != null ? facing.hashCode() : 0);
            return result;
        }

        public CompoundTag write(){
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", this.x);
            tag.putInt("z", this.z);
            tag.putInt("facing", this.facing.get2DDataValue());
            return tag;
        }

        public static ElevatorGroupPosition read(CompoundTag tag){
            return new ElevatorGroupPosition(tag.getInt("x"), tag.getInt("z"), Direction.from2DDataValue(tag.getInt("facing")));
        }
    }

}
