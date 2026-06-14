package com.aclient.client.config;

import com.aclient.client.AClientClient;
import com.aclient.client.module.FreecamModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "aclient_config.json");

    public static class ConfigData {
        public float speed = 1.00f;
        public float sensitivity = 0.00f;
        public boolean turnOffOnDimensionChange = true;
        public boolean turnOffOnDamage = true;
        public boolean chatNotification = true;
        public int keybind = 0;
        public int menuKeycode = 342; // GLFW_KEY_INSERT
    }

    public static void save() {
        try {
            ConfigData data = new ConfigData();
            FreecamModule module = AClientClient.FREECAM;
            data.speed = module.getSpeed();
            data.sensitivity = module.getSensitivity();
            data.turnOffOnDimensionChange = module.isTurnOffOnDimensionChange();
            data.turnOffOnDamage = module.isTurnOffOnDamage();
            data.chatNotification = module.isChatNotification();
            data.keybind = module.getKeybind();
            data.menuKeycode = AClientClient.menuKeycode;

            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }
        try (FileReader reader = new FileReader(FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                FreecamModule module = AClientClient.FREECAM;
                module.setSpeed(data.speed);
                module.setSensitivity(data.sensitivity);
                module.setTurnOffOnDimensionChange(data.turnOffOnDimensionChange);
                module.setTurnOffOnDamage(data.turnOffOnDamage);
                module.setChatNotification(data.chatNotification);
                module.setKeybind(data.keybind);
                AClientClient.menuKeycode = data.menuKeycode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}