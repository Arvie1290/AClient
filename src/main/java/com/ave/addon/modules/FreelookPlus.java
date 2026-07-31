package com.ave.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class FreelookPlus extends Module {
    public static boolean isActive;
    public static boolean isTransitioning;

    public static float cameraYaw;
    public static float cameraPitch;

    public static float lockedPlayerYaw;
    public static float lockedPlayerPitch;

    public static long transitionStartTime;
    public static boolean isEntering = false;

    private Perspective prevPerspective;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgUtility = settings.createGroup("Utility");

    public final Setting<Double> sensitivity = sgGeneral.add(new DoubleSetting.Builder()
        .name("sensitivity")
        .description("Camera rotation sensitivity while freelook is active.")
        .defaultValue(0.15)
        .min(0.01)
        .max(10.00)
        .sliderMax(10.00)
        .build()
    );

    public final Setting<Boolean> fadeOut = sgGeneral.add(new BoolSetting.Builder()
        .name("fade-out")
        .description("Smoothly glides the camera out from the player's body on activation.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> fadeOutDuration = sgGeneral.add(new DoubleSetting.Builder()
        .name("fade-out-duration")
        .description("Duration of the fade-out camera transition in seconds.")
        .defaultValue(0.25)
        .min(0.05)
        .max(2.00)
        .sliderMax(1.00)
        .build()
    );

    public final Setting<Boolean> fadeIn = sgGeneral.add(new BoolSetting.Builder()
        .name("fade-in")
        .description("Smoothly glides the camera back to align with the player on deactivation.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> fadeInDuration = sgGeneral.add(new DoubleSetting.Builder()
        .name("fade-in-duration")
        .description("Duration of the fade-in camera transition in seconds.")
        .defaultValue(0.25)
        .min(0.05)
        .max(2.00)
        .sliderMax(1.00)
        .build()
    );

    public final Setting<Boolean> cameraClip = sgUtility.add(new BoolSetting.Builder()
        .name("camera-clip")
        .description("Allows the camera to pass through blocks and walls freely.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> cameraFar = sgUtility.add(new DoubleSetting.Builder()
        .name("camera-far")
        .description("Custom distance for the Freelook+ camera in blocks.")
        .defaultValue(4.00)
        .min(1.00)
        .max(50.00)
        .sliderMax(25.00)
        .build()
    );

    public FreelookPlus(Category category) {
        super(category, "Freelook+", "Locks player body rotation while allowing free 360 camera movement with smooth transitions.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        // --- SAFETY CHECK ---
        if (FreecamPlus.isActive) {
            warning("§7[ §cAVE§7 ]: Please disable Freecam+ first!");
            toggle(); // Batalkan aktivasi
            return;
        }

        lockedPlayerYaw = mc.player.getYaw();
        lockedPlayerPitch = mc.player.getPitch();

        cameraYaw = lockedPlayerYaw;
        cameraPitch = lockedPlayerPitch;

        prevPerspective = mc.options.getPerspective();
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);

        transitionStartTime = System.currentTimeMillis();
        if (fadeOut.get()) {
            isTransitioning = true;
            isEntering = true;
        } else {
            isTransitioning = false;
        }

        isActive = true;
    }

    @Override
    public void onDeactivate() {
        if (fadeIn.get() && mc.player != null) {
            transitionStartTime = System.currentTimeMillis();
            isTransitioning = true;
            isEntering = false;
        } else {
            finishDeactivation();
        }
    }

    public void finishDeactivation() {
        isActive = false;
        isTransitioning = false;

        if (mc.options != null) {
            if (prevPerspective != null) {
                mc.options.setPerspective(prevPerspective);
            } else {
                mc.options.setPerspective(Perspective.FIRST_PERSON);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || !isActive) return;

        // Kunci arah fisik badan player
        mc.player.setYaw(lockedPlayerYaw);
        mc.player.setPitch(lockedPlayerPitch);
    }

    public static void updateCameraRotation(double deltaX, double deltaY) {
        if (!isActive) return;

        FreelookPlus module = meteordevelopment.meteorclient.systems.modules.Modules.get().get(FreelookPlus.class);
        double sens = module != null ? module.sensitivity.get() * 0.15 : 0.0225;

        cameraYaw += (float) (deltaX * sens);
        cameraPitch += (float) (deltaY * sens);
        cameraPitch = MathHelper.clamp(cameraPitch, -90.0f, 90.0f);
    }
}
