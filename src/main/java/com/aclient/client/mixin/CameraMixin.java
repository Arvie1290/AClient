package com.aclient.client.mixin;

import com.aclient.client.AClientClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Vec3d pos;
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdateHead(World world, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (AClientClient.FREECAM.isActive()) {
            AClientClient.FREECAM.onRenderTick(MinecraftClient.getInstance());
        }
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdateReturn(World world, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (AClientClient.FREECAM.isActive()) {
            this.pos = AClientClient.FREECAM.getCameraPos();
            this.setRotation(AClientClient.FREECAM.getFreecamYaw(), AClientClient.FREECAM.getFreecamPitch());
        }
    }

    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void onIsThirdPerson(CallbackInfoReturnable<Boolean> cir) {
        if (AClientClient.FREECAM.isActive()) {
            cir.setReturnValue(true);
        }
    }
}