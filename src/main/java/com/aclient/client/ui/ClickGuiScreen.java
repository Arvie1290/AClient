package com.aclient.client.ui;

import com.aclient.client.AClientClient;
import com.aclient.client.config.ConfigManager;
import com.aclient.client.module.FreecamModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen {
    private boolean showConfig = false;
    private boolean listeningForKeybind = false;
    private boolean listeningForMenuKey = false;

    private boolean draggingSpeed = false;
    private boolean draggingSensitivity = false;

    public ClickGuiScreen() {
        super(Text.of("AClient Click GUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        FreecamModule module = AClientClient.FREECAM;

        float alpha = module.getCurrentAlpha();
        if (module.isActive() && alpha < 0.25f) {
            alpha = Math.min(0.25f, alpha + (delta * 0.05f));
        } else if (!module.isActive() && alpha > 0.0f) {
            alpha = Math.max(0.0f, alpha - (delta * 0.05f));
        }
        module.setCurrentAlpha(alpha);

        int mainX = this.width / 2 - 50;
        int mainY = this.height / 2 - 60;
        int mainWidth = 100;

        context.fill(mainX, mainY, mainX + mainWidth, mainY + 14, 0xFF202020);
        context.drawCenteredTextWithShadow(this.textRenderer, "Visual", mainX + (mainWidth / 2), mainY + 3, 0xFFFFFFFF);

        int btnY = mainY + 16;
        context.fill(mainX, btnY, mainX + mainWidth, mainY + 40, 0xFF101010);
        if (alpha > 0) {
            int argbColor = ((int)(alpha * 255) << 24) | 0x0000FF00;
            context.fill(mainX, btnY, mainX + mainWidth, mainY + 40, argbColor);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, "Freecam", mainX + (mainWidth / 2), btnY + 5, 0xFFFFFFFF);

        int menuBindY = mainY + 44;
        context.fill(mainX, menuBindY, mainX + mainWidth, menuBindY + 16, 0xFF151515);
        String menuKeyName = listeningForMenuKey ? "[???]" : InputUtil.Type.KEYSYM.createFromCode(AClientClient.menuKeycode).getLocalizedText().getString();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float)(mainX + 4), (float)(menuBindY + 4));
        context.getMatrices().scale(0.72f, 0.72f);
        context.drawTextWithShadow(this.textRenderer, "Menu Bind: §e" + menuKeyName, 0, 0, 0xFFBBBBBB);
        context.getMatrices().popMatrix();

        if (showConfig) {
            int cfgX = mainX - 170;
            int cfgY = mainY - 20;
            int cfgW = 160;
            int cfgH = 196; // Extended configuration layout height

            context.fill(cfgX, cfgY, cfgX + cfgW, cfgY + cfgH, 0xDD050505);
            context.drawTextWithShadow(this.textRenderer, "Freecam Configuration", cfgX + 5, cfgY + 6, 0xFFFF5555);

            context.getMatrices().pushMatrix();
            context.getMatrices().translate((float)(cfgX + 5), (float)(cfgY + 18));
            context.getMatrices().scale(0.75f, 0.75f);
            context.drawTextWithShadow(this.textRenderer, "Can make your camera float anywhere!", 0, 0, 0xFFAAAAAA);
            context.getMatrices().popMatrix();

            int sliderX = cfgX + 8;
            int sliderW = 144;

            int speedY = cfgY + 44;
            if (this.draggingSpeed) {
                float pct = (float) (mouseX - sliderX) / sliderW;
                pct = Math.max(0.0f, Math.min(1.0f, pct));
                float newSpeed = 0.01f + pct * (50.00f - 0.01f);
                module.setSpeed(newSpeed);
            }

            context.drawTextWithShadow(this.textRenderer, "Freecam Speed:", cfgX + 5, cfgY + 32, 0xFFFFFFFF);
            context.fill(cfgX + 116, cfgY + 30, cfgX + 152, cfgY + 41, 0xFF151515);
            context.drawTextWithShadow(this.textRenderer, String.format("%.2f", module.getSpeed()), cfgX + 120, cfgY + 32, 0xFF55FF55);

            context.fill(sliderX, speedY + 3, sliderX + sliderW, speedY + 5, 0xFF333333);
            float speedPct = (module.getSpeed() - 0.01f) / (50.00f - 0.01f);
            int speedKnobX = sliderX + (int) (speedPct * sliderW);
            context.fill(sliderX, speedY + 3, speedKnobX, speedY + 5, 0xFF55FF55);
            context.fill(speedKnobX - 2, speedY, speedKnobX + 2, speedY + 8, 0xFFFFFFFF);

            int sensY = cfgY + 70;
            if (this.draggingSensitivity) {
                float pct = (float) (mouseX - sliderX) / sliderW;
                pct = Math.max(0.0f, Math.min(1.0f, pct));
                float newSens = 0.00f + pct * (10.00f - 0.00f);
                module.setSensitivity(newSens);
            }

            context.drawTextWithShadow(this.textRenderer, "Freecam Sensitivity:", cfgX + 5, cfgY + 58, 0xFFFFFFFF);
            context.fill(cfgX + 116, cfgY + 56, cfgX + 152, cfgY + 67, 0xFF151515);
            context.drawTextWithShadow(this.textRenderer, String.format("%.2f", module.getSensitivity()), cfgX + 122, cfgY + 58, 0xFF55FF55);

            context.fill(sliderX, sensY + 3, sliderX + sliderW, sensY + 5, 0xFF333333);
            float sensPct = (module.getSensitivity() - 0.00f) / (10.00f - 0.00f);
            int sensKnobX = sliderX + (int) (sensPct * sliderW);
            context.fill(sliderX, sensY + 3, sensKnobX, sensY + 5, 0xFF55FF55);
            context.fill(sensKnobX - 2, sensY, sensKnobX + 2, sensY + 8, 0xFFFFFFFF);

            context.getMatrices().pushMatrix();
            context.getMatrices().translate((float)(cfgX + 5), (float)(cfgY + 81));
            context.getMatrices().scale(0.62f, 0.62f);
            context.drawTextWithShadow(this.textRenderer, "If Sensitivity hits 0.00, game sensitivity applies!", 0, 0, 0xFF999999);
            context.getMatrices().popMatrix();

            context.drawTextWithShadow(this.textRenderer, "Dimension Cutoff: " + (module.isTurnOffOnDimensionChange() ? "§aOn" : "§cOff"), cfgX + 5, cfgY + 94, 0xFFFFFFFF);
            context.drawTextWithShadow(this.textRenderer, "Damage Cutoff: " + (module.isTurnOffOnDamage() ? "§aOn" : "§cOff"), cfgX + 5, cfgY + 107, 0xFFFFFFFF);

            // --- GLOBAL SETTINGS SECTION ---
            context.getMatrices().pushMatrix();
            context.getMatrices().translate((float)(cfgX + 5), (float)(cfgY + 119));
            context.getMatrices().scale(0.70f, 0.70f);
            context.drawTextWithShadow(this.textRenderer, "Global Settings", 0, 0, 0xFFAAAAAA);
            context.getMatrices().popMatrix();

            // Chat Notification Button Option
            context.drawTextWithShadow(this.textRenderer, "Chat Notification: " + (module.isChatNotification() ? "§aOn" : "§cOff"), cfgX + 5, cfgY + 129, 0xFFFFFFFF);

            context.drawTextWithShadow(this.textRenderer, "§6[ Reset All ]", cfgX + 5, cfgY + 144, 0xFFFFFFFF);

            String bindText = listeningForKeybind ? "[Press Key...]" : (module.getKeybind() == GLFW.GLFW_KEY_UNKNOWN ? "Not Bound" : InputUtil.Type.KEYSYM.createFromCode(module.getKeybind()).getLocalizedText().getString());
            context.drawTextWithShadow(this.textRenderer, "Keybinds: §e" + bindText, cfgX + 5, cfgY + 157, 0xFFFFFFFF);

            context.drawTextWithShadow(this.textRenderer, "§c[ Hide Configuration ]", cfgX + 5, cfgY + 170, 0xFFFFFFFF);
            context.drawTextWithShadow(this.textRenderer, "Active: " + (module.isActive() ? "§a[✔]" : "§c[ ]"), cfgX + 5, cfgY + 183, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int mainX = this.width / 2 - 50;
        int mainY = this.height / 2 - 60;
        FreecamModule module = AClientClient.FREECAM;

        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (mouseX >= mainX && mouseX <= mainX + 100 && mouseY >= mainY + 16 && mouseY <= mainY + 40) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                module.toggle(this.client);
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                showConfig = !showConfig;
                return true;
            }
        }

        if (mouseX >= mainX && mouseX <= mainX + 100 && mouseY >= mainY + 44 && mouseY <= mainY + 60) {
            this.listeningForMenuKey = true;
            return true;
        }

        if (showConfig) {
            int cfgX = mainX - 170;
            int cfgY = mainY - 20;
            int sliderX = cfgX + 8;
            int sliderW = 144;

            if (mouseX >= sliderX && mouseX <= sliderX + sliderW && mouseY >= cfgY + 40 && mouseY <= cfgY + 52) {
                this.draggingSpeed = true;
                return true;
            }

            if (mouseX >= sliderX && mouseX <= sliderX + sliderW && mouseY >= cfgY + 66 && mouseY <= cfgY + 78) {
                this.draggingSensitivity = true;
                return true;
            }

            if (mouseX >= cfgX && mouseX <= cfgX + 150 && mouseY >= cfgY + 92 && mouseY <= cfgY + 103) {
                module.setTurnOffOnDimensionChange(!module.isTurnOffOnDimensionChange());
                ConfigManager.save();
                return true;
            }
            if (mouseX >= cfgX && mouseX <= cfgX + 150 && mouseY >= cfgY + 105 && mouseY <= cfgY + 116) {
                module.setTurnOffOnDamage(!module.isTurnOffOnDamage());
                ConfigManager.save();
                return true;
            }
            // Click Handler for Chat Notification Toggle
            if (mouseX >= cfgX && mouseX <= cfgX + 150 && mouseY >= cfgY + 127 && mouseY <= cfgY + 138) {
                module.setChatNotification(!module.isChatNotification());
                ConfigManager.save();
                return true;
            }
            if (mouseX >= cfgX && mouseX <= cfgX + 80 && mouseY >= cfgY + 142 && mouseY <= cfgY + 153) {
                module.resetAll();
                ConfigManager.save();
                return true;
            }
            if (mouseX >= cfgX && mouseX <= cfgX + 150 && mouseY >= cfgY + 155 && mouseY <= cfgY + 166) {
                listeningForKeybind = true;
                return true;
            }
            if (mouseX >= cfgX && mouseX <= cfgX + 120 && mouseY >= cfgY + 168 && mouseY <= cfgY + 179) {
                showConfig = false;
                return true;
            }
            if (mouseX >= cfgX && mouseX <= cfgX + 100 && mouseY >= cfgY + 181 && mouseY <= cfgY + 193) {
                module.toggle(this.client);
                return true;
            }
        }

        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (this.draggingSpeed || this.draggingSensitivity) {
            this.draggingSpeed = false;
            this.draggingSensitivity = false;
            ConfigManager.save(); // Save configuration when sliders are released
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        int keyCode = keyInput.key();

        if (listeningForMenuKey) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                AClientClient.menuKeycode = keyCode;
            }
            listeningForMenuKey = false;
            ConfigManager.save();
            return true;
        }

        if (listeningForKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                AClientClient.FREECAM.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                AClientClient.FREECAM.setKeybind(keyCode);
            }
            listeningForKeybind = false;
            ConfigManager.save();
            return true;
        }

        return super.keyPressed(keyInput);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}