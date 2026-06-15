package com.aclient.client.mixin;

import com.aclient.client.AClientClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3d zeroFreecamTravelInput(Vec3d movementInput) {
        // Bypass aman: Bandingkan langsung objek ini dengan player lokal client
        if ((Object) this == MinecraftClient.getInstance().player && AClientClient.FREECAM.isActive()) {
            return Vec3d.ZERO; // Potong input gerak jadi nol mutlak saat freecam aktif!
        }
        return movementInput;
    }
}