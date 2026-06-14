package com.aclient.client;

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
	private static KeyBinding guiKeyBinding;

	// Tracks the last known dimension to catch active portal travel
	private static RegistryKey<World> lastDimension = null;

	@Override
	public void onInitializeClient() {
		// Registering Right-Shift to open the hack menu
		guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.aclient.open_menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.aclient.general"
		));

		// Unified tick event handling all core state modifications
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) {
				lastDimension = null; // Reset when leaving servers
				return;
			}

			// --- 1. DETECT DIMENSION CHANGE ---
			RegistryKey<World> currentDimension = client.world.getRegistryKey();
			if (lastDimension != null && !lastDimension.equals(currentDimension)) {
				if (FREECAM.isActive() && FREECAM.isTurnOffOnDimensionChange()) {
					FREECAM.setActive(false);
				}
			}
			lastDimension = currentDimension;

			// --- 2. DETECT DAMAGE ---
			if (client.player.hurtTime > 0) {
				if (FREECAM.isActive() && FREECAM.isTurnOffOnDamage()) {
					FREECAM.setActive(false);
				}
			}

			// --- 3. OPEN GUI MENU ---
			if (guiKeyBinding.wasPressed()) {
				client.setScreen(new ClickGuiScreen());
			}

			// --- 4. HANDLE CUSTOM MODULE KEYBIND ---
			if (FREECAM.getKeybind() != GLFW.GLFW_KEY_UNKNOWN) {
				if (InputUtil.isKeyPressed(client.getWindow().getHandle(), FREECAM.getKeybind())) {
					if (!FREECAM.isKeybindPressedLastTick()) {
						FREECAM.toggle(client);
						FREECAM.setKeybindPressedLastTick(true);
					}
				} else {
					FREECAM.setKeybindPressedLastTick(false);
				}
			}

			// --- 5. RUN FREECAM TICK LOOKUPS ---
			if (FREECAM.isActive()) {
				FREECAM.onTick(client);
			}
		});
	}
}