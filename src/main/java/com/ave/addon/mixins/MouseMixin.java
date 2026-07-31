package com.ave.addon.mixins;

import com.ave.addon.modules.FreelookPlus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;

    @Inject(
        method = "updateMouse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"),
        cancellable = true
    )
    private void onTurn(CallbackInfo ci) {
        if (FreelookPlus.isActive && MinecraftClient.getInstance().currentScreen == null) {
            FreelookPlus.updateCameraRotation(this.cursorDeltaX, this.cursorDeltaY);
            ci.cancel(); // Cegah badan player berputar
        }
    }
}
