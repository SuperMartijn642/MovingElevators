package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class CamoBlock extends BaseBlock {

    private final Supplier<? extends CamoBlockEntity> tileSupplier;

    public CamoBlock(String registryName, Properties properties, Supplier<? extends CamoBlockEntity> tileSupplier){
        super(registryName, false, properties);
        this.tileSupplier = tileSupplier;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        TileEntity blockEntity = worldIn.getTileEntity(pos);
        if(blockEntity instanceof CamoBlockEntity)
            this.onRightClick(state, worldIn, (CamoBlockEntity)blockEntity, pos, player, handIn, facing, hitX, hitY, hitZ);

        // Always return success to prevent accidentally placing blocks
        return true;
    }

    /**
     * @return whether the interaction has been handled
     */
    protected boolean onRightClick(IBlockState state, World worldIn, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(player.isSneaking() && player.getHeldItem(handIn).isEmpty()){
            blockEntity.setCamoState(null);
            return true;
        }else if(!player.isSneaking() && blockEntity.canBeCamoStack(player.getHeldItem(handIn))){
            Item item = player.getHeldItem(handIn).getItem();
            if(item instanceof ItemBlock){
                Block block = ((ItemBlock)item).getBlock();
                IBlockState state1 = block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, player, handIn);
                if(state1 == null)
                    state1 = block.getDefaultState();
                blockEntity.setCamoState(state1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return this.tileSupplier.get();
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state){
        return EnumPushReaction.BLOCK;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type){
        return false;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess reader, BlockPos pos){
        TileEntity blockEntity = reader.getTileEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getLightValue(reader, pos) : super.getLightValue(state, reader, pos);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess reader, BlockPos pos, EnumFacing face){
        TileEntity blockEntity = reader.getTileEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().shouldSideBeRendered(reader, pos, face) : super.shouldSideBeRendered(state, reader, pos, face);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess reader, BlockPos pos, EnumFacing face){
        TileEntity blockEntity = reader.getTileEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().doesSideBlockRendering(reader, pos, face) : super.doesSideBlockRendering(state, reader, pos, face);
    }

    public boolean isFullCube(IBlockState state){
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess reader, BlockPos pos){
        TileEntity blockEntity = reader.getTileEntity(pos);
        return blockEntity instanceof CamoBlockEntity && ((CamoBlockEntity)blockEntity).hasCamoState() ? ((CamoBlockEntity)blockEntity).getCamoState().getLightOpacity(reader, pos) : super.getLightOpacity(state, reader, pos);
    }

    @Override
    public BlockRenderLayer getBlockLayer(){
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new ExtendedBlockState(this, this.getProperties(), new IUnlistedProperty[]{CamoBakedModel.CAMO_PROPERTY});
    }

    protected IProperty<?>[] getProperties(){
        return new IProperty[0];
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos){
        TileEntity entity = world.getTileEntity(pos);
        if(entity instanceof CamoBlockEntity)
            return ((IExtendedBlockState)state).withProperty(CamoBakedModel.CAMO_PROPERTY, ((CamoBlockEntity)entity).getCamoState());
        return state;
    }
}
