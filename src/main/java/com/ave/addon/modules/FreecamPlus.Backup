package com.ave.addon.modules;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class FreecamPlus extends Module {
    public static double cameraX, cameraY, cameraZ;
    public static float cameraYaw, cameraPitch;

    public static double targetX, targetY, targetZ;
    public static float targetTextYaw, targetTextPitch;

    public static boolean isActive = false;
    public static boolean isReturning = false;

    public static final float RETURN_SPEED = 0.15f;
    private RegistryKey<World> lastDimension;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgUtility = settings.createGroup("Utility");

    public final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Freecam movement speed.")
        .defaultValue(0.85)
        .min(0.00)
        .max(25.00)
        .sliderMax(25.00)
        .build()
    );

    public final Setting<Double> sprintSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("sprint-speed")
        .description("Freecam speed while holding Left Ctrl.")
        .defaultValue(1.25)
        .min(0.00)
        .max(25.00)
        .sliderMax(25.00)
        .build()
    );

    public final Setting<Double> smoothFactor = sgGeneral.add(new DoubleSetting.Builder()
        .name("smoothness")
        .description("Roblox-like tween smoothness value. Lower = smoother.")
        .defaultValue(0.12)
        .min(0.01)
        .max(1.00)
        .sliderMax(1.00)
        .build()
    );

    public final Setting<Boolean> disableOnDimensionChange = sgUtility.add(new BoolSetting.Builder()
        .name("disable-on-dimension-change")
        .description("Automatically disables Freecam after the player enters a different dimension.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> disableOnDamage = sgUtility.add(new BoolSetting.Builder()
        .name("disable-on-damage")
        .description("Disables Freecam when the player takes any damage.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> disableOnPlayerDetection = sgUtility.add(new BoolSetting.Builder()
        .name("disable-on-player-detection")
        .description("Disables Freecam when another player is detected.")
        .defaultValue(false)
        .build()
    );

    public final Setting<String> customChat = sgUtility.add(new StringSetting.Builder()
        .name("custom-chat")
        .description("Displays a customizable chat notification when one or more players are detected.")
        .defaultValue("A player was detected: %player_name%")
        .build()
    );

    public final Setting<Boolean> cinematicReturn = sgUtility.add(new BoolSetting.Builder()
        .name("cinematic-return")
        .description("Smoothly glides the camera back to the player on deactivation.")
        .defaultValue(false)
        .build()
    );

    public FreecamPlus(Category category) {
        super(category, "Freecam+", "Freecam like Krypton Client!");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) return;

        if (FreelookPlus.isActive) {
            warning("Please disable Freelook+ first!");
            toggle();
            return;
        }

        resetPositionsToPlayer();

        lastDimension = mc.world.getRegistryKey();
        isReturning = false;
        isActive = true;
    }

    @Override
    public void onDeactivate() {
        if (cinematicReturn.get() && mc.player != null) {
            isReturning = true;
        } else {
            isActive = false;
            isReturning = false;
        }
    }

    // RESET MUTLAK SAAT PLAYER JOIN SERVER/WORLD BARU
    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        forceResetModule();
    }

    // RESET SAAT LEAVE SERVER
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        forceResetModule();
    }

    private void forceResetModule() {
        isActive = false;
        isReturning = false;
        if (this.isActive()) {
            this.toggle(); // Matikan saklar modul Meteor Client jika masih 'ON'
        }
    }

    private void resetPositionsToPlayer() {
        if (mc.player == null) return;

        targetX = mc.player.getX();
        targetY = mc.player.getEyeY();
        targetZ = mc.player.getZ();

        cameraX = targetX;
        cameraY = targetY;
        cameraZ = targetZ;

        targetTextYaw = mc.player.getYaw();
        targetTextPitch = mc.player.getPitch();

        cameraYaw = targetTextYaw;
        cameraPitch = targetTextPitch;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || !isActive) return;

        if (isReturning) return;

        if (disableOnDimensionChange.get() && mc.world.getRegistryKey() != lastDimension) {
            forceResetModule();
            return;
        }

        if (disableOnDamage.get() && mc.player.hurtTime > 0) {
            toggle();
            return;
        }

        if (disableOnPlayerDetection.get()) {
            List<String> detectedPlayers = new ArrayList<>();
            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;
                detectedPlayers.add(player.getName().getString());
            }

            if (!detectedPlayers.isEmpty()) {
                String playerList = String.join(", ", detectedPlayers);
                String formattedMessage = customChat.get().replace("%player_name%", playerList);
                info(formattedMessage);
                toggle();
                return;
            }
        }

        updateCameraMovement();
    }

    private void updateCameraMovement() {
        if (isReturning || mc.currentScreen != null || mc.getWindow() == null) return;

        double baseSpeed = speed.get();
        boolean isSprinting = InputUtil.isKeyPressed(mc.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);

        double totalSpeed = baseSpeed;
        if (isSprinting) {
            totalSpeed += sprintSpeed.get();
        }

        double moveSpeed = totalSpeed * 0.25;

        double radYaw = Math.toRadians(targetTextYaw);
        double sinYaw = Math.sin(radYaw);
        double cosYaw = Math.cos(radYaw);

        if (mc.options.forwardKey.isPressed()) {
            targetX += -sinYaw * moveSpeed;
            targetZ += cosYaw * moveSpeed;
        }
        if (mc.options.backKey.isPressed()) {
            targetX -= -sinYaw * moveSpeed;
            targetZ -= cosYaw * moveSpeed;
        }
        if (mc.options.leftKey.isPressed()) {
            targetX += cosYaw * moveSpeed;
            targetZ += sinYaw * moveSpeed;
        }
        if (mc.options.rightKey.isPressed()) {
            targetX -= cosYaw * moveSpeed;
            targetZ -= sinYaw * moveSpeed;
        }
        if (mc.options.jumpKey.isPressed()) {
            targetY += moveSpeed;
        }
        if (mc.options.sneakKey.isPressed()) {
            targetY -= moveSpeed;
        }
    }
}
