package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.packets.PacketAddElevatorGroup;
import com.supermartijn642.movingelevators.packets.PacketRemoveElevatorGroup;
import com.supermartijn642.movingelevators.packets.PacketUpdateElevatorGroups;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 11/7/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class ElevatorGroupCapability {

    @CapabilityInject(ElevatorGroupCapability.class)
    public static Capability<ElevatorGroupCapability> CAPABILITY;

    public static void register(){
        CapabilityManager.INSTANCE.register(ElevatorGroupCapability.class, new Capability.IStorage<ElevatorGroupCapability>() {
            public NBTTagCompound writeNBT(Capability<ElevatorGroupCapability> capability, ElevatorGroupCapability instance, EnumFacing side){
                return instance.write();
            }

            public void readNBT(Capability<ElevatorGroupCapability> capability, ElevatorGroupCapability instance, EnumFacing side, NBTBase nbt){
                instance.read((NBTTagCompound)nbt);
            }
        }, ElevatorGroupCapability::new);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
        World level = e.getObject();

        ElevatorGroupCapability capability = new ElevatorGroupCapability(level);
        e.addCapability(new ResourceLocation("movingelevators", "elevator_groups"), new ICapabilitySerializable<NBTBase>() {
            @Override
            public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side){
                return cap == CAPABILITY ? CAPABILITY.cast(capability) : null;
            }

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing){
                return capability == CAPABILITY;
            }

            @Override
            public NBTBase serializeNBT(){
                return CAPABILITY.writeNBT(capability, null);
            }

            @Override
            public void deserializeNBT(NBTBase nbt){
                CAPABILITY.readNBT(capability, null, nbt);
            }
        });
    }


    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END)
            return;

        tickWorldCapability(e.world);
    }

    public static void tickWorldCapability(World level){
        ElevatorGroupCapability capability = level.getCapability(CAPABILITY, null);
        if(capability != null)
            capability.tick();
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerChangedDimensionEvent e){
        EntityPlayerMP player = (EntityPlayerMP)e.player;
        ElevatorGroupCapability groups = player.world.getCapability(CAPABILITY, null);
        if(groups != null)
            MovingElevators.CHANNEL.sendToPlayer(player, new PacketUpdateElevatorGroups(groups.write()));
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        EntityPlayerMP player = (EntityPlayerMP)e.player;
        ElevatorGroupCapability groups = player.world.getCapability(CAPABILITY, null);
        if(groups != null)
            MovingElevators.CHANNEL.sendToPlayer(player, new PacketUpdateElevatorGroups(groups.write()));
    }

    private final World level;
    private final Map<ElevatorGroupPosition,ElevatorGroup> groups = new HashMap<>();

    public ElevatorGroupCapability(World level){
        this.level = level;
    }

    public ElevatorGroupCapability(){
        this.level = null;
    }

    public ElevatorGroup get(int x, int z, EnumFacing facing){
        return this.groups.get(new ElevatorGroupPosition(x, z, facing));
    }

    public void add(ControllerBlockEntity controller){
        ElevatorGroupPosition pos = new ElevatorGroupPosition(controller.getPos(), controller.getFacing());
        this.groups.putIfAbsent(pos, new ElevatorGroup(this.level, pos.x, pos.z, pos.facing));
        this.groups.get(pos).add(controller);
    }

    public void remove(ControllerBlockEntity controller){
        ElevatorGroupPosition pos = new ElevatorGroupPosition(controller.getPos(), controller.getFacing());
        ElevatorGroup group = this.groups.get(pos);
        group.remove(controller);
        if(group.getFloorCount() == 0){
            this.groups.remove(pos);
            MovingElevators.CHANNEL.sendToDimension(this.level, new PacketRemoveElevatorGroup(group));
        }
    }

    public void tick(){
        for(ElevatorGroup group : this.groups.values())
            group.update();
    }

    public void updateGroup(ElevatorGroup group){
        if(!this.level.isRemote && group != null)
            MovingElevators.CHANNEL.sendToDimension(this.level, new PacketAddElevatorGroup(this.writeGroup(group)));
    }

    /**
     * This should only be called client-side from the {@link PacketRemoveElevatorGroup}
     */
    public void removeGroup(int x, int z, EnumFacing facing){
        if(this.level.isRemote)
            this.groups.remove(new ElevatorGroupPosition(x, z, facing));
    }

    public ElevatorGroup getGroup(ControllerBlockEntity entity){
        return this.groups.get(new ElevatorGroupPosition(entity.getPos().getX(), entity.getPos().getZ(), entity.getFacing()));
    }

    public Collection<ElevatorGroup> getGroups(){
        return this.groups.values();
    }

    public NBTTagCompound write(){
        NBTTagCompound compound = new NBTTagCompound();
        for(Map.Entry<ElevatorGroupPosition,ElevatorGroup> entry : this.groups.entrySet()){
            NBTTagCompound groupTag = new NBTTagCompound();
            groupTag.setTag("group", entry.getValue().write());
            groupTag.setTag("pos", entry.getKey().write());
            compound.setTag(entry.getKey().x + ";" + entry.getKey().z, groupTag);
        }
        return compound;
    }

    public void read(NBTTagCompound compound){
        this.groups.clear();
        for(String key : compound.getKeySet()){
            NBTTagCompound groupTag = compound.getCompoundTag(key);
            if(groupTag.hasKey("group") && groupTag.hasKey("pos")){
                ElevatorGroupPosition pos = ElevatorGroupPosition.read(groupTag.getCompoundTag("pos"));
                ElevatorGroup group = new ElevatorGroup(this.level, pos.x, pos.z, pos.facing);
                group.read(groupTag.getCompoundTag("group"));
                this.groups.put(pos, group);
            }
        }
    }

    private NBTTagCompound writeGroup(ElevatorGroup group){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("group", group.write());
        tag.setTag("pos", new ElevatorGroupPosition(group.x, group.z, group.facing).write());
        return tag;
    }

    public void readGroup(NBTTagCompound tag){
        if(tag.hasKey("group") && tag.hasKey("pos")){
            ElevatorGroupPosition pos = ElevatorGroupPosition.read(tag.getCompoundTag("pos"));
            ElevatorGroup group = new ElevatorGroup(this.level, pos.x, pos.z, pos.facing);
            group.read(tag.getCompoundTag("group"));
            this.groups.put(pos, group);
        }
    }

    private static class ElevatorGroupPosition {

        public final int x, z;
        public final EnumFacing facing;

        private ElevatorGroupPosition(int x, int z, EnumFacing facing){
            this.x = x;
            this.z = z;
            this.facing = facing;
        }

        public ElevatorGroupPosition(BlockPos pos, EnumFacing facing){
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

        public NBTTagCompound write(){
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("x", this.x);
            tag.setInteger("z", this.z);
            tag.setInteger("facing", this.facing.getHorizontalIndex());
            return tag;
        }

        public static ElevatorGroupPosition read(NBTTagCompound tag){
            return new ElevatorGroupPosition(tag.getInteger("x"), tag.getInteger("z"), EnumFacing.getHorizontal(tag.getInteger("facing")));
        }
    }

}
