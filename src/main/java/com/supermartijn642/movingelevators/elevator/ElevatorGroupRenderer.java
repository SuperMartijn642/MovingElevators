package com.supermartijn642.movingelevators.elevator;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.TextureAtlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElevatorGroupRenderer {

    public static final double RENDER_DISTANCE = 255 * 255 * 4;

    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent e){
        LazyOptional<ElevatorGroupCapability> optional = ClientUtils.getWorld().getCapability(ElevatorGroupCapability.CAPABILITY);
        if(!optional.isPresent())
            return;
        ElevatorGroupCapability groups = optional.orElseGet(null);

        GlStateManager.pushMatrix();
        Vec3d camera = RenderUtils.getCameraPosition();
        GlStateManager.translated(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, group.getCurrentY(), group.z);
            if(elevatorPos.distSqr(ClientUtils.getPlayer().getCommandSenderBlockPosition()) < RENDER_DISTANCE)
                renderGroup(group, e.getPartialTicks());
        }
        GlStateManager.popMatrix();
    }

    public static void renderGroup(ElevatorGroup group, float partialTicks){
        if(ClientUtils.getMinecraft().getEntityRenderDispatcher().shouldRenderHitBoxes())
            renderGroupCageOutlines(group);

        if(!group.isMoving())
            return;

        ElevatorCage cage = group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3d startPos = group.getCageAnchorPos(renderY);

        BlockPos topPos = new BlockPos(group.x, renderY, group.z).relative(group.facing, (int)Math.ceil(group.getCageDepth() / 2f));
        int currentLight = group.level.getLightColor(topPos, group.level.getBlockState(topPos).getLightValue(group.level, topPos));
        int j = currentLight % 65536;
        int k = currentLight / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);
        GlStateManager.enableLighting();
        RenderHelper.turnOff();
        GlStateManager.shadeModel(7425);

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());

        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockStates[x][y][z] == null)
                        continue;

                    buffer.offset(0, startPos.y - (int)startPos.y, 0);

                    try{
                        BlockRendererDispatcher rendererDispatcher = ClientUtils.getBlockRenderer();
                        IBakedModel model = rendererDispatcher.getBlockModel(cage.blockStates[x][y][z]);
                        pos.set(startPos.x + x, (int)startPos.y + y, startPos.z + z);
                        rendererDispatcher.getModelRenderer().renderModel(group.level, model, cage.blockStates[x][y][z], pos, buffer, false, new Random(), 0, EmptyModelData.INSTANCE);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    buffer.offset(0, 0, 0);
                }
            }
        }

        Tessellator.getInstance().end();

        if(ClientUtils.getMinecraft().getEntityRenderDispatcher().shouldRenderHitBoxes()){
            RenderUtils.renderBox(new AxisAlignedBB(startPos, startPos.add(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ())), 1, 0, 0, true);
            RenderUtils.renderShape(cage.shape.move(startPos.x, startPos.y, startPos.z), 49 / 255f, 224 / 255f, 219 / 255f, true);
        }
    }

    public static void renderGroupCageOutlines(ElevatorGroup group){
        for(int floor = 0; floor < group.getFloorCount(); floor++){
            BlockPos anchorPos = group.getCageAnchorBlockPos(group.getFloorYLevel(floor));
            AxisAlignedBB cageArea = new AxisAlignedBB(anchorPos, anchorPos.offset(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ()));
            cageArea.inflate(0.01);
            RenderUtils.renderBox(cageArea, 1, 1, 1, true);
        }
    }
}
