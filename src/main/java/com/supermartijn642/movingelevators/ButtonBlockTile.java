package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class ButtonBlockTile extends ElevatorInputTile {

    private Direction facing = Direction.NORTH;
    private Direction lastFacing = facing;
    private BlockPos controllerPos = BlockPos.ZERO;

    public ButtonBlockTile(BlockPos pos, BlockState state){
        super(MovingElevators.button_tile, pos, state);
    }

    public void setValues(Direction facing, BlockPos controllerPos){
        this.facing = facing;
        this.controllerPos = controllerPos;
        this.dataChanged();
    }

    @Override
    protected CompoundTag getChangedData(){
        CompoundTag data = super.getChangedData();
        if(this.lastFacing != this.facing){
            data.putInt("facing", this.facing.get3DDataValue());
            this.lastFacing = this.facing;
        }
        return data;
    }

    @Override
    protected CompoundTag getAllData(){
        CompoundTag data = super.getAllData();
        data.putInt("facing", this.facing.get3DDataValue());
        data.putInt("controllerX", this.controllerPos.getX());
        data.putInt("controllerY", this.controllerPos.getY());
        data.putInt("controllerZ", this.controllerPos.getZ());
        return data;
    }

    @Override
    protected void handleData(CompoundTag data){
        super.handleData(data);
        if(data.contains("facing"))
            this.facing = Direction.from3DDataValue(data.getInt("facing"));
        if(data.contains("controllerX"))
            this.controllerPos = new BlockPos(data.getInt("controllerX"), data.getInt("controllerY"), data.getInt("controllerZ"));
    }

    @Override
    public Direction getFacing(){
        return this.facing;
    }

    public ElevatorBlockTile getController(){
        if(this.level == null || this.controllerPos == null)
            return null;
        BlockEntity tile = this.level.getBlockEntity(this.controllerPos);
        return tile instanceof ElevatorBlockTile ? (ElevatorBlockTile)tile : null;
    }

    @Override
    public boolean hasGroup(){
        ElevatorBlockTile controller = this.getController();
        return controller != null && controller.hasGroup();
    }

    @Override
    public ElevatorGroup getGroup(){
        ElevatorBlockTile controller = this.getController();
        return controller == null ? null : controller.getGroup();
    }

    @Override
    public String getFloorName(){
        ElevatorBlockTile controller = this.getController();
        return controller == null ? null : controller.getFloorName();
    }

    @Override
    public DyeColor getDisplayLabelColor(){
        ElevatorBlockTile controller = this.getController();
        return controller == null ? null : controller.getDisplayLabelColor();
    }

    @Override
    public int getFloorLevel(){
        return this.controllerPos.getY();
    }
}
