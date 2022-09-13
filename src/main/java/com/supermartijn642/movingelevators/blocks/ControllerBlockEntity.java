package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ControllerBlockEntity extends ElevatorInputBlockEntity {

    private boolean initialized = false;
    private EnumFacing facing;
    private String name;
    private EnumDyeColor color = EnumDyeColor.GRAY;
    private boolean showButtons = true;

    public ControllerBlockEntity(){
        super(MovingElevators.elevator_tile);
    }

    @Override
    public void update(){
        super.update();
        if(!this.initialized){
            ElevatorGroupCapability groupCapability = this.world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
            if(groupCapability != null)
                groupCapability.add(this);
            this.getGroup().updateFloorData(this, this.name, this.color);
            this.initialized = true;
        }
    }

    @Override
    public EnumFacing getFacing(){
        if(this.facing == null)
            this.facing = this.world.getBlockState(this.pos).getValue(ControllerBlock.FACING);
        return this.facing;
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = super.writeData();
        compound.setBoolean("hasName", this.name != null);
        if(this.name != null)
            compound.setString("name", this.name);
        compound.setInteger("color", this.color.getMetadata());
        compound.setBoolean("showButtons", this.showButtons);
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        super.readData(compound);
        if(compound.hasKey("hasName", Constants.NBT.TAG_BYTE))
            this.name = compound.getBoolean("hasName") ? compound.getString("name") : null;
        else if(compound.hasKey("name", Constants.NBT.TAG_STRING)){ // For older versions
            this.name = compound.getString("name");
        }else
            this.name = null;
        this.color = EnumDyeColor.byMetadata(compound.getInteger("color"));
        this.showButtons = !compound.hasKey("showButtons", Constants.NBT.TAG_BYTE) || compound.getBoolean("showButtons");
    }

    public void onRemove(){
        if(!this.world.isRemote){
            ElevatorGroupCapability groups = this.world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
            if(groups != null)
                groups.remove(this);
        }
    }

    @Override
    public String getFloorName(){
        return this.name;
    }

    public void setFloorName(String name){
        this.name = name;
        this.dataChanged();
        if(this.hasGroup())
            this.getGroup().updateFloorData(this, this.name, this.color);
    }

    public void setDisplayLabelColor(EnumDyeColor color){
        this.color = color;
        this.dataChanged();
        if(this.hasGroup())
            this.getGroup().updateFloorData(this, this.name, this.color);
    }

    @Override
    public EnumDyeColor getDisplayLabelColor(){
        return this.color;
    }

    public boolean shouldShowButtons(){
        return this.showButtons;
    }

    public void toggleShowButtons(){
        this.showButtons = !this.showButtons;
        this.dataChanged();
    }

    @Override
    public ElevatorGroup getGroup(){
        ElevatorGroupCapability groups = this.world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
        return groups == null ? null : groups.getGroup(this);
    }

    @Override
    public boolean hasGroup(){
        if(!this.initialized)
            return false;
        ElevatorGroupCapability groups = this.world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
        return groups != null && groups.getGroup(this) != null;
    }

    @Override
    public int getFloorLevel(){
        return this.pos.getY();
    }
}
