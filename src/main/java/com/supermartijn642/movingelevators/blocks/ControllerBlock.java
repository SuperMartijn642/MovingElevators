package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 3/28/2020 by SuperMartijn642
 */
public class ControllerBlock extends ElevatorInputBlock {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public ControllerBlock(String registryName, Properties properties){
        super(registryName, properties, ControllerBlockEntity::new);
    }

    @Override
    protected boolean onRightClick(IBlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(player != null && player.getHeldItem(handIn).getItem() instanceof RemoteControllerBlockItem && blockEntity instanceof ControllerBlockEntity){
            if(!worldIn.isRemote){
                ItemStack stack = player.getHeldItem(handIn);
                NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
                tag.setInteger("controllerDim", worldIn.provider.getDimensionType().getId());
                tag.setInteger("controllerX", pos.getX());
                tag.setInteger("controllerY", pos.getY());
                tag.setInteger("controllerZ", pos.getZ());
                tag.setInteger("controllerFacing", ((ControllerBlockEntity)blockEntity).getFacing().getHorizontalIndex());
                stack.setTagCompound(tag);
                player.sendStatusMessage(TextComponents.translation("movingelevators.remote_controller.bind").get(), true);
            }
            return true;
        }

        if(super.onRightClick(state, worldIn, blockEntity, pos, player, handIn, facing, hitX, hitY, hitZ))
            return true;

        if(state.getValue(FACING) != facing){
            if(worldIn.isRemote)
                MovingElevatorsClient.openElevatorScreen(pos);
            return true;
        }
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    protected IProperty<?>[] getProperties(){
        return new IProperty[]{FACING};
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        EnumFacing facing = EnumFacing.getFront(meta);
        if(facing.getAxis() == EnumFacing.Axis.Y)
            facing = EnumFacing.NORTH;
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state){
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos){
        TileEntity entity = world.getTileEntity(pos);
        if(entity instanceof ControllerBlockEntity
            && ((ControllerBlockEntity)entity).hasGroup()
            && ((ControllerBlockEntity)entity).getGroup().isCageAvailableAt((ControllerBlockEntity)entity)){
            return 15;
        }
        return 0;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World reader, List<String> tooltips, ITooltipFlag advanced){
        super.addInformation(stack, reader, tooltips, advanced);
        tooltips.add(TextComponents.translation("movingelevators.elevator_controller.tooltip").color(TextFormatting.AQUA).format());
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        if(this.hasTileEntity(state)){
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof ControllerBlockEntity)
                ((ControllerBlockEntity)tile).onRemove();
        }
        super.breakBlock(worldIn, pos, state);
    }
}
