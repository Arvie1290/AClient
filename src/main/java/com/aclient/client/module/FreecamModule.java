package com.aclient.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FreecamModule {
    private boolean active = false;

    // Configuration Fields
    private float speed = 1.00f;
    private float sensitivity = 0.00f;
    private boolean turnOffOnDimensionChange = true;
    private boolean turnOffOnDamage = true;
    private int keybind = GLFW.GLFW_KEY_UNKNOWN; // Not Bound

    // Animation alpha tracking (0.0f to 0.25f)
    private float currentAlpha = 0.0f;
    private boolean keybindPressedLastTick = false;

    public void toggle(MinecraftClient client) {
        this.active = !this.active;
        if (client.player != null) {
            client.player.sendMessage(Text.of("§aFreecam status: " + (active ? "§2ON" : "§4OFF")), true);
        }
    }

    public void onTick(MinecraftClient client) {
        if (!active || client.player == null) return;
        // Basic Freecam behavior tracking can be injected here or via Mixins
        // depending on whether you spawn a fake player to trick your paper anti-cheat
    }

    public void resetAll() {
        this.speed = 1.00f;
        this.sensitivity = 0.00f;
        this.turnOffOnDimensionChange = true;
        this.turnOffOnDamage = true;
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
    public int getKeybind() { return keybind; }
    public void setKeybind(int keybind) { this.keybind = keybind; }
    public float getCurrentAlpha() { return currentAlpha; }
    public void setCurrentAlpha(float alpha) { this.currentAlpha = alpha; }
    public boolean isKeybindPressedLastTick() { return keybindPressedLastTick; }
    public void setKeybindPressedLastTick(boolean b) { this.keybindPressedLastTick = b; }
}