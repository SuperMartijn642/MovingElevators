package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketOnElevator;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 8/6/2021 by SuperMartijn642
 */
public class ElevatorCollisionHandler {

    public static void handleEntityCollisions(World level, AxisAlignedBB bounds, List<AxisAlignedBB> boundingBoxes, Vec3d position, Vec3d motion){
        bounds = bounds.offset(position);
        bounds = new AxisAlignedBB(bounds.minX, bounds.minY + Math.min(0, motion.y), bounds.minZ, bounds.maxX, bounds.maxY + Math.max(0, motion.y), bounds.maxZ);
        bounds = bounds.grow(2);

        List<? extends Entity> entities = level.getEntitiesInAABBexcluding(null, bounds, ElevatorCollisionHandler::canCollideWith);

        for(Entity entity : entities){
            // horizontal collisions
            for(AxisAlignedBB box : boundingBoxes){
                box = box.offset(position);
                handleHorizontalCollision(entity, box);
            }

            // vertical collisions
            for(AxisAlignedBB box : boundingBoxes){
                box = box.offset(position);
                handleVerticalCollision(entity, box, motion);
            }
        }
    }

    private static void handleHorizontalCollision(Entity entity, AxisAlignedBB box){
        Vec3d entityPos = entity.getPositionVector();
        Vec3d oldEntityPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
        Vec3d entityMotion = entityPos.subtract(oldEntityPos);
        AxisAlignedBB entityBox = entity.getEntityBoundingBox().shrink(1E-7d);
        AxisAlignedBB oldEntityBox = entity.getEntityBoundingBox().shrink(1E-7d).offset(-entityMotion.x, 0, -entityMotion.z);

        if(oldEntityBox.maxY > box.minY && oldEntityBox.minY < box.maxY){
            if(oldEntityBox.maxX > box.minX && oldEntityBox.minX < box.maxX){
                if(oldEntityBox.maxZ < box.minZ && entityBox.maxZ > box.minZ){
                    entity.setPosition(entityPos.x, entityPos.y, box.minZ - entity.width / 2);
                    entity.motionZ = 0;
                }else if(oldEntityBox.minZ > box.maxZ && entityBox.minZ < box.maxZ){
                    entity.setPosition(entityPos.x, entityPos.y, box.maxZ + entity.width / 2);
                    entity.motionZ = 0;
                }
            }else if(oldEntityBox.maxZ > box.minZ && oldEntityBox.minZ < box.maxZ){
                if(oldEntityBox.maxX < box.minX && entityBox.maxX > box.minX){
                    entity.setPosition(box.minX - entity.width / 2, entityPos.y, entityPos.z);
                    entity.motionX = 0;
                }else if(oldEntityBox.minX > box.maxX && entityBox.minX < box.maxX){
                    entity.setPosition(box.maxX + entity.width / 2, entityPos.y, entityPos.z);
                    entity.motionX = 0;
                }
            }
        }
    }

    private static void handleVerticalCollision(Entity entity, AxisAlignedBB box, Vec3d motion){
        AxisAlignedBB newBox = box.offset(motion);
        boolean movingUp = motion.y > 0;

        Vec3d entityPos = entity.getPositionVector();
        Vec3d oldEntityPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
        Vec3d entityMotion = entityPos.subtract(oldEntityPos);
        AxisAlignedBB entityBox = entity.getEntityBoundingBox().shrink(1E-7d);
        AxisAlignedBB oldEntityBox = entity.getEntityBoundingBox().shrink(1E-7d).offset(-entityMotion.x, -entityMotion.y, -entityMotion.z);

        if(oldEntityBox.maxX > box.minX && oldEntityBox.minX < box.maxX && oldEntityBox.maxZ > box.minZ && oldEntityBox.minZ < box.maxZ){
            if(oldEntityBox.maxY < box.minY && entityBox.maxY > newBox.minY){
                entity.setPosition(entityPos.x, newBox.minY - entity.width, entityPos.z);
                entity.motionY = 0;
            }else if(oldEntityBox.minY > box.maxY && (entityBox.minY < newBox.maxY || (!movingUp && canPullEntity(entity) && oldEntityBox.minY > box.maxY && oldEntityBox.minY < box.maxY + 0.1))){
                entity.setPosition(entityPos.x, newBox.maxY, entityPos.z);
                entity.motionY = 0;

                entity.onGround = true;
                entity.fall(entity.fallDistance, 1);
                entity.fallDistance = 0;
                if(entity instanceof EntityPlayer){
                    ElevatorFallDamageHandler.resetElevatorTime((EntityPlayer)entity);
                    if(entity.world.isRemote)
                        MovingElevators.CHANNEL.sendToServer(new PacketOnElevator());
                }
            }
        }
    }

    private static boolean canCollideWith(Entity entity){
        return !(entity instanceof EntityPlayer && ((EntityPlayer)entity).isSpectator()) && entity.canBeCollidedWith() && !entity.isRiding() && entity.getPushReaction() == EnumPushReaction.NORMAL;
    }

    private static boolean canPullEntity(Entity entity){
        return entity.motionY <= 0 && !entity.hasNoGravity();
    }
}
