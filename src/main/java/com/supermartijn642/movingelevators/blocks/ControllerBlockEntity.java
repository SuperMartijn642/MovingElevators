package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    protected void writeData(ValueOutput output){
        super.writeData(output);
        if(this.name != null)
            output.putString("name", this.name);
        output.putInt("color", this.color.getId());
        output.putBoolean("showButtons", this.showButtons);
        if(this.facing != null)
            output.putInt("facing", this.facing.get2DDataValue());
    }

    @Override
    protected void readData(ValueInput input){
        super.readData(input);
        this.name = input.getStringOr("name", null);
        this.color = input.getInt("color").map(DyeColor::byId).orElse(DyeColor.GRAY);
        this.showButtons = input.getBooleanOr("showButtons", true);
        this.facing = input.getInt("facing").map(Direction::from2DDataValue).orElse(null);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state){
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
