package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputBlock;
import com.supermartijn642.movingelevators.base.ElevatorInputTile;
import com.supermartijn642.movingelevators.base.MEBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class PresenceBlock extends MEBlock {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    private boolean isPowered;

    public PresenceBlock() {
        super("presence_block", PresenceBlockTile::new);
        this.isPowered = false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        if(worldIn == null || pos == null || placer == null || stack.isEmpty())
            return;
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof PresenceBlockTile){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.hasKey("controllerDim"))
                return;

            ((PresenceBlockTile)tile).setValues(placer.getHorizontalFacing().getOpposite(), new BlockPos(compound.getInteger("controllerX"), compound.getInteger("controllerY"), compound.getInteger("controllerZ")));
        }

    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            if (!worldIn.isUpdateScheduled(pos, this))
            {
                worldIn.scheduleUpdate(pos, this, 2);
            }
        }
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
    public boolean isFullCube(IBlockState state){
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced){
        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null && tag.hasKey("controllerDim"))
            tooltip.add(TextFormatting.AQUA + ClientProxy.translate("block.movingelevators.button_block.info").replace("$x$", Integer.toString(tag.getInteger("controllerX")))
                    .replace("$y$", Integer.toString(tag.getInteger("controllerY"))).replace("$z$", Integer.toString(tag.getInteger("controllerZ"))));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public void observedNeighborChange(IBlockState state, World world, BlockPos pos, Block changedBlock, BlockPos fromPos)
    {
        if (!world.isRemote && pos.offset((EnumFacing)state.getValue(FACING).getOpposite()).equals(fromPos))
        {
            if (!world.isUpdateScheduled(pos, this))
            {
                world.scheduleUpdate(pos, this, 2);
            }
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof PresenceBlockTile)
        {
            ElevatorBlockTile controller = ((PresenceBlockTile) tile).getController();
            if (controller != null)
            {
                this.isPowered = controller.hasPlatform();
            }
        }

        this.updateNeighborsOnTop(worldIn, pos, state);
    }

    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return this.isPowered && side == EnumFacing.DOWN ? 15 : 0;
    }

    void updateNeighborsOnTop(World worldIn, BlockPos pos, IBlockState state)
    {
        EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
        BlockPos blockpos = pos.up();
        worldIn.neighborChanged(blockpos, this, pos);
        worldIn.notifyNeighborsOfStateExcept(blockpos, this, enumfacing);
    }
}
