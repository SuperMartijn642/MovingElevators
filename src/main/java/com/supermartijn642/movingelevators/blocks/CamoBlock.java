package com.supermartijn642.movingelevators.blocks;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.function.Supplier;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class CamoBlock extends BaseBlock implements EntityHoldingBlock {

    public static final IUnlistedProperty<IBlockState> CAMO_PROPERTY = new IUnlistedProperty<IBlockState>() {
        @Override
        public String getName(){
            return "camo_data";
        }

        @Override
        public boolean isValid(IBlockState value){
            return true;
        }

        @Override
        public Class<IBlockState> getType(){
            return IBlockState.class;
        }

        @Override
        public String valueToString(IBlockState value){
            return value.toString();
        }
    };

    private final Supplier<? extends CamoBlockEntity> entitySupplier;

    public CamoBlock(BlockProperties properties, Supplier<? extends CamoBlockEntity> entitySupplier){
        super(false, properties);
        this.entitySupplier = entitySupplier;
    }

    @Override
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        TileEntity blockEntity = level.getTileEntity(pos);
        if(blockEntity instanceof CamoBlockEntity)
            this.onRightClick(state, level, (CamoBlockEntity)blockEntity, pos, player, hand, hitSide, hitLocation);

        // Always return success to prevent accidentally placing blocks
        return InteractionFeedback.SUCCESS;
    }

    /**
     * @return whether the interaction has been handled
     */
    protected boolean onRightClick(IBlockState state, World level, CamoBlockEntity blockEntity, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        if(player.isSneaking() && player.getHeldItem(hand).isEmpty()){
            blockEntity.setCamoState(null);
            return true;
        }else if(!player.isSneaking() && blockEntity.canBeCamoStack(player.getHeldItem(hand))){
            ItemStack stack = player.getHeldItem(hand);
            Item item = stack.getItem();
            if(item instanceof ItemBlock){
                int metadata = item.getMetadata(stack.getMetadata());
                Block block = ((ItemBlock)item).getBlock();
                IBlockState state1 = block.getStateForPlacement(level, pos, hitSide, (float)hitLocation.x, (float)hitLocation.y, (float)hitLocation.z, metadata, player, hand);
                if(state1 == null)
                    state1 = block.getDefaultState();
                blockEntity.setCamoState(state1);
            }
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return this.entitySupplier.get();
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state){
        return EnumPushReaction.BLOCK;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess level, BlockPos pos, EntityLiving.SpawnPlacementType type){
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
    protected BlockStateContainer createBlockState(){
        return new ExtendedBlockState(this, this.getProperties(), new IUnlistedProperty[]{CAMO_PROPERTY});
    }

    protected IProperty<?>[] getProperties(){
        return new IProperty[0];
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess level, BlockPos pos){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof CamoBlockEntity){
            IBlockState camoState = ((CamoBlockEntity)entity).getCamoState();
            if(camoState != null)
                camoState = camoState.getBlock().getExtendedState(camoState, level, pos);
            return ((IExtendedBlockState)state).withProperty(CAMO_PROPERTY, camoState);
        }
        return state;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer){
        return true;
    }
}
