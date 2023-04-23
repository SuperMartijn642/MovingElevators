package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.packets.PacketAddElevatorGroup;
import com.supermartijn642.movingelevators.packets.PacketRemoveElevatorGroup;
import com.supermartijn642.movingelevators.packets.PacketUpdateElevatorGroups;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 11/7/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElevatorGroupCapability {

    @CapabilityInject(ElevatorGroupCapability.class)
    public static Capability<ElevatorGroupCapability> CAPABILITY;

    public static void register(){
        CapabilityManager.INSTANCE.register(ElevatorGroupCapability.class, new Capability.IStorage<ElevatorGroupCapability>() {
            public CompoundNBT writeNBT(Capability<ElevatorGroupCapability> capability, ElevatorGroupCapability instance, Direction side){
                return instance.write();
            }

            public void readNBT(Capability<ElevatorGroupCapability> capability, ElevatorGroupCapability instance, Direction side, INBT nbt){
                instance.read((CompoundNBT)nbt);
            }
        }, ElevatorGroupCapability::new);
    }

    public static ElevatorGroupCapability get(World level){
        return level.getCapability(CAPABILITY).orElse(null);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
        World level = e.getObject();

        LazyOptional<ElevatorGroupCapability> capability = LazyOptional.of(() -> new ElevatorGroupCapability(level));
        e.addCapability(new ResourceLocation("movingelevators", "elevator_groups"), new ICapabilitySerializable<INBT>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
                return cap == CAPABILITY ? capability.cast() : LazyOptional.empty();
            }

            @Override
            public INBT serializeNBT(){
                return CAPABILITY.writeNBT(capability.orElse(null), null);
            }

            @Override
            public void deserializeNBT(INBT nbt){
                CAPABILITY.readNBT(capability.orElse(null), null, nbt);
            }
        });
        e.addListener(capability::invalidate);
    }


    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END)
            return;

        tickWorldCapability(e.world);
    }

    public static void tickWorldCapability(World level){
        level.getCapability(CAPABILITY).ifPresent(ElevatorGroupCapability::tick);
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerChangedDimensionEvent e){
        ServerPlayerEntity player = (ServerPlayerEntity)e.getPlayer();
        player.level.getCapability(CAPABILITY).ifPresent(groups ->
            MovingElevators.CHANNEL.sendToPlayer(player, new PacketUpdateElevatorGroups(groups.write()))
        );
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        ServerPlayerEntity player = (ServerPlayerEntity)e.getPlayer();
        player.level.getCapability(CAPABILITY).ifPresent(groups ->
            MovingElevators.CHANNEL.sendToPlayer(player, new PacketUpdateElevatorGroups(groups.write()))
        );
    }

    private final World level;
    private final Map<ElevatorGroupPosition,ElevatorGroup> groups = new HashMap<>();

    public ElevatorGroupCapability(World level){
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

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();
        for(Map.Entry<ElevatorGroupPosition,ElevatorGroup> entry : this.groups.entrySet()){
            CompoundNBT groupTag = new CompoundNBT();
            groupTag.put("group", entry.getValue().write());
            groupTag.put("pos", entry.getKey().write());
            compound.put(entry.getKey().x + ";" + entry.getKey().z, groupTag);
        }
        return compound;
    }

    public void read(CompoundNBT compound){
        this.groups.clear();
        for(String key : compound.getAllKeys()){
            CompoundNBT groupTag = compound.getCompound(key);
            if(groupTag.contains("group") && groupTag.contains("pos")){
                ElevatorGroupPosition pos = ElevatorGroupPosition.read(groupTag.getCompound("pos"));
                ElevatorGroup group = new ElevatorGroup(this.level, pos.x, pos.z, pos.facing);
                group.read(groupTag.getCompound("group"));
                this.groups.put(pos, group);
            }
        }
    }

    private CompoundNBT writeGroup(ElevatorGroup group){
        CompoundNBT tag = new CompoundNBT();
        tag.put("group", group.write());
        tag.put("pos", new ElevatorGroupPosition(group.x, group.z, group.facing).write());
        return tag;
    }

    public void readGroup(CompoundNBT tag){
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

        public CompoundNBT write(){
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("x", this.x);
            tag.putInt("z", this.z);
            tag.putInt("facing", this.facing.get2DDataValue());
            return tag;
        }

        public static ElevatorGroupPosition read(CompoundNBT tag){
            return new ElevatorGroupPosition(tag.getInt("x"), tag.getInt("z"), Direction.from2DDataValue(tag.getInt("facing")));
        }
    }

}
