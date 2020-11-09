package com.supermartijn642.movingelevators;

import com.supermartijn642.movingelevators.base.ElevatorInputTileRenderer;

/**
 * Created 4/5/2020 by SuperMartijn642
 */
public class ElevatorBlockTileRenderer extends ElevatorInputTileRenderer<ElevatorBlockTile> {

    public ElevatorBlockTileRenderer(){
        super();
    }

    @Override
    protected void render(){
        if(!tile.hasGroup())
            return;

        super.render();

        // render platform
//        this.renderPlatform();
    }

//    private void renderPlatform(){
//        if(tile.getGroup().getLowest() != tile.getPos().getY() || !tile.getGroup().isMoving() || tile.getGroup().getCurrentY() == tile.getGroup().getLastY())
//            return;
//        IBlockState[][] state = tile.getGroup().getPlatform();
//        int size = tile.getGroup().getSize();
//        double lastY = tile.getGroup().getLastY(), currentY = tile.getGroup().getCurrentY();
//        double renderY = lastY + (currentY - lastY) * partialTicks;
//        int startX = tile.getFacing().getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
//        int startZ = tile.getFacing().getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;
//
//        for(int platformX = 0; platformX < size; platformX++){
//            for(int platformZ = 0; platformZ < size; platformZ++){
//                BlockPos pos = tile.getPos().add(startX + platformX, renderY, startZ + platformZ);
//
//                GlStateManager.pushMatrix();
//
//                GlStateManager.translate(x, y, z);
//                GlStateManager.translate(0, renderY - pos.getY(), 0);
//
//                Tessellator tessellator = Tessellator.getInstance();
//                BufferBuilder buffer = tessellator.getBuffer();
//                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//
//                GlStateManager.disableLighting();
//
//                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//
//                try{
//                    BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
//                    IBakedModel model = brd.getModelForState(state[platformX][platformZ]);
//                    brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state[platformX][platformZ], pos, buffer, false);
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//
//                GlStateManager.translate(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());
//
//                tessellator.draw();
//
//                GlStateManager.popMatrix();
//            }
//        }
//    }
}
