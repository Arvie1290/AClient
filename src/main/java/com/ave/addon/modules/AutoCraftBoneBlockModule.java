package com.ave.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class AutoCraftBoneBlockModule extends Module {
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
    private boolean isInteracting = false;
    private boolean wasInCraftingScreen = false;

    public AutoCraftBoneBlockModule(Category category) {
        super(category, "auto-craft-bone-block", "Automatically crafts Bone Blocks from Bone Meal using Crafting Tables.");
    }

    @Override
    public void onActivate() {
        // Mutual Exclusion Check
        AutoCraftBoneMealModule boneMealModule = Modules.get().get(AutoCraftBoneMealModule.class);
        if (boneMealModule != null && boneMealModule.isActive()) {
            warning("Cannot enable AutoCraftBoneBlock while AutoCraftBoneMeal is active!");
            toggle();
            return;
        }

        timerTicks = 0;
        noMaterialTicks = 0;
        guiCloseCount = 0;
        guiCooldownTicks = 0;
        isInteracting = false;
        wasInCraftingScreen = false;
        resetTimer();

        info("AutoCraftBoneBlock activated. Monitoring inventory and nearby Crafting Tables...");
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

        // Material depletion check
        int totalBoneMeal = getTotalBoneMealCount();

        if (totalBoneMeal < 9) {
            if (mc.currentScreen instanceof CraftingScreen) {
                CraftingScreenHandler handler = ((CraftingScreen) mc.currentScreen).getScreenHandler();
                if (hasItemInGridOrOutput(handler, 10)) {
                    flushOutputAndGrid(handler, 10);
                    noMaterialTicks = 0;
                    return;
                }
            }

            noMaterialTicks++;
            if (noMaterialTicks % 20 == 0) {
                sendStatus("Insufficient Bone Meal (Requires at least 9, found " + totalBoneMeal + "). Disabling in " + (5 - (noMaterialTicks / 20)) + "s...", false);
            }

            if (noMaterialTicks >= 100) {
                sendStatus("Insufficient Bone Meal for 5 seconds! Safety triggered. Disabling module...", true);
                toggle();
                return;
            }
            return;
        } else {
            noMaterialTicks = 0;
        }

        // Detect Crafting Table GUI closure specifically
        boolean currentIsCraftingScreen = mc.currentScreen instanceof CraftingScreen;
        if (wasInCraftingScreen && !currentIsCraftingScreen) {
            guiCloseCount++;
            sendStatus("Crafting Table closed unexpectedly (" + guiCloseCount + "/4).", true);

            if (guiCloseCount >= 4) {
                sendStatus("Crafting Table closed 4 times unexpectedly! Safety triggered. Disabling AutoCraftBoneBlock...", true);
                toggle();
                return;
            }
        }
        wasInCraftingScreen = currentIsCraftingScreen;

        BlockPos tablePos = findNearbyCraftingTable();
        if (tablePos == null) {
            sendStatus("No Crafting Table found nearby! Disabling module...", true);
            toggle();
            return;
        }

        // Jika GUI Crafting Table belum terbuka -> Interaksi (Buka) & siapkan cooldown 20 tick
        if (!currentIsCraftingScreen) {
            if (!isInteracting) {
                sendStatus("Opening Crafting Table at " + tablePos.toShortString() + "...", false);
                isInteracting = true;

                Rotations.rotate(Rotations.getYaw(tablePos), Rotations.getPitch(tablePos), () -> {
                    if (mc.interactionManager != null) {
                        BlockHitResult hitResult = new BlockHitResult(
                            Vec3d.ofCenter(tablePos), Direction.UP, tablePos, false
                        );
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                    }
                });
            }
            guiCooldownTicks = 20;
            return;
        }

        isInteracting = false;

        // Tunggu 20 tick setelah GUI terbuka
        if (guiCooldownTicks > 0) {
            guiCooldownTicks--;
            return;
        }

        // Jalankan delay normal sebelum crafting
        timerTicks++;
        if (timerTicks < targetDelayTicks) return;

        CraftingScreenHandler handler = ((CraftingScreen) mc.currentScreen).getScreenHandler();

        fillGridSmart(handler);

        ItemStack outputStack = handler.getSlot(0).getStack();

        if (!outputStack.isEmpty() && outputStack.getItem() == Items.BONE_BLOCK) {
            if (autoDropCraft.get()) {
                dropOutputToGround(handler);
                sendStatus("Crafted Bone Block -> Dropped output directly to ground.", false);
            } else {
                if (mc.player.getInventory().getEmptySlot() == -1) {
                    sendStatus("Inventory is full! AutoCraftBoneBlock disabled.", true);
                    toggle();
                    return;
                }

                if (mc.interactionManager != null) {
                    mc.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                    sendStatus("Crafted Bone Block -> Moved output to inventory.", false);
                }
            }
            guiCloseCount = 0;
        }

        resetTimer();
    }

    private void fillGridSmart(CraftingScreenHandler handler) {
        if (mc.interactionManager == null || mc.player == null) return;

        boolean needFill = false;
        for (int slot = 1; slot <= 9; slot++) {
            if (handler.getSlot(slot).getStack().getItem() != Items.BONE_MEAL) {
                needFill = true;
                break;
            }
        }

        if (!needFill) return;

        int totalBoneMeal = getTotalBoneMealCount();

        if (totalBoneMeal >= 576) {
            for (int i = 10; i < 46; i++) {
                ItemStack stack = handler.getSlot(i).getStack();
                if (stack.getItem() == Items.BONE_MEAL) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
            }
            return;
        }

        int sourceSlot = findBoneMealSlot(handler);
        if (sourceSlot == -1) return;

        int syncId = handler.syncId;

        mc.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, -999, ScreenHandler.packQuickCraftData(0, 0), SlotActionType.QUICK_CRAFT, mc.player);

        for (int slot = 1; slot <= 9; slot++) {
            mc.interactionManager.clickSlot(syncId, slot, ScreenHandler.packQuickCraftData(1, 0), SlotActionType.QUICK_CRAFT, mc.player);
        }

        mc.interactionManager.clickSlot(syncId, -999, ScreenHandler.packQuickCraftData(2, 0), SlotActionType.QUICK_CRAFT, mc.player);

        if (!handler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private int findBoneMealSlot(CraftingScreenHandler handler) {
        for (int i = 10; i < 46; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (stack.getItem() == Items.BONE_MEAL) {
                return i;
            }
        }
        return -1;
    }

    private void dropOutputToGround(CraftingScreenHandler handler) {
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
                dropOutputToGround((CraftingScreenHandler) handler);
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

    private int getTotalBoneMealCount() {
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.BONE_MEAL) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private BlockPos findNearbyCraftingTable() {
        if (mc.player == null || mc.world == null) return null;

        BlockPos playerPos = mc.player.getBlockPos();
        int radius = 5;

        BlockPos nearestPos = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).isOf(Blocks.CRAFTING_TABLE)) {
                        double dist = playerPos.getSquaredDistance(pos);
                        if (dist < nearestDistance) {
                            nearestDistance = dist;
                            nearestPos = pos;
                        }
                    }
                }
            }
        }

        return nearestPos;
    }
}
