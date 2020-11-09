package com.supermartijn642.movingelevators;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
        ElevatorGroupCapability groups = Minecraft.getMinecraft().world.getCapability(ElevatorGroupCapability.CAPABILITY, null);
        if(groups == null)
            return;

        GlStateManager.pushMatrix();
        Vec3d matrix = ClientProxy.getPlayer().getPositionEyes(e.getPartialTicks());
        GlStateManager.translate(-matrix.x, -matrix.y, -matrix.z);
        for(ElevatorGroup group : groups.getGroups()){
            BlockPos elevatorPos = new BlockPos(group.x, group.getCurrentY(), group.z);
            if(elevatorPos.distanceSq(Minecraft.getMinecraft().player.getPosition()) < RENDER_DISTANCE)
                renderGroup(group, e.getPartialTicks());
        }
        GlStateManager.popMatrix();
    }

    public static void renderGroup(ElevatorGroup group, float partialTicks){
        if(!group.isMoving() || group.getCurrentY() == group.getLastY())
            return;
        IBlockState[][] state = group.getPlatform();
        int size = group.getSize();
        double lastY = group.getLastY(), currentY = group.getCurrentY();
        double y = lastY + (currentY - lastY) * partialTicks;
        int startX = group.x + group.facing.getFrontOffsetX() * (int)Math.ceil(size / 2f) - size / 2;
        int startZ = group.z + group.facing.getFrontOffsetZ() * (int)Math.ceil(size / 2f) - size / 2;

        for(int x = 0; x < size; x++){
            for(int z = 0; z < size; z++){
                GlStateManager.pushMatrix();

                GlStateManager.translate(startX + x, y + ClientProxy.getPlayer().eyeHeight, startZ + z);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                GlStateManager.disableLighting();

                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                try{
                    BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
                    IBakedModel model = brd.getModelForState(state[x][z]);
                    brd.getBlockModelRenderer().renderModel(group.world, model, state[x][z], BlockPos.ORIGIN, buffer, false);
                }catch(Exception e){
                    e.printStackTrace();
                }

                tessellator.draw();

                GlStateManager.popMatrix();
            }
        }
    }

}
