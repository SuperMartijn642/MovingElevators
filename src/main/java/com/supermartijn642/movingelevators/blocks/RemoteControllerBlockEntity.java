package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlockEntity extends ElevatorInputBlockEntity implements ITickable {

    private EnumFacing facing = EnumFacing.NORTH;
    private BlockPos controllerPos = BlockPos.ORIGIN;
    private EnumFacing controllerFacing = null;
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

    public void setValues(EnumFacing facing, BlockPos controllerPos, EnumFacing controllerFacing){
        this.facing = facing;
        this.controllerPos = controllerPos;
        this.controllerFacing = controllerFacing;
        this.dataChanged();
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = super.writeData();
        compound.setInteger("facing", this.facing.getIndex());
        compound.setInteger("controllerX", this.controllerPos.getX());
        compound.setInteger("controllerY", this.controllerPos.getY());
        compound.setInteger("controllerZ", this.controllerPos.getZ());
        if(this.controllerFacing != null)
            compound.setInteger("controllerPos", this.controllerFacing.getHorizontalIndex());
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        super.readData(compound);
        this.facing = EnumFacing.getFront(compound.getInteger("facing"));
        this.controllerPos = new BlockPos(compound.getInteger("controllerX"), compound.getInteger("controllerY"), compound.getInteger("controllerZ"));
        this.controllerFacing = compound.hasKey("controllerFacing", Constants.NBT.TAG_INT) ? EnumFacing.getHorizontal(compound.getInteger("controllerFacing")) : null;
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
        if(this.world == null || this.controllerPos == null || this.controllerFacing == null)
            return null;
        ElevatorGroupCapability capability = this.world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
        return capability == null ? null : capability.get(this.controllerPos.getX(), this.controllerPos.getZ(), this.controllerFacing);
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
