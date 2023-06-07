package com.supermartijn642.movingelevators.elevator;

import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.packets.PacketOnElevator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Created 8/6/2021 by SuperMartijn642
 */
public class ElevatorCollisionHandler {

    public static void handleEntityCollisions(Level world, AABB bounds, List<AABB> boundingBoxes, Vec3 position, Vec3 motion){
        bounds = bounds.move(position);
        bounds = new AABB(bounds.minX, bounds.minY + Math.min(0, motion.y), bounds.minZ, bounds.maxX, bounds.maxY + Math.max(0, motion.y), bounds.maxZ);
        bounds = bounds.inflate(2);

        List<? extends Entity> entities = world.getEntities((Entity)null, bounds, ElevatorCollisionHandler::canCollideWith);

        for(Entity entity : entities){
            // horizontal collisions
            for(AABB box : boundingBoxes){
                box = box.move(position);
                handleHorizontalCollision(entity, box);
            }

            // vertical collisions
            for(AABB box : boundingBoxes){
                box = box.move(position);
                handleVerticalCollision(entity, box, motion);
            }
        }
    }

    private static void handleHorizontalCollision(Entity entity, AABB box){
        Vec3 entityPos = entity.position();
        Vec3 oldEntityPos = new Vec3(entity.xo, entity.yo, entity.zo);
        Vec3 entityMotion = entityPos.subtract(oldEntityPos);
        AABB entityBox = entity.getBoundingBox().deflate(1E-7d);
        AABB oldEntityBox = entity.getBoundingBox().deflate(1E-7d).move(-entityMotion.x, 0, -entityMotion.z);

        if(oldEntityBox.maxY > box.minY && oldEntityBox.minY < box.maxY){
            if(oldEntityBox.maxX > box.minX && oldEntityBox.minX < box.maxX){
                if(oldEntityBox.maxZ < box.minZ && entityBox.maxZ > box.minZ){
                    entity.setPos(entityPos.x, entityPos.y, box.minZ - entity.getBbWidth() / 2);
                    entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, 0);
                }else if(oldEntityBox.minZ > box.maxZ && entityBox.minZ < box.maxZ){
                    entity.setPos(entityPos.x, entityPos.y, box.maxZ + entity.getBbWidth() / 2);
                    entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, 0);
                }
            }else if(oldEntityBox.maxZ > box.minZ && oldEntityBox.minZ < box.maxZ){
                if(oldEntityBox.maxX < box.minX && entityBox.maxX > box.minX){
                    entity.setPos(box.minX - entity.getBbWidth() / 2, entityPos.y, entityPos.z);
                    entity.setDeltaMovement(0, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
                }else if(oldEntityBox.minX > box.maxX && entityBox.minX < box.maxX){
                    entity.setPos(box.maxX + entity.getBbWidth() / 2, entityPos.y, entityPos.z);
                    entity.setDeltaMovement(0, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
                }
            }
        }
    }

    private static void handleVerticalCollision(Entity entity, AABB box, Vec3 motion){
        AABB newBox = box.move(motion);
        boolean movingUp = motion.y > 0;

        Vec3 entityPos = entity.position();
        Vec3 oldEntityPos = new Vec3(entity.xo, entity.yo, entity.zo);
        Vec3 entityMotion = entityPos.subtract(oldEntityPos);
        AABB entityBox = entity.getBoundingBox().deflate(1E-7d);
        AABB oldEntityBox = entity.getBoundingBox().deflate(1E-7d).move(-entityMotion.x, -entityMotion.y, -entityMotion.z);

        if(oldEntityBox.maxX > box.minX && oldEntityBox.minX < box.maxX && oldEntityBox.maxZ > box.minZ && oldEntityBox.minZ < box.maxZ){
            if(oldEntityBox.maxY < box.minY && entityBox.maxY > newBox.minY){
                entity.setPos(entityPos.x, newBox.minY - entity.getBbHeight(), entityPos.z);
                entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
            }else if(oldEntityBox.minY > box.maxY && (entityBox.minY < newBox.maxY || (!movingUp && canPullEntity(entity) && oldEntityBox.minY > box.maxY && oldEntityBox.minY < box.maxY + 0.1))){
                entity.setPos(entityPos.x, newBox.maxY, entityPos.z);
                entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);

                entity.setOnGround(true);
                entity.causeFallDamage(entity.fallDistance, 1, entity.damageSources().fall());
                entity.fallDistance = 0;
                if(entity instanceof Player){
                    ElevatorFallDamageHandler.resetElevatorTime((Player)entity);
                    if(entity.level().isClientSide)
                        MovingElevators.CHANNEL.sendToServer(new PacketOnElevator());
                }
            }
        }
    }

    private static boolean canCollideWith(Entity entity){
        return !entity.isSpectator() && !entity.noPhysics && !entity.isPassenger() && entity.getPistonPushReaction() == PushReaction.NORMAL;
    }

    private static boolean canPullEntity(Entity entity){
        return entity.getDeltaMovement().y <= 0 && !entity.isNoGravity();
    }
}
