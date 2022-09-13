package com.supermartijn642.movingelevators.elevator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;
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
        LazyOptional<ElevatorGroupCapability> optional = ClientUtils.getWorld().getCapability(ElevatorGroupCapability.CAPABILITY);
        if(!optional.isPresent())
            return;
        ElevatorGroupCapability groups = optional.resolve().get();

        e.getPoseStack().pushPose();
        Vector3d camera = RenderUtils.getCameraPosition();
        e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, group.getCurrentY(), group.z);
            if(elevatorPos.distSqr(Minecraft.getInstance().player.blockPosition()) < RENDER_DISTANCE)
                renderGroup(e.getPoseStack(), group, RenderUtils.getMainBufferSource(), e.getPartialTicks());
        }
        e.getPoseStack().popPose();
    }

    public static void renderGroup(MatrixStack poseStack, ElevatorGroup group, IRenderTypeBuffer buffer, float partialTicks){
        if(ClientUtils.getMinecraft().getEntityRenderDispatcher().shouldRenderHitBoxes())
            renderGroupCageOutlines(poseStack, group);

        if(!group.isMoving())
            return;

        ElevatorCage cage = group.getCage();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double renderY = lastY + (currentY - lastY) * partialTicks;
        Vector3d startPos = group.getCageAnchorPos(renderY);

        BlockPos topPos = new BlockPos(group.x, renderY, group.z).relative(group.facing, (int)Math.ceil(group.getCageDepth() / 2f));
        int currentLight = WorldRenderer.getLightColor(group.level, topPos);

        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    if(cage.blockStates[x][y][z] == null)
                        continue;

                    poseStack.pushPose();

                    poseStack.translate(startPos.x + x, startPos.y + y, startPos.z + z);

                    ClientUtils.getBlockRenderer().renderBlock(cage.blockStates[x][y][z], poseStack, buffer, currentLight, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

                    poseStack.popPose();
                }
            }
        }

        if(ClientUtils.getMinecraft().getEntityRenderDispatcher().shouldRenderHitBoxes()){
            RenderUtils.renderBox(poseStack, new AxisAlignedBB(startPos, startPos.add(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ())), 1, 0, 0, true);
            RenderUtils.renderShape(poseStack, cage.shape.move(startPos.x, startPos.y, startPos.z), 49 / 255f, 224 / 255f, 219 / 255f, true);
        }
    }

    public static void renderGroupCageOutlines(MatrixStack poseStack, ElevatorGroup group){
        for(int floor = 0; floor < group.getFloorCount(); floor++){
            BlockPos anchorPos = group.getCageAnchorBlockPos(group.getFloorYLevel(floor));
            AxisAlignedBB cageArea = new AxisAlignedBB(anchorPos, anchorPos.offset(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ()));
            cageArea.inflate(0.01);
            RenderUtils.renderBox(poseStack, cageArea, 1, 1, 1, true);
        }
    }
}
