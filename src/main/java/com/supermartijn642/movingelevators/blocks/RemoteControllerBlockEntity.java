package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlockEntity extends ElevatorInputBlockEntity implements ITickable {

    private EnumFacing facing = EnumFacing.NORTH;
    private BlockPos controllerPos = BlockPos.ORIGIN;
    private int groupCheckCounter = 0;
    private ElevatorGroup lastGroup;

    public RemoteControllerBlockEntity(){
        super();
    }

    @Override
    public void update(){
        super.update();
        this.groupCheckCounter++;
        if(this.groupCheckCounter == 40){
            ElevatorGroup group = this.getGroup();
            if(group != this.lastGroup){
                this.world.updateComparatorOutputLevel(this.pos, this.getBlockState().getBlock());
                group.addComparatorListener(this.getFloorLevel(), this.pos);
                this.lastGroup = group;
            }
        }
    }

    public void setValues(EnumFacing facing, BlockPos controllerPos){
        this.facing = facing;
        this.controllerPos = controllerPos;
        this.dataChanged();
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = super.writeData();
        compound.setInteger("facing", this.facing.getIndex());
        compound.setInteger("controllerX", this.controllerPos.getX());
        compound.setInteger("controllerY", this.controllerPos.getY());
        compound.setInteger("controllerZ", this.controllerPos.getZ());
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        super.readData(compound);
        this.facing = EnumFacing.getFront(compound.getInteger("facing"));
        this.controllerPos = new BlockPos(compound.getInteger("controllerX"), compound.getInteger("controllerY"), compound.getInteger("controllerZ"));
    }

    @Override
    public EnumFacing getFacing(){
        return this.facing;
    }

    public ControllerBlockEntity getController(){
        if(this.world == null || this.controllerPos == null)
            return null;
        TileEntity tile = this.world.getTileEntity(this.controllerPos);
        return tile instanceof ControllerBlockEntity ? (ControllerBlockEntity)tile : null;
    }

    @Override
    public boolean hasGroup(){
        ControllerBlockEntity controller = this.getController();
        return controller != null && controller.hasGroup();
    }

    @Override
    public ElevatorGroup getGroup(){
        ControllerBlockEntity controller = this.getController();
        return controller == null ? null : controller.getGroup();
    }

    @Override
    public String getFloorName(){
        ControllerBlockEntity controller = this.getController();
        return controller == null ? null : controller.getFloorName();
    }

    @Override
    public EnumDyeColor getDisplayLabelColor(){
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

    public void onBreak(){
        ElevatorGroup group = this.getGroup();
        if(group != null)
            group.removeComparatorListener(this.pos);
    }
}
