package com.aclient.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class FreecamModule {
    private boolean active = false;
    private float speed = 1.00f;
    private float sprintSpeed = 2.25f;
    private float sensitivity = 1.00f; // Sensi default sekarang 1.00f
    private boolean turnOffOnDimensionChange = true;
    private boolean turnOffOnDamage = true;
    private boolean chatNotification = true;
    private int keybind = 0;

    private float currentAlpha = 0.0f;
    private boolean keybindPressedLastTick = false;

    private Vec3d cameraPos = Vec3d.ZERO;
    private float freecamYaw = 0.0f;
    private float freecamPitch = 0.0f;
    private Vec3d velocity = Vec3d.ZERO;

    private float playerYaw = 0.0f;
    private float playerPitch = 0.0f;

    public void toggle(MinecraftClient client) {
        this.active = !this.active;
        if (client.player != null && chatNotification) {
            client.player.sendMessage(Text.literal("§7[§aAClient§7]: §fFreecam Turn " + (active ? "§aOn" : "§cOff")), false);
        }
        if (active && client.player != null) {
            this.playerYaw = client.player.getYaw();
            this.playerPitch = client.player.getPitch();

            this.cameraPos = new Vec3d(client.player.getX(), client.player.getY() + client.player.getStandingEyeHeight(), client.player.getZ());
            this.freecamYaw = this.playerYaw;
            this.freecamPitch = this.playerPitch;
            this.velocity = Vec3d.ZERO;
        } else if (!active && client.player != null) {
            client.player.setVelocity(Vec3d.ZERO);
        }
    }

    public void disableOnDimensionChange(MinecraftClient client) {
        if (!this.active) return;
        this.active = false;
        this.velocity = Vec3d.ZERO;
        if (client.player != null && chatNotification) {
            client.player.sendMessage(Text.literal("§7[§aAClient§7]: §fFreecam Turn §cOff (Safety Shutdown)"), false);
        }
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        this.freecamYaw += (float) (deltaX * this.sensitivity);
        this.freecamPitch += (float) (deltaY * this.sensitivity);
        this.freecamPitch = Math.max(-90.0f, Math.min(90.0f, this.freecamPitch));
    }

    public void onClientTick(MinecraftClient client) {
        if (!active || client.player == null) return;
        client.player.setYaw(this.playerYaw);
        client.player.setPitch(this.playerPitch);
        client.player.setHeadYaw(this.playerYaw);
        client.player.setBodyYaw(this.playerYaw);
    }

    public void onRenderTick(MinecraftClient client) {
        if (!active || client.player == null) return;

        float yawRad = (float) Math.toRadians(this.freecamYaw);
        long window = client.getWindow().getHandle();

        boolean isSprinting = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS);

        double currentCalculatedSpeed = this.speed;
        if (isSprinting) {
            currentCalculatedSpeed += this.sprintSpeed;
        }

        double moveSpeed = (currentCalculatedSpeed * 0.05D);
        double forwardX = -Math.sin(yawRad) * moveSpeed;
        double forwardZ = Math.cos(yawRad) * moveSpeed;
        double strafeX = Math.cos(yawRad) * moveSpeed;
        double strafeZ = Math.sin(yawRad) * moveSpeed;

        Vec3d wishDir = Vec3d.ZERO;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) wishDir = wishDir.add(forwardX, 0, forwardZ);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) wishDir = wishDir.subtract(forwardX, 0, forwardZ);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) wishDir = wishDir.add(strafeX, 0, strafeZ);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) wishDir = wishDir.subtract(strafeX, 0, strafeZ);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) wishDir = wishDir.add(0, moveSpeed, 0);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) wishDir = wishDir.subtract(0, moveSpeed, 0);

        double lerpFactor = 0.15D;
        this.velocity = new Vec3d(
                this.velocity.x + (wishDir.x - this.velocity.x) * lerpFactor,
                this.velocity.y + (wishDir.y - this.velocity.y) * lerpFactor,
                this.velocity.z + (wishDir.z - this.velocity.z) * lerpFactor
        );

        this.cameraPos = this.cameraPos.add(this.velocity);
    }

    public void resetAll() {
        this.speed = 1.00f;
        this.sprintSpeed = 2.25f;
        this.sensitivity = 1.00f;
        this.turnOffOnDimensionChange = true;
        this.turnOffOnDamage = true;
        this.chatNotification = true;
        this.velocity = Vec3d.ZERO;
    }

    // Getters & Setters ... (biarkan sisa getter/setter di bawahnya tetap sama)
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getSprintSpeed() { return sprintSpeed; }
    public void setSprintSpeed(float speed) { this.sprintSpeed = speed; }
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