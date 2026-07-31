package com.ave.addon.mixins;

import com.ave.addon.modules.AntiTrap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow public HitResult crosshairTarget;

    // Untuk klik kiri (Attack): Masih aman dibatalkan return valuenya agar gak mukul angin/hitbox ghaib
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) crosshairTarget;
            if (AntiTrap.shouldHideEntity(entityHit.getEntity())) {
                cir.setReturnValue(false);
            }
        }
    }

    // Untuk klik kanan (Interact/Use Item): Kita manipulasi targetnya tepat sebelum eksekusi
    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void onDoItemUse(CallbackInfo ci) {
        if (crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) crosshairTarget;

            if (AntiTrap.shouldHideEntity(entityHit.getEntity())) {
                // Di sini triknya! Kita ubah tipe target crosshair sementara menjadi MISS.
                // Dengan begini, Minecraft akan mengabaikan interaksi ke Armor Stand,
                // dan langsung mengeksekusi fungsi lempar Ender Pearl yang dipegang tangan lu!
                crosshairTarget = new BlockHitResult(entityHit.getPos(), Direction.UP, BlockPos.ORIGIN, false);
            }
        }
    }
}
