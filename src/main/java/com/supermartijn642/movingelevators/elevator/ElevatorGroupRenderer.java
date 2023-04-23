package com.supermartijn642.movingelevators.elevator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 11/8/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElevatorGroupRenderer {

    public static final double RENDER_DISTANCE = 255 * 255 * 4;

    @SubscribeEvent
    public static void onRender(RenderWorldEvent e){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        e.getPoseStack().pushPose();
        Vec3d camera = RenderUtils.getCameraPosition();
        e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, (int)group.getCurrentY(), group.z);
            if(elevatorPos.distSqr(Minecraft.getInstance().player.getCommandSenderBlockPosition()) < RENDER_DISTANCE
                && ClientUtils.getMinecraft().getEntityRenderDispatcher().shouldRenderHitBoxes())
                renderGroupCageOutlines(e.getPoseStack(), group);
        }
        e.getPoseStack().popPose();
    }

    public static void renderBlocks(MatrixStack poseStack, RenderType renderType, IRenderTypeBuffer bufferSource){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        IVertexBuilder buffer = bufferSource.getBuffer(renderType);
        poseStack.pushPose();
        Vec3d camera = RenderUtils.getCameraPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            if(group.isMoving()){
                BlockPos elevatorPos = new BlockPos(group.x, (int)group.getCurrentY(), group.z);
                if(elevatorPos.distSqr(Minecraft.getInstance().player.getCommandSenderBlockPosition()) < RENDER_DISTANCE)
                    renderGroupBlocks(poseStack, group, renderType, buffer, ClientUtils.getPartialTicks());
            }
        }
        poseStack.popPose();

        // For some reason this is needed ¯\(o_o)/¯
        if(renderType == RenderType.translucent())
            ((IRenderTypeBuffer.Impl)bufferSource).endBatch(renderType);
    }

    public static void renderBlockEntities(MatrixStack poseStack, float partialTicks, IRenderTypeBuffer bufferSource){
        ElevatorGroupCapability groups = ElevatorGroupCapability.get(ClientUtils.getWorld());

        poseStack.pushPose();
        Vec3d camera = RenderUtils.getCameraPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            if(group.isMoving()){
                BlockPos elevatorPos = new BlockPos(group.x, (int)group.getCurrentY(), group.z);
                if(elevatorPos.distSqr(Minecraft.getInstance().player.getCommandSenderBlockPosition()) < RENDER_DISTANCE)
                    renderGroupBlockEntities(poseStack, group, bufferSource, partialTicks);
            }
        }
        poseStack.popPose();
    }

    public static void renderGroupBlocks(MatrixStack poseStack, ElevatorGroup group, RenderType renderType, IVertexBuilder buffer, float partialTicks){
        ClientElevatorCage cage = (ClientElevatorCage)group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vec3d startPos = group.getCageAnchorPos(renderY);
        BlockPos anchorPos = new BlockPos((int)startPos.x, (int)startPos.y, (int)startPos.z);
        cage.loadRenderInfo(anchorPos, group);
        World level = ClientElevatorCage.getFakeLevel();

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockStates[x][y][z] == null)
                        continue;

                    poseStack.pushPose();
                    poseStack.translate(startPos.x + x, startPos.y + y, startPos.z + z);

                    BlockState state = cage.blockStates[x][y][z];
                    if(state.getRenderShape() == BlockRenderType.MODEL && RenderTypeLookup.canRenderInLayer(state, renderType)){
                        IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
                        IModelData modelData = cage.blockEntities[x][y][z] == null ? EmptyModelData.INSTANCE : cage.blockEntities[x][y][z].getModelData();
                        modelData = model.getModelData(level, pos, state, modelData);
                        pos.set(anchorPos.getX() + x, anchorPos.getY() + y, anchorPos.getZ() + z);
                        ClientUtils.getBlockRenderer().renderModel(state, pos, level, poseStack, buffer, true, level.random, modelData);
                    }
                    poseStack.popPose();
                }
            }
        }
    }

    public static void renderGroupBlockEntities(MatrixStack poseStack, ElevatorGroup group, IRenderTypeBuffer buffer, float partialTicks){
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

                    poseStack.pushPose();
                    poseStack.translate(startPos.x + x, startPos.y + y, startPos.z + z);

                    TileEntity entity = cage.blockEntities[x][y][z];
                    TileEntityRendererDispatcher.instance.render(entity, partialTicks, poseStack, buffer);

                    poseStack.popPose();
                }
            }
        }
    }

    public static void renderGroupCageOutlines(MatrixStack poseStack, ElevatorGroup group){
        for(int floor = 0; floor < group.getFloorCount(); floor++){
            BlockPos anchorPos = group.getCageAnchorBlockPos(group.getFloorYLevel(floor));
            AxisAlignedBB cageArea = new AxisAlignedBB(anchorPos, anchorPos.offset(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ()));
            cageArea.inflate(0.01);
            RenderUtils.renderBox(poseStack, cageArea, 1, 1, 1, true);
        }
        if(group.isMoving()){
            ElevatorCage cage = group.getCage();
            double lastY = group.getLastY(), currentY = group.getCurrentY();
            double renderY = lastY + (currentY - lastY) * ClientUtils.getPartialTicks();
            Vec3d startPos = group.getCageAnchorPos(renderY);
            RenderUtils.renderBox(poseStack, new AxisAlignedBB(startPos, startPos.add(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ())), 1, 0, 0, true);
            RenderUtils.renderShape(poseStack, cage.shape.move(startPos.x, startPos.y, startPos.z), 49 / 255f, 224 / 255f, 219 / 255f, true);
        }
    }
}
