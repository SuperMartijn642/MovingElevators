package com.supermartijn642.movingelevators.gui.preview;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.TextureAtlases;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * Created 25/12/2021 by SuperMartijn642
 */
public class ElevatorPreviewRenderer {

    public static void renderPreview(WorldBlockCapture capture, AxisAlignedBB cabinBox, AxisAlignedBB previewBox, double x, double y, double scale, float yaw, float pitch, boolean doShading){
        AxisAlignedBB bounds = capture.getBounds();
        Vec3d center = bounds.getCenter();
        double span = Math.sqrt(bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize());
        scale /= span;

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());
        ClientUtils.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS).pushFilter(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1, 1, 1, 1);

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, 350);
        GlStateManager.scalef(1, -1, 1);
        GlStateManager.scaled(scale, scale, scale);

        GlStateManager.rotated(pitch, 1, 0, 0);
        GlStateManager.rotated(yaw, 0, 1, 0);
        GlStateManager.translated(-center.x, -center.y, -center.z);

        if(doShading)
            RenderHelper.turnOnGui();
        else
            GlStateManager.disableLighting();

        for(BlockPos pos : capture.getBlockLocations())
            renderBlock(capture, pos);

        if(doShading)
            GlStateManager.disableLighting();

        RenderUtils.renderBox(cabinBox, 1, 1, 1, 0.8f, true);
        if(previewBox != null)
            RenderUtils.renderBox(previewBox, 0, 0.7f, 0, 0.8f, true);

        GlStateManager.popMatrix();

        ClientUtils.getTextureManager().getTexture(TextureAtlases.getBlocks()).popFilter();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableRescaleNormal();
    }

    private static void renderBlock(WorldBlockCapture capture, BlockPos pos){
        GlStateManager.pushMatrix();
        GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

        BlockState state = capture.getBlockState(pos);
        if(state.getBlock() != Blocks.AIR){
            IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
            IModelData modelData = EmptyModelData.INSTANCE;
//            if(model instanceof RechiseledConnectedBakedModel){ // TODO
//                RechiseledModelData data = new RechiseledModelData();
//                for(Direction direction : Direction.values())
//                    data.sides.put(direction, new RechiseledModelData.SideData(direction, capture::getBlock, pos, state.getBlock()));
//                modelData = new ModelDataMap.Builder().withInitial(RechiseledModelData.PROPERTY, data).build();
//            }

            renderLitItem(model, modelData);
        }

        TileEntity blockEntity = capture.getBlockEntity(pos);
        if(blockEntity != null)
            TileEntityRendererDispatcher.instance.render(blockEntity, 0, 0, 0, ClientUtils.getPartialTicks(), -1, false);

        GlStateManager.popMatrix();
    }

    //
    // Below is all copied from ForgeHooksClient and edited to pass along the model data
    //

    private static class LightGatheringTransformer extends QuadGatheringTransformer {

        private static final VertexFormat FORMAT = new VertexFormat().addElement(DefaultVertexFormats.ELEMENT_UV0).addElement(DefaultVertexFormats.ELEMENT_UV1);

        int blockLight, skyLight;

        {this.setVertexFormat(FORMAT);}

        boolean hasLighting(){
            return this.dataLength[1] >= 2;
        }

        @Override
        protected void processQuad(){
            // Reset light data
            this.blockLight = 0;
            this.skyLight = 0;
            // Compute average light for all 4 vertices
            for(int i = 0; i < 4; i++){
                this.blockLight += (int)((this.quadData[1][i][0] * 0xFFFF) / 0x20);
                this.skyLight += (int)((this.quadData[1][i][1] * 0xFFFF) / 0x20);
            }
            // Values must be multiplied by 16, divided by 4 for average => x4
            this.blockLight *= 4;
            this.skyLight *= 4;
        }

        // Dummy overrides

        @Override
        public void setQuadTint(int tint){
        }

        @Override
        public void setQuadOrientation(Direction orientation){
        }

        @Override
        public void setApplyDiffuseLighting(boolean diffuse){
        }

        @Override
        public void setTexture(TextureAtlasSprite texture){
        }
    }

    private static final LightGatheringTransformer lightGatherer = new LightGatheringTransformer();

    public static void renderLitItem(IBakedModel model, IModelData modelData){
        List<BakedQuad> allquads = new ArrayList<>();
        Random random = new Random();
        long seed = 42L;

        for(Direction enumfacing : Direction.values()){
            random.setSeed(seed);
            allquads.addAll(model.getQuads(null, enumfacing, random, modelData));
        }

        random.setSeed(seed);
        allquads.addAll(model.getQuads(null, null, random, modelData));

        if(allquads.isEmpty()) return;

        // Current list of consecutive quads with the same lighting
        List<BakedQuad> segment = new ArrayList<>();

        // Lighting of the current segment
        int segmentBlockLight = 0;
        int segmentSkyLight = 0;
        // Diffuse lighting state
        boolean segmentShading = true;
        // State changed by the current segment
        boolean segmentLightingDirty = false;
        boolean segmentShadingDirty = false;
        // If the current segment contains lighting data
        boolean hasLighting = false;

        for(int i = 0; i < allquads.size(); i++){
            BakedQuad q = allquads.get(i);

            // Lighting of the current quad
            int bl = 0;
            int sl = 0;

            // Fail-fast on ITEM, as it cannot have light data
            if(q.getFormat() != DefaultVertexFormats.BLOCK_NORMALS && q.getFormat().hasUv(1)){
                q.pipe(lightGatherer);
                if(lightGatherer.hasLighting()){
                    bl = lightGatherer.blockLight;
                    sl = lightGatherer.skyLight;
                }
            }

            boolean shade = q.shouldApplyDiffuseLighting();

            boolean lightingDirty = segmentBlockLight != bl || segmentSkyLight != sl;
            boolean shadeDirty = shade != segmentShading;

            // If lighting or color data has changed, draw the segment and flush it
            if(lightingDirty || shadeDirty){
                if(i > 0) // Make sure this isn't the first quad being processed
                {
                    drawSegment(segment, segmentBlockLight, segmentSkyLight, segmentShading, segmentLightingDirty && (hasLighting || segment.size() < i), segmentShadingDirty);
                }
                segmentBlockLight = bl;
                segmentSkyLight = sl;
                segmentShading = shade;
                segmentLightingDirty = lightingDirty;
                segmentShadingDirty = shadeDirty;
                hasLighting = segmentBlockLight > 0 || segmentSkyLight > 0 || !segmentShading;
            }

            segment.add(q);
        }

        drawSegment(segment, segmentBlockLight, segmentSkyLight, segmentShading, segmentLightingDirty && (hasLighting || segment.size() < allquads.size()), segmentShadingDirty);

        // Clean up render state if necessary
        if(hasLighting){
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, GLX.lastBrightnessX, GLX.lastBrightnessY);
            GlStateManager.enableLighting();
        }
    }

    private static void drawSegment(List<BakedQuad> segment, int bl, int sl, boolean shade, boolean updateLighting, boolean updateShading){
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.BLOCK_NORMALS);

        float lastBl = GLX.lastBrightnessX;
        float lastSl = GLX.lastBrightnessY;

        if(updateShading){
            if(shade){
                // (Re-)enable lighting for normal look with shading
                GlStateManager.enableLighting();
            }else{
                // Disable lighting to simulate a lack of diffuse lighting
                GlStateManager.disableLighting();
            }
        }

        if(updateLighting){
            // Force lightmap coords to simulate synthetic lighting
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, Math.max(bl, lastBl), Math.max(sl, lastSl));
        }

        ClientUtils.getItemRenderer().renderQuadList(bufferbuilder, segment, -1, ItemStack.EMPTY);
        Tessellator.getInstance().end();

        // Preserve this as it represents the "world" lighting
        GLX.lastBrightnessX = lastBl;
        GLX.lastBrightnessY = lastSl;

        segment.clear();
    }
}
