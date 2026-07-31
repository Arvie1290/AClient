package com.ave.addon.mixins;

import com.ave.addon.modules.FreelookPlus;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class FreelookRaycastMixin {

    // Overrides rotation vector generation for raycasting (hit detection & block placing)
    // to use the locked physical body angles when FreelookPlus is active.
    @Inject(method = "getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void onGetRotationVector(float pitch, float yaw, CallbackInfoReturnable<Vec3d> cir) {
        if (FreelookPlus.isActive) {
            // Force raycast calculations to use locked player body orientation
            float lockedPitch = FreelookPlus.lockedPlayerPitch;
            float lockedYaw = FreelookPlus.lockedPlayerYaw;

            float f = lockedPitch * 0.017453292F;
            float g = -lockedYaw * 0.017453292F;
            float h = (float) Math.cos(g);
            float i = (float) Math.sin(g);
            float j = (float) Math.cos(f);
            float k = (float) Math.sin(f);

            cir.setReturnValue(new Vec3d(i * j, -k, h * j));
        }
    }
}
