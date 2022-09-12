package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlockEntity extends ElevatorInputBlockEntity {

    private Direction facing = Direction.NORTH;
    private BlockPos controllerPos = BlockPos.ZERO;
    private Direction controllerFacing = null;
    private int groupCheckCounter = 0;
    private ElevatorGroup lastGroup;

    public RemoteControllerBlockEntity(BlockPos pos, BlockState state){
        super(MovingElevators.button_tile, pos, state);
    }

    @Override
    public void update(){
        super.update();
        this.groupCheckCounter++;
        if(this.groupCheckCounter == 40){
            ElevatorGroup group = this.getGroup();
            if(group != this.lastGroup){
                this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
                group.addComparatorListener(this.getFloorLevel(), this.worldPosition);
                this.lastGroup = group;
            }
        }
    }

    public void setValues(Direction facing, BlockPos controllerPos, Direction controllerFacing){
        this.facing = facing;
        this.controllerPos = controllerPos;
        this.controllerFacing = controllerFacing;
        this.dataChanged();
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag compound = super.writeData();
        compound.putInt("facing", this.facing.get3DDataValue());
        compound.putInt("controllerX", this.controllerPos.getX());
        compound.putInt("controllerY", this.controllerPos.getY());
        compound.putInt("controllerZ", this.controllerPos.getZ());
        if(this.controllerFacing != null)
            compound.putInt("controllerPos", this.controllerFacing.get2DDataValue());
        return compound;
    }

    @Override
    protected void readData(CompoundTag compound){
        super.readData(compound);
        this.facing = Direction.from3DDataValue(compound.getInt("facing"));
        this.controllerPos = new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ"));
        this.controllerFacing = compound.contains("controllerFacing", Tag.TAG_INT) ? Direction.from2DDataValue(compound.getInt("controllerFacing")) : null;
    }

    @Override
    public Direction getFacing(){
        return this.facing;
    }

    public ControllerBlockEntity getController(){
        if(this.level == null || this.controllerPos == null)
            return null;
        BlockEntity entity = this.level.getBlockEntity(this.controllerPos);
        return entity instanceof ControllerBlockEntity ? (ControllerBlockEntity)entity : null;
    }

    @Override
    public boolean hasGroup(){
        return this.getGroup() != null;
    }

    @Override
    public ElevatorGroup getGroup(){
        if(this.controllerFacing == null && this.controllerPos != null){
            ControllerBlockEntity controller = this.getController();
            if(controller != null){
                this.controllerFacing = controller.getFacing();
                return controller.getGroup();
            }
        }
        if(this.level == null || this.controllerPos == null || this.controllerFacing == null)
            return null;
        ElevatorGroupCapability capability = this.level.getCapability(ElevatorGroupCapability.CAPABILITY).orElse(null);
        return capability == null ? null : capability.get(this.controllerPos.getX(), this.controllerPos.getZ(), this.controllerFacing);
    }

    @Override
    public String getFloorName(){
        ControllerBlockEntity controller = this.getController();
        return controller == null ? null : controller.getFloorName();
    }

    @Override
    public DyeColor getDisplayLabelColor(){
        ControllerBlockEntity controller = this.getController();
        return controller == null ? null : controller.getDisplayLabelColor();
    }

    @Override
    public int getFloorLevel(){
        return this.controllerPos.getY();
    }

    public BlockPos getControllerPos(){
        return this.controllerPos;
    }

    @Override
    public void setRemoved(){
        ElevatorGroup group = this.getGroup();
        if(group != null)
            group.removeComparatorListener(this.worldPosition);
        super.setRemoved();
    }
}
