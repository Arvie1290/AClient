package com.ave.addon.mixins;

import com.ave.addon.modules.FreecamPlus;
import com.ave.addon.modules.FreelookPlus;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 2000)
public abstract class CameraMixin {
    @Shadow protected abstract void setPos(Vec3d pos);
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow private boolean thirdPerson;

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(World world, Entity focusedEntity, boolean thirdPerson, boolean inverted, float tickDelta, CallbackInfo ci) {
        // --- 1. FREECAM PLUS LOGIC ---
        if (FreecamPlus.isActive) {
            if (FreecamPlus.cameraX == 0.0 && FreecamPlus.cameraY == 0.0 && FreecamPlus.cameraZ == 0.0) {
                FreecamPlus.isActive = false;
                return;
            }

            this.thirdPerson = true;
            FreecamPlus freecamMod = Modules.get().get(FreecamPlus.class);

            if (FreecamPlus.isReturning && freecamMod != null && freecamMod.cinematicReturn.get()) {
                Vec3d targetPos = focusedEntity.getCameraPosVec(tickDelta);
                float targetYaw = focusedEntity.getYaw(tickDelta);
                float targetPitch = focusedEntity.getPitch(tickDelta);

                FreecamPlus.cameraX = MathHelper.lerp(FreecamPlus.RETURN_SPEED, FreecamPlus.cameraX, targetPos.x);
                FreecamPlus.cameraY = MathHelper.lerp(FreecamPlus.RETURN_SPEED, FreecamPlus.cameraY, targetPos.y);
                FreecamPlus.cameraZ = MathHelper.lerp(FreecamPlus.RETURN_SPEED, FreecamPlus.cameraZ, targetPos.z);

                FreecamPlus.cameraYaw = MathHelper.lerpAngleDegrees(FreecamPlus.RETURN_SPEED, FreecamPlus.cameraYaw, targetYaw);
                FreecamPlus.cameraPitch = MathHelper.lerpAngleDegrees(FreecamPlus.RETURN_SPEED, FreecamPlus.cameraPitch, targetPitch);

                setPos(new Vec3d(FreecamPlus.cameraX, FreecamPlus.cameraY, FreecamPlus.cameraZ));
                setRotation(FreecamPlus.cameraYaw, FreecamPlus.cameraPitch);

                double dx = FreecamPlus.cameraX - targetPos.x;
                double dy = FreecamPlus.cameraY - targetPos.y;
                double dz = FreecamPlus.cameraZ - targetPos.z;
                if ((dx * dx + dy * dy + dz * dz) < 0.0025) {
                    FreecamPlus.isActive = false;
                    FreecamPlus.isReturning = false;
                    this.thirdPerson = thirdPerson;
                }
            } else {
                if (FreecamPlus.isReturning) {
                    FreecamPlus.isActive = false;
                    FreecamPlus.isReturning = false;
                    this.thirdPerson = thirdPerson;
                    return;
                }

                float smooth = freecamMod != null ? freecamMod.smoothFactor.get().floatValue() : 0.12f;

                FreecamPlus.cameraX = MathHelper.lerp(smooth, FreecamPlus.cameraX, FreecamPlus.targetX);
                FreecamPlus.cameraY = MathHelper.lerp(smooth, FreecamPlus.cameraY, FreecamPlus.targetY);
                FreecamPlus.cameraZ = MathHelper.lerp(smooth, FreecamPlus.cameraZ, FreecamPlus.targetZ);

                FreecamPlus.cameraYaw = MathHelper.lerpAngleDegrees(smooth, FreecamPlus.cameraYaw, FreecamPlus.targetTextYaw);
                FreecamPlus.cameraPitch = MathHelper.lerp(smooth, FreecamPlus.cameraPitch, FreecamPlus.targetTextPitch);

                setPos(new Vec3d(FreecamPlus.cameraX, FreecamPlus.cameraY, FreecamPlus.cameraZ));
                setRotation(FreecamPlus.cameraYaw, FreecamPlus.cameraPitch);
            }
            return;
        }

        // --- 2. FREELOOK PLUS LOGIC ---
        if (FreelookPlus.isActive) {
            FreelookPlus freelookMod = Modules.get().get(FreelookPlus.class);
            if (freelookMod == null) return;

            this.thirdPerson = true;

            float targetYaw = FreelookPlus.cameraYaw;
            float targetPitch = FreelookPlus.cameraPitch;

            float startYaw = FreelookPlus.lockedPlayerYaw;
            float startPitch = FreelookPlus.lockedPlayerPitch;

            float currentYaw = targetYaw;
            float currentPitch = targetPitch;

            // Baca jarak maksimum kamera dari setting cameraFar
            double targetFar = freelookMod.cameraFar.get();
            double maxDistance = targetFar;

            if (FreelookPlus.isTransitioning) {
                double durationSec = FreelookPlus.isEntering ? freelookMod.fadeOutDuration.get() : freelookMod.fadeInDuration.get();
                long durationMs = (long) (durationSec * 1000.0);
                long elapsed = System.currentTimeMillis() - FreelookPlus.transitionStartTime;

                float t = durationMs > 0 ? (float) elapsed / durationMs : 1.0f;

                if (t >= 1.0f) {
                    t = 1.0f;
                    FreelookPlus.isTransitioning = false;
                    if (!FreelookPlus.isEntering) {
                        freelookMod.finishDeactivation();
                        return;
                    }
                }

                float smoothT = (float) Math.sin(t * Math.PI / 2);
                float yawDelta = MathHelper.wrapDegrees(targetYaw - startYaw);
                float pitchDelta = targetPitch - startPitch;

                if (FreelookPlus.isEntering) {
                    currentYaw = startYaw + (yawDelta * smoothT);
                    currentPitch = startPitch + (pitchDelta * smoothT);
                    maxDistance = targetFar * smoothT;
                } else {
                    currentYaw = targetYaw - (yawDelta * smoothT);
                    currentPitch = targetPitch - (pitchDelta * smoothT);
                    maxDistance = targetFar * (1.0f - smoothT);
                }
            }

            setRotation(currentYaw, currentPitch);

            Vec3d eyePos = focusedEntity.getCameraPosVec(tickDelta);
            Vec3d dir = Vec3d.fromPolar(currentPitch, currentYaw);

            if (maxDistance <= 0.05D) {
                setPos(eyePos);
                return;
            }

            Vec3d desiredCamPos = eyePos.subtract(dir.multiply(maxDistance));

            if (freelookMod.cameraClip.get()) {
                setPos(desiredCamPos);
                return;
            }

            BlockHitResult hitResult = world.raycast(new RaycastContext(
                eyePos,
                desiredCamPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                focusedEntity
            ));

            double actualDistance = maxDistance;
            if (hitResult.getType() != HitResult.Type.MISS) {
                actualDistance = hitResult.getPos().distanceTo(eyePos) - 0.1D;
                if (actualDistance < 0.05D) actualDistance = 0.05D;
            }

            setPos(eyePos.subtract(dir.multiply(actualDistance)));
        }
    }
}
