package com.ave.addon.modules;

import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;

public class HandViewPlusModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Configuration Slider: Swing Duration (Ticks)
    // Vanilla Minecraft Default = 6 ticks (~0.3s)
    // Max 100 ticks = ~5 seconds per swing!
    private final Setting<Integer> swingDuration = sgGeneral.add(new IntSetting.Builder()
        .name("swing-duration")
        .description("Hand swing animation duration in ticks. Vanilla default = 6. Higher values create smooth slow-motion.")
        .defaultValue(6)
        .min(1)
        .sliderMin(1)
        .sliderMax(6767)
        .max(6767) // Allows typing up to 200 manually (~10 seconds)
        .build()
    );

    public HandViewPlusModule(Category category) {
        super(category, "hand-view+", "Slow down your hand animation even further! (For more features, use Hand View module)");
    }

    /**
     * Getter called by LivingEntityMixin
     */
    public int getSwingDuration() {
        return swingDuration.get();
    }
}
