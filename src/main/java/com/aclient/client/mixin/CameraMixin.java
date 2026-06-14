package com.aclient.mixin;

import com.aclient.client.AClientClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World; // FIX: Import World instead of BlockView
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow private Vec3d pos;
    @Shadow private float pitch;
    @Shadow private float yaw;

    @Inject(method = "update", at = @At("RETURN"))
    // FIX: Swapped out BlockView parameter for World to align cleanly with 1.21.11 method descriptor expectations
    private void onUpdate(World world, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (AClientClient.FREECAM.isActive()) {
            // Decouple camera coordinates to use the isolated fly vector
            this.pos = AClientClient.FREECAM.getCameraPos();

            // Camera orientation behavior matches configuration setup
            if (AClientClient.FREECAM.getSensitivity() > 0.00f) {
                this.pitch = AClientClient.FREECAM.getFreecamPitch();
                this.yaw = AClientClient.FREECAM.getFreecamYaw();
            }
        }
    }
}