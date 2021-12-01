package com.supermartijn642.movingelevators.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public class MEBlock extends Block implements EntityBlock {

    private final BiFunction<BlockPos,BlockState,? extends METile> tileSupplier;

    public MEBlock(String registry_name, BiFunction<BlockPos,BlockState,? extends METile> tileSupplier){
        super(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).strength(1.5F, 6.0F).noOcclusion());
        this.tileSupplier = tileSupplier;
        this.setRegistryName(registry_name);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof METile){
            METile meTile = (METile)tile;
            if(meTile.getFacing() == null || meTile.getFacing() != rayTraceResult.getDirection()){
                if(player.isShiftKeyDown() && player.getItemInHand(handIn).isEmpty()){
                    meTile.setCamoState(null);
                    return InteractionResult.SUCCESS;
                }else if(!player.isShiftKeyDown() && meTile.canBeCamoStack(player.getItemInHand(handIn))){
                    Item item = player.getItemInHand(handIn).getItem();
                    if(item instanceof BlockItem){
                        Block block = ((BlockItem)item).getBlock();
                        BlockState state1 = block.getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, handIn, rayTraceResult)));
                        if(state1 == null)
                            state1 = block.defaultBlockState();
                        meTile.setCamoState(state1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        this.onRightClick(state, worldIn, pos, player, handIn, rayTraceResult);
        return InteractionResult.SUCCESS;
    }

    protected void onRightClick(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult rayTraceResult){
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return this.tileSupplier.apply(pos, state);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state){
        return PushReaction.BLOCK;
    }

    @Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.MODEL;
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, @Nullable EntityType<?> entityType){
        return false;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context){
        return Shapes.empty();
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos){
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos){
        return true;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> blockEntityType){
        return blockEntityType.getRegistryName().getNamespace().equals("movingelevators") ?
            (world2, pos, state2, blockEntity) -> {
                if(blockEntity instanceof ElevatorInputTile)
                    ((ElevatorInputTile)blockEntity).tick();
            } : null;
    }
}
