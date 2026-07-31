package com.ave.addon.mixins;

import com.ave.addon.modules.AntiTrap;
import com.ave.addon.modules.FreecamPlus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (AntiTrap.shouldHideEntity(entity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = {"changeLookDirection", "method_5735"}, at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double x, double y, CallbackInfo ci) {
        if (FreecamPlus.isActive && (Object) this == MinecraftClient.getInstance().player) {
            FreecamPlus.targetTextYaw += (float) x * 0.15F;
            FreecamPlus.targetTextPitch += (float) y * 0.15F;

            if (FreecamPlus.targetTextPitch > 90.0F) FreecamPlus.targetTextPitch = 90.0F;
            if (FreecamPlus.targetTextPitch < -90.0F) FreecamPlus.targetTextPitch = -90.0F;

            ci.cancel();
        }
    }

    // Kunci total bodi asli agar tidak bisa melakukan visualisasi animasi jongkok
    @Inject(method = {"isSneaking", "method_5715"}, at = @At("HEAD"), cancellable = true)
    private void onIsSneaking(CallbackInfoReturnable<Boolean> cir) {
        if (FreecamPlus.isActive && (Object) this == MinecraftClient.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
