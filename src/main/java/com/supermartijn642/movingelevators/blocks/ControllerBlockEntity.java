package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ControllerBlockEntity extends ElevatorInputBlockEntity {

    private boolean initialized = false;
    private Direction facing;
    private String name;
    private DyeColor color = DyeColor.GRAY;
    private boolean showButtons = true;

    public ControllerBlockEntity(BlockPos pos, BlockState state){
        super(MovingElevators.elevator_tile, pos, state);
        this.facing = state.hasProperty(ControllerBlock.FACING) ? state.getValue(ControllerBlock.FACING) : null;
    }

    @Override
    public void update(){
        super.update();
        if(!this.initialized){
            ElevatorGroupCapability.get(this.level).add(this);
            this.getGroup().updateFloorData(this, this.name, this.color);
            this.initialized = true;
        }
    }

    @Override
    public Direction getFacing(){
        if(this.facing == null)
            this.facing = this.level.getBlockState(this.worldPosition).getValue(ControllerBlock.FACING);
        return this.facing;
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag compound = super.writeData();
        compound.putBoolean("hasName", this.name != null);
        if(this.name != null)
            compound.putString("name", this.name);
        compound.putInt("color", this.color.getId());
        compound.putBoolean("showButtons", this.showButtons);
        if(this.facing != null)
            compound.putInt("facing", this.facing.get2DDataValue());
        return compound;
    }

    @Override
    protected void readData(CompoundTag compound){
        super.readData(compound);
        this.name = compound.getBoolean("hasName") ? compound.getString("name") : null;
        this.color = DyeColor.byId(compound.getInt("color"));
        this.showButtons = !compound.contains("showButtons", Tag.TAG_BYTE) || compound.getBoolean("showButtons");
        this.facing = compound.contains("facing", Tag.TAG_INT) ? Direction.from2DDataValue(compound.getInt("facing")) : null;
    }

    public void onRemove(){
        if(!this.level.isClientSide)
            ElevatorGroupCapability.get(this.level).remove(this);
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

    public void setDisplayLabelColor(DyeColor color){
        this.color = color;
        this.dataChanged();
        if(this.hasGroup())
            this.getGroup().updateFloorData(this, this.name, this.color);
    }

    @Override
    public DyeColor getDisplayLabelColor(){
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
        return ElevatorGroupCapability.get(this.level).getGroup(this);
    }

    @Override
    public boolean hasGroup(){
        return this.initialized && ElevatorGroupCapability.get(this.level).getGroup(this) != null;
    }

    @Override
    public int getFloorLevel(){
        return this.worldPosition.getY();
    }
}
