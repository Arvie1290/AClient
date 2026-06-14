package com.aclient.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.PlayerInput;
import org.lwjgl.glfw.GLFW;

public class FreecamModule {
    private boolean active = false;
    private float speed = 1.00f;
    private float sensitivity = 0.00f;
    private boolean turnOffOnDimensionChange = true;
    private boolean turnOffOnDamage = true;
    private boolean chatNotification = true;
    private int keybind = 0; // Not Bound default state

    private float currentAlpha = 0.0f;
    private boolean keybindPressedLastTick = false;

    private Vec3d cameraPos = Vec3d.ZERO;
    private float freecamYaw = 0.0f;
    private float freecamPitch = 0.0f;

    // --- SMOOTH MOMENTUM ENGINE VECTORS ---
    private Vec3d velocity = Vec3d.ZERO;

    public void toggle(MinecraftClient client) {
        this.active = !this.active;

        if (client.player != null && chatNotification) {
            client.player.sendMessage(Text.literal("§7[§aAClient§7]: §fFreecam Turn " + (active ? "§aOn" : "§cOff")), false);
        }

        if (active && client.player != null) {
            this.cameraPos = new Vec3d(client.player.getX(), client.player.getY() + client.player.getStandingEyeHeight(), client.player.getZ());
            this.freecamYaw = client.player.getYaw();
            this.freecamPitch = client.player.getPitch();
            this.velocity = Vec3d.ZERO; // Reset kinetic energy on activation
        }
    }

    public void onTick(MinecraftClient client) {
        if (!active || client.player == null) return;

        // 1. PHYSICAL BODY IMMOBILIZER GUARD
        if (client.player.input != null) {
            client.player.input.playerInput = PlayerInput.DEFAULT;
        }

        // 2. CAMERA ORIENTATION TRACKING
        if (this.sensitivity > 0.00f) {
            this.freecamYaw = client.player.getYaw();
            this.freecamPitch = client.player.getPitch();
        }

        // 3. DIRECTIONAL MATRICES SETUP
        float yawRad = (float) Math.toRadians(client.player.getYaw());
        double moveSpeed = (this.speed * 0.35D);

        double forwardX = -Math.sin(yawRad) * moveSpeed;
        double forwardZ = Math.cos(yawRad) * moveSpeed;
        double strafeX = Math.cos(yawRad) * moveSpeed;
        double strafeZ = Math.sin(yawRad) * moveSpeed;

        long window = client.getWindow().getHandle();
        Vec3d wishDir = Vec3d.ZERO;

        // 4. GENERATE TARGET INTENTION VECTOR (WITH INPUT INVERSIONS)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) wishDir = wishDir.add(forwardX, 0, forwardZ);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) wishDir = wishDir.subtract(forwardX, 0, forwardZ);

        // FIX: Inverted Strafe Controls Requirement
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) wishDir = wishDir.add(strafeX, 0, strafeZ);      // Pressing A moves Camera RIGHT
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) wishDir = wishDir.subtract(strafeX, 0, strafeZ); // Pressing D moves Camera LEFT

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) wishDir = wishDir.add(0, moveSpeed, 0);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) wishDir = wishDir.subtract(0, moveSpeed, 0);

        // 5. VELOCITY INTERPOLATION (SMOOTH ACCELERATION & DRIFT FRICTION)
        // Adjust 0.18D to tune momentum (Lower = more drift slider glide, Higher = snappy halts)
        double lerpFactor = 0.18D;
        this.velocity = new Vec3d(
                this.velocity.x + (wishDir.x - this.velocity.x) * lerpFactor,
                this.velocity.y + (wishDir.y - this.velocity.y) * lerpFactor,
                this.velocity.z + (wishDir.z - this.velocity.z) * lerpFactor
        );

        // Apply updated kinetic vectors onto camera coordinate maps
        this.cameraPos = this.cameraPos.add(this.velocity);
    }

    public void resetAll() {
        this.speed = 1.00f;
        this.sensitivity = 0.00f;
        this.turnOffOnDimensionChange = true;
        this.turnOffOnDamage = true;
        this.chatNotification = true;
        this.velocity = Vec3d.ZERO;
    }

    // Getters & Setters
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getSensitivity() { return sensitivity; }
    public void setSensitivity(float sensitivity) { this.sensitivity = sensitivity; }
    public boolean isTurnOffOnDimensionChange() { return turnOffOnDimensionChange; }
    public void setTurnOffOnDimensionChange(boolean val) { this.turnOffOnDimensionChange = val; }
    public boolean isTurnOffOnDamage() { return turnOffOnDamage; }
    public void setTurnOffOnDamage(boolean val) { this.turnOffOnDamage = val; }
    public boolean isChatNotification() { return chatNotification; }
    public void setChatNotification(boolean val) { this.chatNotification = val; }
    public int getKeybind() { return keybind; }
    public void setKeybind(int keybind) { this.keybind = keybind; }
    public float getCurrentAlpha() { return currentAlpha; }
    public void setCurrentAlpha(float alpha) { this.currentAlpha = alpha; }
    public boolean isKeybindPressedLastTick() { return keybindPressedLastTick; }
    public void setKeybindPressedLastTick(boolean b) { this.keybindPressedLastTick = b; }
    public Vec3d getCameraPos() { return cameraPos; }
    public float getFreecamYaw() { return freecamYaw; }
    public float getFreecamPitch() { return freecamPitch; }
}