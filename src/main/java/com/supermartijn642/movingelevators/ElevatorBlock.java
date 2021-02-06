package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputBlock;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlock extends ElevatorInputBlock {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public ElevatorBlock(){
        super("elevator_block", ElevatorBlockTile::new);
    }

    @Override
    protected void onRightClick(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(player != null && ((player.getHeldItem(handIn).getItem() instanceof ButtonBlockItem) || (player.getHeldItem(handIn).getItem() instanceof PresenceBlockItem) || (player.getHeldItem(handIn).getItem() instanceof CallButtonBlockItem))){
            if(!worldIn.isRemote){
                ItemStack stack = player.getHeldItem(handIn);
                if(stack.getTagCompound() == null)
                    stack.setTagCompound(new NBTTagCompound());
                NBTTagCompound tag = stack.getTagCompound();
                tag.setInteger("controllerDim", worldIn.provider.getDimensionType().getId());
                tag.setInteger("controllerX", pos.getX());
                tag.setInteger("controllerY", pos.getY());
                tag.setInteger("controllerZ", pos.getZ());
                player.sendMessage(new TextComponentTranslation("block.movingelevators.button_block.bind").setStyle(new Style().setColor(TextFormatting.YELLOW)));
            }
        }else if(state.getValue(FACING) != facing){
            if(worldIn.isRemote)
                ClientProxy.openElevatorScreen(pos);
        }else
            super.onRightClick(worldIn, pos, state, player, handIn, facing, hitX, hitY, hitZ);
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
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof ElevatorBlockTile)
            ((ElevatorBlockTile)tile).onBreak();
        super.breakBlock(worldIn, pos, state);
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
    public int getComparatorInputOverride(IBlockState state, World worldIn, BlockPos pos){
        EnumFacing facing = state.getValue(FACING);
        if(facing == null)
            return 0;
        return worldIn.isAirBlock(pos.offset(facing).down()) ? 0 : 15;
    }
}
