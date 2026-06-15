package com.aclient.client.config;

import com.aclient.client.AClientClient;
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
        public float sprintSpeed = 2.25f;
        public float sensitivity = 1.00f;
        public boolean turnOffOnDimensionChange = true;
        public boolean turnOffOnDamage = true;
        public boolean chatNotification = true;
        public int keybind = 0;
        public int menuKeycode = 342;
    }

    public static void save() {
        try {
            ConfigData data = new ConfigData();
            data.speed = AClientClient.FREECAM.getSpeed();
            data.sprintSpeed = AClientClient.FREECAM.getSprintSpeed();
            data.sensitivity = AClientClient.FREECAM.getSensitivity();
            data.turnOffOnDimensionChange = AClientClient.FREECAM.isTurnOffOnDimensionChange();
            data.turnOffOnDamage = AClientClient.FREECAM.isTurnOffOnDamage();
            data.chatNotification = AClientClient.FREECAM.isChatNotification();
            data.keybind = AClientClient.FREECAM.getKeybind();
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
                AClientClient.FREECAM.setSpeed(data.speed);
                AClientClient.FREECAM.setSprintSpeed(data.sprintSpeed);
                AClientClient.FREECAM.setSensitivity(data.sensitivity);
                AClientClient.FREECAM.setTurnOffOnDimensionChange(data.turnOffOnDimensionChange);
                AClientClient.FREECAM.setTurnOffOnDamage(data.turnOffOnDamage);
                AClientClient.FREECAM.setChatNotification(data.chatNotification);
                AClientClient.FREECAM.setKeybind(data.keybind);
                AClientClient.menuKeycode = data.menuKeycode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}