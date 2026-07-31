package com.ave.addon;

import com.ave.addon.modules.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AVEAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger(AVEAddon.class);
    public static final Category CATEGORY = new Category("AVE Addon");

    @Override
    public void onInitialize() {
        LOG.info("Initializing AVE Addon Modules...");

        Modules modules = Modules.get();

        List<Module> moduleList = List.of(
            new AntiTrap(CATEGORY),
            new ChunkStashModule(CATEGORY),
            new CoordinatesModule(CATEGORY),
            new DisconnectButton(CATEGORY),
            new FreecamPlus(CATEGORY),
            new FreelookPlus(CATEGORY),
            new HandViewPlusModule(CATEGORY),
            new SpawnerFinderModule(CATEGORY),
            new AutoCraftBoneBlockModule(CATEGORY),
            new AutoCraftBoneMealModule(CATEGORY)
        );

        moduleList.forEach(modules::add);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.ave.addon";
    }
}
