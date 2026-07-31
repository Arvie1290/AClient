package com.ave.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Random;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class AutoCraftBoneMealModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> delayRandom = sgGeneral.add(new BoolSetting.Builder()
        .name("delay-random")
        .description("Applies a random delay between 1s and 10s for crafting actions.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> delayDuration = sgGeneral.add(new DoubleSetting.Builder()
        .name("delay-duration")
        .description("Delay duration in seconds before crafting (0.001s min, no max limit).")
        .defaultValue(1.000)
        .min(0.001)
        .max(Double.MAX_VALUE)
        .sliderMin(0.001)
        .sliderMax(50.0)
        .decimalPlaces(3)
        .visible(() -> !delayRandom.get())
        .build()
    );

    private final Setting<Boolean> autoDropCraft = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-drop-craft")
        .description("Automatically drops the crafted item directly to the ground.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> sendStatusChat = sgGeneral.add(new BoolSetting.Builder()
        .name("send-status-chat")
        .description("Sends status and warning messages in chat.")
        .defaultValue(true)
        .build()
    );

    private final Random random = new Random();
    private int timerTicks = 0;
    private int targetDelayTicks = 0;
    private int noMaterialTicks = 0;
    private int guiCloseCount = 0;
    private int guiCooldownTicks = 0;
    private boolean wasInInventoryScreen = false;

    public AutoCraftBoneMealModule(Category category) {
        super(category, "auto-craft-bone-meal", "Automatically crafts Bone Meal from Bones using Player Inventory Grid.");
    }

    @Override
    public void onActivate() {
        // Safety Protocol: Mutual Exclusion Check
        AutoCraftBoneBlockModule boneBlockModule = Modules.get().get(AutoCraftBoneBlockModule.class);
        if (boneBlockModule != null && boneBlockModule.isActive()) {
            warning("Cannot enable AutoCraftBoneMeal while AutoCraftBoneBlock is active!");
            toggle();
            return;
        }

        timerTicks = 0;
        noMaterialTicks = 0;
        guiCloseCount = 0;
        guiCooldownTicks = 0;
        wasInInventoryScreen = false;
        resetTimer();

        info("AutoCraftBoneMeal activated. Searching for Bones in inventory...");
    }

    private void resetTimer() {
        if (delayRandom.get()) {
            int seconds = random.nextInt(10) + 1;
            targetDelayTicks = seconds * 20;
        } else {
            targetDelayTicks = (int) Math.max(1, Math.round(delayDuration.get() * 20.0));
        }
        timerTicks = 0;
    }

    private void sendStatus(String message, boolean isWarning) {
        if (!sendStatusChat.get()) return;
        if (isWarning) {
            warning(message);
        } else {
            info(message);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        // Safety Protocol: Material depletion check
        if (!hasBoneInInventory()) {
            PlayerScreenHandler handler = mc.player.playerScreenHandler;

            if (hasItemInGridOrOutput(handler, 5)) {
                flushOutputAndGrid(handler, 5);
                noMaterialTicks = 0;
                return;
            }

            noMaterialTicks++;
            if (noMaterialTicks % 20 == 0) {
                sendStatus("No Bones found in inventory. Disabling in " + (5 - (noMaterialTicks / 20)) + "s...", false);
            }

            if (noMaterialTicks >= 100) {
                sendStatus("Out of Bones for 5 seconds! Safety triggered. Disabling module...", true);
                toggle();
                return;
            }
            return;
        } else {
            noMaterialTicks = 0;
        }

        // Safety Protocol: Detect Inventory GUI unexpected closure
        boolean currentIsInventoryScreen = mc.currentScreen instanceof InventoryScreen;
        if (wasInInventoryScreen && !currentIsInventoryScreen) {
            guiCloseCount++;
            sendStatus("Inventory closed unexpectedly (" + guiCloseCount + "/4).", true);

            if (guiCloseCount >= 4) {
                sendStatus("Inventory closed 4 times unexpectedly! Safety triggered. Disabling AutoCraftBoneMeal...", true);
                toggle();
                return;
            }
        }
        wasInInventoryScreen = currentIsInventoryScreen;

        // Auto Open Inventory Screen if not open
        if (!currentIsInventoryScreen) {
            mc.setScreen(new InventoryScreen(mc.player));
            guiCooldownTicks = 20; // Pause 20 ticks after opening screen before crafting
            return;
        }

        // Handle 20-tick cooldown after GUI opens
        if (guiCooldownTicks > 0) {
            guiCooldownTicks--;
            return;
        }

        timerTicks++;
        if (timerTicks < targetDelayTicks) return;

        PlayerScreenHandler handler = mc.player.playerScreenHandler;

        // Execute Crafting Action (GUI KEEPS OPEN)
        if (processCrafting(handler)) {
            guiCloseCount = 0; // Reset count on successful craft
        }

        resetTimer();
    }

    private boolean processCrafting(PlayerScreenHandler handler) {
        if (!putBoneInCraftingSlot(handler)) {
            return false;
        }

        ItemStack outputStack = handler.getSlot(0).getStack();

        if (outputStack.isEmpty() || outputStack.getItem() != Items.BONE_MEAL) {
            return false;
        }

        if (autoDropCraft.get()) {
            dropOutputToGround(handler);
            sendStatus("Crafted Bone Meal -> Dropped output directly to ground.", false);
        } else {
            if (mc.player.getInventory().getEmptySlot() == -1) {
                sendStatus("Inventory is full! AutoCraftBoneMeal disabled.", true);
                toggle();
                return false;
            }

            if (mc.interactionManager != null) {
                mc.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                sendStatus("Crafted Bone Meal -> Moved output to inventory.", false);
            }
        }
        return true;
    }

    private void dropOutputToGround(ScreenHandler handler) {
        if (mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.clickSlot(handler.syncId, 0, 1, SlotActionType.THROW, mc.player);
    }

    private boolean hasItemInGridOrOutput(ScreenHandler handler, int gridSlots) {
        for (int i = 0; i < gridSlots; i++) {
            if (!handler.getSlot(i).getStack().isEmpty()) return true;
        }
        return false;
    }

    private void flushOutputAndGrid(ScreenHandler handler, int gridSlots) {
        if (mc.interactionManager == null || mc.player == null) return;

        if (!handler.getSlot(0).getStack().isEmpty()) {
            if (autoDropCraft.get()) {
                dropOutputToGround(handler);
            } else {
                mc.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }

        for (int i = 1; i < gridSlots; i++) {
            if (!handler.getSlot(i).getStack().isEmpty()) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }

    private boolean hasBoneInInventory() {
        if (mc.player == null) return false;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.BONE) {
                return true;
            }
        }
        return false;
    }

    private boolean putBoneInCraftingSlot(PlayerScreenHandler handler) {
        if (mc.interactionManager == null) return false;

        if (handler.getSlot(1).getStack().getItem() == Items.BONE) return true;

        for (int i = 9; i < 45; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (stack.getItem() == Items.BONE) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(handler.syncId, 1, 0, SlotActionType.PICKUP, mc.player);
                if (!handler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                }
                return true;
            }
        }
        return false;
    }
}
