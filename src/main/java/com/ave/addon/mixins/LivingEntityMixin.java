package com.ave.addon.mixins;

import com.ave.addon.modules.FreecamPlus;
import com.ave.addon.modules.HandViewPlusModule;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * FreecamPlus Integration:
     * Zeroes keyboard movement input while Freecam is active without breaking gravity.
     */
    @ModifyVariable(method = {"travel", "method_6091"}, at = @At("HEAD"), argsOnly = true)
    private Vec3d modifyMovementInput(Vec3d movementInput) {
        if (FreecamPlus.isActive && (Object) this == MinecraftClient.getInstance().player) {
            return Vec3d.ZERO;
        }
        return movementInput;
    }

    /**
     * HandViewPlusModule Integration:
     * Overrides hand swing duration natively via Minecraft's getHandSwingDuration method.
     */
    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void onGetHandSwingDuration(CallbackInfoReturnable<Integer> info) {
        if (Modules.get().isActive(HandViewPlusModule.class)) {
            HandViewPlusModule module = Modules.get().get(HandViewPlusModule.class);
            if (module != null) {
                info.setReturnValue(module.getSwingDuration());
            }
        }
    }
}
