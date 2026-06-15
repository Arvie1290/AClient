package com.aclient.client.mixin;

import com.aclient.client.AClientClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (AClientClient.FREECAM.isActive()) {
            KeyboardInput input = (KeyboardInput) (Object) this;
            // Melumpuhkan pergerakan tubuh fisik secara total di mesin input dasar
            input.playerInput = PlayerInput.DEFAULT;
        }
    }
}