package com.aclient.client;

import com.aclient.client.config.ConfigManager;
import com.aclient.client.module.FreecamModule;
import com.aclient.client.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class AClientClient implements ClientModInitializer {
    public static final FreecamModule FREECAM = new FreecamModule();
    public static int menuKeycode = GLFW.GLFW_KEY_INSERT;

    private static KeyBinding guiKeyBinding;
    private static RegistryKey<World> lastDimension = null;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aclient.open_menu",
                InputUtil.Type.KEYSYM,
                menuKeycode,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                lastDimension = null;
                if (FREECAM.isActive()) {
                    FREECAM.disableOnDimensionChange(client);
                }
                return;
            }

            if (client.currentScreen != null && !(client.currentScreen instanceof ClickGuiScreen)) {
                return;
            }

            RegistryKey<World> currentDimension = client.world.getRegistryKey();
            if (lastDimension != null && !lastDimension.equals(currentDimension)) {
                if (FREECAM.isActive() && FREECAM.isTurnOffOnDimensionChange()) {
                    FREECAM.disableOnDimensionChange(client);
                }
            }
            lastDimension = currentDimension;

            if (client.player.hurtTime > 0) {
                if (FREECAM.isActive() && FREECAM.isTurnOffOnDamage()) {
                    FREECAM.toggle(client);
                }
            }

            if (InputUtil.isKeyPressed(client.getWindow(), menuKeycode)) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }

            if (client.currentScreen == null && FREECAM.getKeybind() != 0) {
                if (InputUtil.isKeyPressed(client.getWindow(), FREECAM.getKeybind())) {
                    if (!FREECAM.isKeybindPressedLastTick()) {
                        FREECAM.toggle(client);
                        FREECAM.setKeybindPressedLastTick(true);
                    }
                } else {
                    FREECAM.setKeybindPressedLastTick(false);
                }
            }

            if (FREECAM.isActive()) {
                FREECAM.onClientTick(client);
            }
        });
    }
}