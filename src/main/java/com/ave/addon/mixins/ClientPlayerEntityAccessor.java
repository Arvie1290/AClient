package com.ave.addon.mixins;

import net.minecraft.entity.Entity; // Import Entity kelas induk
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class) // Ganti target ke Entity.class agar bisa nemu field yaw & pitch
public interface ClientPlayerEntityAccessor {
    @Accessor("yaw")
    void setYaw(float yaw);

    @Accessor("pitch")
    void setPitch(float pitch);

    @Accessor("yaw")
    float getYaw();

    @Accessor("pitch")
    float getPitch();
}
