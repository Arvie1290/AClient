package com.ave.addon.modules;

import java.util.Set;
import com.google.common.collect.Sets;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.client.MinecraftClient;

public class AntiTrap extends Module {
    public enum Mode {
        HideAndShow("Hide and Show"),
        DestroyEntities("Destroy Entities");

        private final String title;
        Mode(String title) { this.title = title; }
        @Override public String toString() { return title; }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Choose how to handle the targeted entities.")
        .defaultValue(Mode.HideAndShow)
        .build()
    );

    private final Setting<Boolean> allEntitiesExceptPlayer = sgGeneral.add(new BoolSetting.Builder()
        .name("all-entities-except-player")
        .description("Hide or destroy all entities except players.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Set<EntityType<?>>> specificEntities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("specific-entities")
        .description("Select specific entities to target.")
        .defaultValue(Sets.newHashSet(EntityType.ARMOR_STAND, EntityType.CHEST_MINECART))
        .visible(() -> !allEntitiesExceptPlayer.get())
        .build()
    );

    public AntiTrap(Category category) {
        super(category, "anti-trap-+", "Locally hides or destroys trap entities to clear your view.");
    }

    // Method pembantu static untuk di-inject ke Mixin Renderer
    public static boolean shouldHideEntity(Entity entity) {
        AntiTrap antiTrapMod = Modules.get().get(AntiTrap.class);
        if (antiTrapMod == null || !antiTrapMod.isActive()) return false;

        // Jika mode Destroy aktif, kita tetap biarkan dia lewat jalur remove standar (jika lu mau tetep pake destroy)
        if (antiTrapMod.mode.get() == Mode.DestroyEntities) return false;

        return antiTrapMod.shouldTarget(entity);
    }

    // Method pembantu static khusus untuk mode Destroy
    public static boolean shouldDestroyEntity(Entity entity) {
        AntiTrap antiTrapMod = Modules.get().get(AntiTrap.class);
        if (antiTrapMod == null || !antiTrapMod.isActive()) return false;
        return antiTrapMod.mode.get() == Mode.DestroyEntities && antiTrapMod.shouldTarget(entity);
    }

    // Hanya mode Destroy yang butuh looping tick untuk membersihkan world secara paksa
    @meteordevelopment.orbit.EventHandler
    private void onTick(meteordevelopment.meteorclient.events.world.TickEvent.Pre event) {
        if (mc.world == null || mode.get() != Mode.DestroyEntities) return;

        for (Entity entity : mc.world.getEntities()) {
            if (shouldDestroyEntity(entity)) {
                mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
            }
        }
    }

    private boolean shouldTarget(Entity entity) {
        if (entity == MinecraftClient.getInstance().player) return false;

        if (allEntitiesExceptPlayer.get()) {
            return entity.getType() != EntityType.PLAYER;
        }

        return specificEntities.get().contains(entity.getType());
    }
}
