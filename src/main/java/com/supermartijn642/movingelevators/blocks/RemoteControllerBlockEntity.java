package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.elevator.ElevatorCabinLevel;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

/**
 * Created 5/5/2020 by SuperMartijn642
 */
public class RemoteControllerBlockEntity extends ElevatorInputBlockEntity {

    private EnumFacing facing = EnumFacing.NORTH;
    private BlockPos controllerPos = BlockPos.ORIGIN;
    private EnumFacing controllerFacing = null;
    private boolean isInCabin = false;
    private int cabinFloorIndex = -1;
    private int groupCheckCounter = 2;
    private ElevatorGroup lastGroup;

    public RemoteControllerBlockEntity(){
        super(MovingElevators.button_tile);
    }

    @Override
    public void update(){
        super.update();
        this.groupCheckCounter--;
        if(this.groupCheckCounter <= 0){
            // Update missing data when a remote elevator panel was placed in an older version
            if(this.controllerFacing == null && this.controllerPos != null){
                ControllerBlockEntity controller = this.getController();
                if(controller != null)
                    this.controllerFacing = controller.getFacing();
            }

            // Update comparator output if the remote group changed
            ElevatorGroup group = this.getGroup();
            if(group != this.lastGroup){
                this.world.updateComparatorOutputLevel(this.pos, this.getBlockState().getBlock());
                if(group != null)
                    group.addComparatorListener(this.getFloorLevel(), this.pos);
                this.lastGroup = group;
            }

            this.calculateInCabin();
            this.groupCheckCounter = 40;
        }
    }

    public void setValues(EnumFacing facing, BlockPos controllerPos, EnumFacing controllerFacing){
        this.facing = facing;
        this.controllerPos = controllerPos;
        this.controllerFacing = controllerFacing;
        this.dataChanged();
    }

    private void calculateInCabin(){
        if(this.hasGroup()){
            ElevatorGroup group = this.getGroup();
            for(int floor = 0; floor < group.getFloorCount(); floor++){
                int y = group.getFloorYLevel(floor);
                BlockPos min = group.getCageAnchorBlockPos(y);
                if(this.pos.getX() >= min.getX() && this.pos.getX() < min.getX() + group.getCageSizeX()
                    && this.pos.getY() >= min.getY() && this.pos.getY() < min.getY() + group.getCageSizeY()
                    && this.pos.getZ() >= min.getZ() && this.pos.getZ() < min.getZ() + group.getCageSizeZ()){
                    this.isInCabin = true;
                    this.cabinFloorIndex = floor;
                    return;
                }
            }
        }
        this.isInCabin = false;
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = super.writeData();
        compound.setInteger("facing", this.facing.getIndex());
        compound.setInteger("controllerX", this.controllerPos.getX());
        compound.setInteger("controllerY", this.controllerPos.getY());
        compound.setInteger("controllerZ", this.controllerPos.getZ());
        if(this.controllerFacing != null)
            compound.setInteger("controllerFacing", this.controllerFacing.getHorizontalIndex());
        this.groupCheckCounter = 2;
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        super.readData(compound);
        this.facing = EnumFacing.getFront(compound.getInteger("facing"));
        this.controllerPos = new BlockPos(compound.getInteger("controllerX"), compound.getInteger("controllerY"), compound.getInteger("controllerZ"));
        this.controllerFacing = compound.hasKey("controllerFacing", Constants.NBT.TAG_INT) ? EnumFacing.getHorizontal(compound.getInteger("controllerFacing")) : null;
        this.isInCabin = false;
    }

    @Override
    public EnumFacing getFacing(){
        return this.facing;
    }

    public ControllerBlockEntity getController(){
        if(this.world == null || this.controllerPos == null)
            return null;
        TileEntity entity = this.world.getTileEntity(this.controllerPos);
        return entity instanceof ControllerBlockEntity ? (ControllerBlockEntity)entity : null;
    }

    @Override
    public boolean hasGroup(){
        return this.getGroup() != null;
    }

    @Override
    public ElevatorGroup getGroup(){
        if(this.world == null || this.controllerPos == null || this.controllerFacing == null)
            return null;
        ElevatorGroup group;
        if(this.world instanceof ElevatorCabinLevel)
            group = ((ElevatorCabinLevel)this.world).getElevatorGroup();
        else{
            ElevatorGroupCapability capability = ElevatorGroupCapability.get(this.world);
            group = capability == null ? null : capability.get(this.controllerPos.getX(), this.controllerPos.getZ(), this.controllerFacing);
        }
        return group != null && group.hasControllerAt(this.controllerPos.getY()) ? group : null;
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
        if(this.world instanceof ElevatorCabinLevel && this.hasGroup()){
            ElevatorGroup group = this.getGroup();
            return group.getFloorYLevel(group.getClosestFloorNumber(this.pos.getY()));
        }
        if(this.isInCabin && this.hasGroup()){
            ElevatorGroup group = this.getGroup();
            if(this.cabinFloorIndex >= 0 && this.cabinFloorIndex < group.getFloorCount())
                return group.getFloorYLevel(this.cabinFloorIndex);
        }
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
