package com.ave.addon.mixins;

import com.ave.addon.modules.FreecamPlus;
import net.minecraft.client.input.KeyboardInput; // Ganti ke subclass KeyboardInput
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class) // Target langsung ke kelas input keyboard
public abstract class ClientPlayerEntityMixin {

    @Inject(method = {"tick", "method_3129"}, at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (FreecamPlus.isActive) {
            KeyboardInput input = (KeyboardInput) (Object) this;

            // Paksa reset semua status tombol di akhir proses tick keyboard
            input.playerInput = new PlayerInput(false, false, false, false, false, false, false);
        }
    }
}
