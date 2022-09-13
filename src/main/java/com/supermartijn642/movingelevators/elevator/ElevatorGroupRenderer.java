package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.TextureAtlases;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ElevatorGroupRenderer {

    public static final double RENDER_DISTANCE = 255 * 255 * 4;

    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent e){
        ElevatorGroupCapability groups = ClientUtils.getWorld().getCapability(ElevatorGroupCapability.CAPABILITY, null);
        if(groups == null)
            return;

        GlStateManager.pushMatrix();
        Vec3d camera = RenderUtils.getCameraPosition();
        GlStateManager.translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, group.getCurrentY(), group.z);
            if(elevatorPos.distanceSq(ClientUtils.getPlayer().getPosition()) < RENDER_DISTANCE)
                renderGroup(group, e.getPartialTicks());
        }
        GlStateManager.popMatrix();
    }

    public static void renderGroup(ElevatorGroup group, float partialTicks){
        if(ClientUtils.getMinecraft().getRenderManager().isDebugBoundingBox())
            renderGroupCageOutlines(group);

        if(!group.isMoving())
            return;

        ElevatorCage cage = group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3d startPos = group.getCageAnchorPos(renderY);

        BlockPos topPos = new BlockPos(group.x, renderY, group.z).offset(group.facing, (int)Math.ceil(group.getCageDepth() / 2f));
        int currentLight = group.level.getCombinedLight(topPos, group.level.getBlockState(topPos).getLightValue(group.level, topPos));
        int j = currentLight >> 16 & 65535;
        int k = currentLight & 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.shadeModel(7425);

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockStates[x][y][z] == null)
                        continue;

                    buffer.setTranslation(0, startPos.y - (int)startPos.y, 0);

                    try{
                        BlockRendererDispatcher rendererDispatcher = ClientUtils.getBlockRenderer();
                        IBakedModel model = rendererDispatcher.getModelForState(cage.blockStates[x][y][z]);
                        pos.setPos(startPos.x + x, (int)startPos.y + y, startPos.z + z);
                        rendererDispatcher.getBlockModelRenderer().renderModel(group.level, model, cage.blockStates[x][y][z], pos, buffer, false, 0);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    buffer.setTranslation(0, 0, 0);
                }
            }
        }

        Tessellator.getInstance().draw();

        if(ClientUtils.getMinecraft().getRenderManager().isDebugBoundingBox()){
            RenderUtils.renderBox(new AxisAlignedBB(startPos, startPos.addVector(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ())), 1, 0, 0, true);
            RenderUtils.renderShape(cage.shape.offset(startPos.x, startPos.y, startPos.z), 49 / 255f, 224 / 255f, 219 / 255f, true);
        }
    }

    public static void renderGroupCageOutlines(ElevatorGroup group){
        for(int floor = 0; floor < group.getFloorCount(); floor++){
            BlockPos anchorPos = group.getCageAnchorBlockPos(group.getFloorYLevel(floor));
            AxisAlignedBB cageArea = new AxisAlignedBB(anchorPos, anchorPos.add(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ()));
            cageArea.grow(0.01);
            RenderUtils.renderBox(cageArea, 1, 1, 1, true);
        }
    }
}
