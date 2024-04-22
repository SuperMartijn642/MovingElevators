package com.supermartijn642.movingelevators.elevator;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElevatorGroupRenderer {

    private static boolean isWithinRenderDistance(ElevatorGroup group){
        GameRenderer renderer = ClientUtils.getMinecraft().gameRenderer;
        if(renderer == null)
            return false;
        float renderDistance = renderer.getRenderDistance() + 8 + group.getCageSizeX() / 2f + group.getCageSizeZ() / 2f;
        BlockPos playerPos = ClientUtils.getPlayer().getCommandSenderBlockPosition();
        float distance = (group.x - playerPos.getX()) * (group.x - playerPos.getX()) + (group.z - playerPos.getZ()) * (group.z - playerPos.getZ());
        return distance < renderDistance * renderDistance;
    }

    @SubscribeEvent
    public static void onRender(RenderWorldEvent e){
        if(!ClientUtils.getMinecraft().getEntityRenderDispatcher().shouldRenderHitBoxes())
            return;
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        GlStateManager.pushMatrix();
        Vec3d camera = RenderUtils.getCameraPosition();
        GlStateManager.translated(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            if(isWithinRenderDistance(group))
                renderGroupCageOutlines(group);
        }
        GlStateManager.popMatrix();
    }

    public static void renderBlocks(BlockRenderLayer renderType){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        GlStateManager.pushMatrix();
        Vec3d camera = RenderUtils.getCameraPosition();
        GlStateManager.translated(-camera.x, -camera.y, -camera.z);
        BufferBuilder buffer = null;
        for(ElevatorGroup group : groups.getGroups()){
            if(group.isMoving() && isWithinRenderDistance(group)){
                if(buffer == null){
                    buffer = Tessellator.getInstance().getBuilder();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                }
                renderGroupBlocks(group, renderType, buffer, ClientUtils.getPartialTicks());
            }
        }
        if(buffer != null)
            Tessellator.getInstance().end();
        GlStateManager.popMatrix();
    }

    public static void renderBlockEntities(float partialTicks){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        for(ElevatorGroup group : groups.getGroups()){
            if(group.isMoving() && isWithinRenderDistance(group))
                renderGroupBlockEntities(group, partialTicks);
        }
    }

    public static void renderGroupBlocks(ElevatorGroup group, BlockRenderLayer renderType, BufferBuilder buffer, float partialTicks){
        ClientElevatorCage cage = (ClientElevatorCage)group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3d startPos = group.getCageAnchorPos(renderY);
        BlockPos anchorPos = new BlockPos((int)startPos.x, (int)startPos.y, (int)startPos.z);
        cage.loadRenderInfo(anchorPos, group);
        World level = ClientElevatorCage.getFakeLevel();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockStates[x][y][z] == null)
                        continue;

                    buffer.offset(0, startPos.y - anchorPos.getY(), 0);

                    BlockState state = cage.blockStates[x][y][z];
                    if(state.getRenderShape() == BlockRenderType.MODEL && state.canRenderInLayer(renderType)){
                        IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
                        IModelData modelData = cage.blockEntities[x][y][z] == null ? EmptyModelData.INSTANCE : cage.blockEntities[x][y][z].getModelData();
                        modelData = model.getModelData(level, pos, state, modelData);
                        pos.set(anchorPos.getX() + x, anchorPos.getY() + y, anchorPos.getZ() + z);
                        ClientUtils.getBlockRenderer().renderBlock(state, pos, level, buffer, level.random, modelData);
                    }
                    buffer.offset(0, 0, 0);
                }
            }
        }
    }

    public static void renderGroupBlockEntities(ElevatorGroup group, float partialTicks){
        ClientElevatorCage cage = (ClientElevatorCage)group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3d startPos = group.getCageAnchorPos(renderY);
        BlockPos anchorPos = new BlockPos((int)startPos.x, (int)startPos.y, (int)startPos.z);
        cage.loadRenderInfo(anchorPos, group);

        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockEntities[x][y][z] == null)
                        continue;

                    GlStateManager.pushMatrix();
                    GlStateManager.translated(0, startPos.y - anchorPos.getY(), 0);

                    TileEntity entity = cage.blockEntities[x][y][z];
                    TileEntityRendererDispatcher.instance.render(entity, partialTicks, -1);

                    GlStateManager.popMatrix();
                }
            }
        }
    }

    public static void renderGroupCageOutlines(ElevatorGroup group){
        for(int floor = 0; floor < group.getFloorCount(); floor++){
            BlockPos anchorPos = group.getCageAnchorBlockPos(group.getFloorYLevel(floor));
            AxisAlignedBB cageArea = new AxisAlignedBB(anchorPos, anchorPos.offset(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ()));
            cageArea.inflate(0.01);
            RenderUtils.renderBox(cageArea, 1, 1, 1, true);
        }
        if(group.isMoving()){
            ElevatorCage cage = group.getCage();
            double lastY = group.getLastY(), currentY = group.getCurrentY();
            double renderY = lastY + (currentY - lastY) * ClientUtils.getPartialTicks();
            Vec3d startPos = group.getCageAnchorPos(renderY);
            RenderUtils.renderBox(new AxisAlignedBB(startPos, startPos.add(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ())), 1, 0, 0, true);
            RenderUtils.renderShape(cage.shape.move(startPos.x, startPos.y, startPos.z), 49 / 255f, 224 / 255f, 219 / 255f, true);
        }
    }
}
