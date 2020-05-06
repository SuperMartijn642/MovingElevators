package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTileRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

/**
 * Created 3/29/2020 by SuperMartijn642
 */
public class ElevatorBlockTileRenderer extends ElevatorInputTileRenderer<ElevatorBlockTile> {

    public ElevatorBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn){
        super(rendererDispatcherIn);
    }

    @Override
    protected void render(){
        if(!tile.hasGroup())
            return;

        super.render();

        // render platform
        this.renderPlatform();
    }

    private void renderPlatform(){
        if(tile.getGroup().getLowest() != tile.getPos().getY() || !tile.getGroup().isMoving() || tile.getGroup().getCurrentY() == tile.getGroup().getLastY())
            return;
        BlockState[][] state = tile.getGroup().getPlatform();
        int size = tile.getGroup().getSize();
        double lastY = tile.getGroup().getLastY(), currentY = tile.getGroup().getCurrentY();
        double y = lastY + (currentY - lastY) * partialTicks - tile.getPos().getY();
        int startX = tile.getFacing().getXOffset() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = tile.getFacing().getZOffset() * (int)Math.ceil(size / 2f) - size / 2;

        BlockPos topPos = tile.getPos().offset(tile.getFacing(), (int)Math.ceil(size / 2f)).add(0, y, 0);
        int currentLight = WorldRenderer.getCombinedLight(tile.getWorld(), topPos);

        for(int x = 0; x < size; x++){
            for(int z = 0; z < size; z++){
                matrixStack.push();

                matrixStack.translate(startX + x, y, startZ + z);

                Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state[x][z], matrixStack, buffer, currentLight, combinedOverlay, EmptyModelData.INSTANCE);

                matrixStack.pop();
            }
        }
    }
}
