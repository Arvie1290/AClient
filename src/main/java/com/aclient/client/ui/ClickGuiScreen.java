package com.aclient.client.ui;

import com.aclient.client.AClientClient;
import com.aclient.client.module.FreecamModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen {
    private boolean showConfig = false;
    private boolean listeningForKeybind = false;

    // Draggable Slider States
    private boolean draggingSpeed = false;
    private boolean draggingSensitivity = false;

    public ClickGuiScreen() {
        super(Text.of("AClient Click GUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        FreecamModule module = AClientClient.FREECAM;

        // --- ANIMATION ENGINE ---
        float alpha = module.getCurrentAlpha();
        if (module.isActive() && alpha < 0.25f) {
            alpha = Math.min(0.25f, alpha + (delta * 0.05f));
        } else if (!module.isActive() && alpha > 0.0f) {
            alpha = Math.max(0.0f, alpha - (delta * 0.05f));
        }
        module.setCurrentAlpha(alpha);

        // --- MAIN PANEL COORDINATES ---
        int mainX = this.width / 2 - 50;
        int mainY = this.height / 2 - 60;
        int mainWidth = 100;
        int mainHeight = 40;

        // Visual category box
        context.fill(mainX, mainY, mainX + mainWidth, mainY + 14, 0xFF202020);
        context.drawCenteredTextWithShadow(this.textRenderer, "Visual", mainX + (mainWidth / 2), mainY + 3, 0xFFFFFFFF);

        // Module Base Button
        int btnY = mainY + 16;
        context.fill(mainX, btnY, mainX + mainWidth, mainY + mainHeight, 0xFF101010);
        if (alpha > 0) {
            int argbColor = ((int)(alpha * 255) << 24) | 0x0000FF00;
            context.fill(mainX, btnY, mainX + mainWidth, mainY + mainHeight, argbColor);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, "Freecam", mainX + (mainWidth / 2), btnY + 5, 0xFFFFFFFF);

        // --- CONFIGURATION SIDEBAR ---
        if (showConfig) {
            int cfgX = mainX - 170;
            int cfgY = mainY - 20;
            int cfgW = 160;
            int cfgH = 180;

            // Base Panel Background
            context.fill(cfgX, cfgY, cfgX + cfgW, cfgY + cfgH, 0xDD050505);

            // Header Texts
            context.drawTextWithShadow(this.textRenderer, "Freecam Configuration", cfgX + 5, cfgY + 6, 0xFFFF5555);

            context.getMatrices().push();
            context.getMatrices().translate(cfgX + 5, cfgY + 18, 0);
            context.getMatrices().scale(0.75f, 0.75f, 1.0f);
            context.drawTextWithShadow(this.textRenderer, "Can make your camera float anywhere!", 0, 0, 0xFFAAAAAA);
            context.getMatrices().pop();

            // Slider Geometry Variables
            int sliderX = cfgX + 8;
            int sliderW = 144;

            // --- 1. SMOOTH SPEED SLIDER LOGIC & RENDERING ---
            int speedY = cfgY + 44;
            if (this.draggingSpeed) {
                float pct = (float) (mouseX - sliderX) / sliderW;
                pct = Math.max(0.0f, Math.min(1.0f, pct));
                // Scale math from 0.01 to 50.00
                float newSpeed = 0.01f + pct * (50.00f - 0.01f);
                module.setSpeed(newSpeed);
            }

            context.drawTextWithShadow(this.textRenderer, "Freecam Speed:", cfgX + 5, cfgY + 32, 0xFFFFFFFF);
            // Draw [TextBox] value representation
            context.fill(cfgX + 116, cfgY + 30, cfgX + 152, cfgY + 41, 0xFF151515);
            context.drawTextWithShadow(this.textRenderer, String.format("%.2f", module.getSpeed()), cfgX + 120, cfgY + 32, 0xFF55FF55);

            // Draw Slider Track
            context.fill(sliderX, speedY + 3, sliderX + sliderW, speedY + 5, 0xFF333333); // Background track
            float speedPct = (module.getSpeed() - 0.01f) / (50.00f - 0.01f);
            int speedKnobX = sliderX + (int) (speedPct * sliderW);
            context.fill(sliderX, speedY + 3, speedKnobX, speedY + 5, 0xFF55FF55); // Active filled track
            context.fill(speedKnobX - 2, speedY, speedKnobX + 2, speedY + 8, 0xFFFFFFFF); // Knob handle

            // --- 2. SMOOTH SENSITIVITY SLIDER LOGIC & RENDERING ---
            int sensY = cfgY + 70;
            if (this.draggingSensitivity) {
                float pct = (float) (mouseX - sliderX) / sliderW;
                pct = Math.max(0.0f, Math.min(1.0f, pct));
                // Scale math from 0.00 to 10.00
                float newSens = 0.00f + pct * (10.00f - 0.00f);
                module.setSensitivity(newSens);
            }

            context.drawTextWithShadow(this.textRenderer, "Freecam Sensitivity:", cfgX + 5, cfgY + 58, 0xFFFFFFFF);
            // Draw [TextBox] value representation
            context.fill(cfgX + 116, cfgY + 56, cfgX + 152, cfgY + 67, 0xFF151515);
            context.drawTextWithShadow(this.textRenderer, String.format("%.2f", module.getSensitivity()), cfgX + 122, cfgY + 58, 0xFF55FF55);

            // Draw Slider Track
            context.fill(sliderX, sensY + 3, sliderX + sliderW, sensY + 5, 0xFF333333); // Background track
            float sensPct = (module.getSensitivity() - 0.00f) / (10.00f - 0.00f);
            int sensKnobX = sliderX + (int) (sensPct * sliderW);
            context.fill(sliderX, sensY + 3, sensKnobX, sensY + 5, 0xFF55FF55); // Active filled track
            context.fill(sensKnobX - 2, sensY, sensKnobX + 2, sensY + 8, 0xFFFFFFFF); // Knob handle

            // Tiny Notice Text Below Sensitivity
            context.getMatrices().push();
            context.getMatrices().translate(cfgX + 5, cfgY + 81, 0);
            context.getMatrices().scale(0.62f, 0.62f, 1.0f);
            context.drawTextWithShadow(this.textRenderer, "If Sensitivity hits 0.00, game sensitivity applies!", 0, 0, 0xFF999999);
            context.getMatrices().pop();

            // Interactive Buttons & Toggles
            context.drawTextWithShadow(this.textRenderer, "Dimension Cutoff: " + (module.isTurnOffOnDimensionChange() ? "§aOn" : "§cOff"), cfgX + 5, cfgY + 94, 0xFFFFFFFF);
            context.drawTextWithShadow(this.textRenderer, "Damage Cutoff: " + (module.isTurnOffOnDamage() ? "§aOn" : "§cOff"), cfgX + 5, cfgY + 107, 0xFFFFFFFF);

            context.getMatrices().push();
            context.getMatrices().translate(cfgX + 5, cfgY + 118, 0);
            context.getMatrices().scale(0.70f, 0.70f, 1.0f);
            context.drawTextWithShadow(this.textRenderer, "Global Settings", 0, 0, 0xFFAAAAAA);
            context.getMatrices().pop();

            context.drawTextWithShadow(this.textRenderer, "§6[ Reset All ]", cfgX + 5, cfgY + 128, 0xFFFFFFFF);

            String bindText = listeningForKeybind ? "[Press Key...]" : (module.getKeybind() == GLFW.GLFW_KEY_UNKNOWN ? "Not Bound" : InputUtil.fromKeyCode(module.getKeybind(), 0).getLocalizedText().getString());
            context.drawTextWithShadow(this.textRenderer, "Keybinds: §e" + bindText, cfgX + 5, cfgY + 141, 0xFFFFFFFF);

            context.drawTextWithShadow(this.textRenderer, "§c[ Hide Configuration ]", cfgX + 5, cfgY + 154, 0xFFFFFFFF);
            context.drawTextWithShadow(this.textRenderer, "Active: " + (module.isActive() ? "§a[✔]" : "§c[ ]"), cfgX + 5, cfgY + 167, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mainX = this.width / 2 - 50;
        int mainY = this.height / 2 - 60;
        FreecamModule module = AClientClient.FREECAM;

        // Core Module Frame Check
        if (mouseX >= mainX && mouseX <= mainX + 100 && mouseY >= mainY + 16 && mouseY <= mainY + 40) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                module.toggle(this.client);
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                showConfig = !showConfig;
                return true;
            }
        }

        // Inside Panel Actions
        if (showConfig) {
            int cfgX = mainX - 170;
            int cfgY = mainY - 20;
            int sliderX = cfgX + 8;
            int sliderW = 144;

            // Catch Speed Slider Interactions (Including TextBox boundaries)
            if (mouseX >= sliderX && mouseX <= sliderX + sliderW && mouseY >= cfgY + 40 && mouseY <= cfgY + 52) {
                this.draggingSpeed = true;
                return true;
            }

            // Catch Sensitivity Slider Interactions
            if (mouseX >= sliderX && mouseX <= sliderX + sliderW && mouseY >= cfgY + 66 && mouseY <= cfgY + 78) {
                this.draggingSensitivity = true;
                return true;
            }

            // Dimension Cutoff
            if (mouseX >= cfgX && mouseX <= cfgX + 150 && mouseY >= cfgY + 92 && mouseY <= cfgY + 103) {
                module.setTurnOffOnDimensionChange(!module.isTurnOffOnDimensionChange());
                return true;
            }
            // Damage Cutoff
            if (mouseX >= cfgX && mouseX <= cfgX + 150 && mouseY >= cfgY + 105 && mouseY <= cfgY + 116) {
                module.setTurnOffOnDamage(!module.isTurnOffOnDamage());
                return true;
            }
            // Reset All Action Button
            if (mouseX >= cfgX && mouseX <= cfgX + 80 && mouseY >= cfgY + 126 && mouseY <= cfgY + 137) {
                module.resetAll();
                return true;
            }
            // Keybind Picker Trigger
            if (mouseX >= cfgX && mouseX <= cfgX + 150 && mouseY >= cfgY + 139 && mouseY <= cfgY + 150) {
                listeningForKeybind = true;
                return true;
            }
            // Hide Menu
            if (mouseX >= cfgX && mouseX <= cfgX + 120 && mouseY >= cfgY + 152 && mouseY <= cfgY + 163) {
                showConfig = false;
                return true;
            }
            // Checkbox Status Handler
            if (mouseX >= cfgX && mouseX <= cfgX + 100 && mouseY >= cfgY + 165 && mouseY <= cfgY + 177) {
                module.toggle(this.client);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Drop dragging states instantly on mouse up
        this.draggingSpeed = false;
        this.draggingSensitivity = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                AClientClient.FREECAM.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                AClientClient.FREECAM.setKeybind(keyCode);
            }
            listeningForKeybind = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}