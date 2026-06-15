package com.aclient.client.mixin;

import com.aclient.client.AClientClient;
import net.minecraft.entity.Entity;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double x, double y, CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            if (AClientClient.FREECAM.isActive()) {
                // Kirim pergerakan mouse langsung ke rotasi kamera freecam
                AClientClient.FREECAM.changeLookDirection(x, y);
                ci.cancel(); // Kunci badan player asli agar tidak ikut berputar
            }
        }
    }
}