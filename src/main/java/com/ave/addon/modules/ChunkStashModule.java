package com.ave.addon.modules;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Vector3d;

import java.util.*;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class ChunkStashModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPerformance = settings.createGroup("Performance");
    private final SettingGroup sgUtility = settings.createGroup("Utility");

    // --- STATIC WEIGHT TABLE ---
    private static final Reference2IntOpenHashMap<Block> BLOCK_WEIGHTS = new Reference2IntOpenHashMap<>();

    static {
        BLOCK_WEIGHTS.put(Blocks.BEACON, 20);
        BLOCK_WEIGHTS.put(Blocks.NETHERITE_BLOCK, 15);
        BLOCK_WEIGHTS.put(Blocks.LODESTONE, 12);
        BLOCK_WEIGHTS.put(Blocks.RESPAWN_ANCHOR, 10);
        BLOCK_WEIGHTS.put(Blocks.ENCHANTING_TABLE, 8);
        BLOCK_WEIGHTS.put(Blocks.SMITHING_TABLE, 6);
        BLOCK_WEIGHTS.put(Blocks.ANVIL, 5);
        BLOCK_WEIGHTS.put(Blocks.CHIPPED_ANVIL, 5);
        BLOCK_WEIGHTS.put(Blocks.DAMAGED_ANVIL, 5);
        BLOCK_WEIGHTS.put(Blocks.BLAST_FURNACE, 3);
        BLOCK_WEIGHTS.put(Blocks.CRAFTING_TABLE, 2);
        BLOCK_WEIGHTS.put(Blocks.FURNACE, 1);

        BLOCK_WEIGHTS.defaultReturnValue(2);
    }

    // --- PERFORMANCE SETTINGS ---
    private final Setting<Integer> scanDelay = sgPerformance.add(new IntSetting.Builder()
        .name("scan-delay-ticks")
        .description("Delay in ticks between normal scans (set lower for faster scanning).")
        .defaultValue(2) // Diturunkan dari 10 ke 2 agar jauh lebih responsif
        .min(0)
        .max(100)
        .sliderMin(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> chunksPerTick = sgPerformance.add(new IntSetting.Builder()
        .name("chunks-per-tick")
        .description("Maximum new chunks to scan per execution (increase for Elytra flying).")
        .defaultValue(8) // Scan sekaligus 8 chunk terdekat per siklus
        .min(1)
        .max(32)
        .sliderMin(1)
        .sliderMax(16)
        .build()
    );

    private final Setting<Integer> checkChunkAgain = sgPerformance.add(new IntSetting.Builder()
        .name("check-chunk-again")
        .description("Interval in ticks to re-check previously scanned chunks. Set to 0 to only check new chunks.")
        .defaultValue(67)
        .min(0)
        .max(Integer.MAX_VALUE)
        .sliderMin(0)
        .sliderMax(100)
        .build()
    );

    // --- GENERAL SETTINGS ---
    private final Setting<Boolean> manualThreeDim = sgGeneral.add(new BoolSetting.Builder()
        .name("manual-for-three-dimension")
        .description("Separate block pickers for Overworld, Nether, and The End.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Block>> overworldFinder = sgGeneral.add(new BlockListSetting.Builder()
        .name("overworld-finder")
        .description("Unnatural blocks to search for in the Overworld.")
        .defaultValue(List.of(
            Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
            Blocks.STONECUTTER, Blocks.GRINDSTONE, Blocks.SMITHING_TABLE, Blocks.CARTOGRAPHY_TABLE,
            Blocks.FLETCHING_TABLE, Blocks.LOOM, Blocks.CRAFTER, Blocks.ENCHANTING_TABLE,
            Blocks.BREWING_STAND, Blocks.BEACON, Blocks.LODESTONE, Blocks.RESPAWN_ANCHOR,
            Blocks.CONDUIT, Blocks.END_ROD, Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_TORCH,
            Blocks.REPEATER, Blocks.COMPARATOR, Blocks.OBSERVER, Blocks.DISPENSER,
            Blocks.DROPPER, Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.DAYLIGHT_DETECTOR,
            Blocks.TARGET, Blocks.NOTE_BLOCK, Blocks.JUKEBOX, Blocks.REDSTONE_LAMP,
            Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.LANTERN, Blocks.SOUL_LANTERN, Blocks.SEA_LANTERN,
            Blocks.IRON_DOOR, Blocks.IRON_TRAPDOOR, Blocks.CAULDRON, Blocks.WATER_CAULDRON,
            Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON, Blocks.COMPOSTER, Blocks.LECTERN,
            Blocks.DECORATED_POT, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.WHITE_BED, Blocks.ORANGE_BED,
            Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED,
            Blocks.PINK_BED, Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED,
            Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED,
            Blocks.RED_BED, Blocks.BLACK_BED
        ))
        .visible(manualThreeDim::get)
        .build()
    );

    private final Setting<List<Block>> netherFinder = sgGeneral.add(new BlockListSetting.Builder()
        .name("nether-finder")
        .description("Unnatural blocks to search for in the Nether.")
        .defaultValue(List.of(
            Blocks.WATER, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BLAST_FURNACE,
            Blocks.SMOKER, Blocks.STONECUTTER, Blocks.GRINDSTONE, Blocks.SMITHING_TABLE,
            Blocks.CARTOGRAPHY_TABLE, Blocks.FLETCHING_TABLE, Blocks.LOOM, Blocks.CRAFTER,
            Blocks.ENCHANTING_TABLE, Blocks.BREWING_STAND, Blocks.BEACON, Blocks.LODESTONE,
            Blocks.RESPAWN_ANCHOR, Blocks.CONDUIT, Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_TORCH,
            Blocks.REPEATER, Blocks.COMPARATOR, Blocks.OBSERVER, Blocks.DISPENSER,
            Blocks.DROPPER, Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.DAYLIGHT_DETECTOR,
            Blocks.TARGET, Blocks.NOTE_BLOCK, Blocks.JUKEBOX, Blocks.REDSTONE_LAMP,
            Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.IRON_DOOR,
            Blocks.IRON_TRAPDOOR, Blocks.COMPOSTER, Blocks.LECTERN, Blocks.CAULDRON,
            Blocks.WATER_CAULDRON, Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON,
            Blocks.DECORATED_POT, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.WHITE_BED, Blocks.ORANGE_BED,
            Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED,
            Blocks.PINK_BED, Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED,
            Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED,
            Blocks.RED_BED, Blocks.BLACK_BED
        ))
        .visible(manualThreeDim::get)
        .build()
    );

    private final Setting<List<Block>> endFinder = sgGeneral.add(new BlockListSetting.Builder()
        .name("the-end-finder")
        .description("Unnatural blocks to search for in The End.")
        .defaultValue(List.of(
            Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
            Blocks.STONECUTTER, Blocks.GRINDSTONE, Blocks.SMITHING_TABLE, Blocks.CARTOGRAPHY_TABLE,
            Blocks.FLETCHING_TABLE, Blocks.LOOM, Blocks.CRAFTER, Blocks.ENCHANTING_TABLE,
            Blocks.BREWING_STAND, Blocks.BEACON, Blocks.LODESTONE, Blocks.RESPAWN_ANCHOR,
            Blocks.CONDUIT, Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_TORCH, Blocks.REPEATER,
            Blocks.COMPARATOR, Blocks.OBSERVER, Blocks.DISPENSER, Blocks.DROPPER,
            Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.DAYLIGHT_DETECTOR, Blocks.TARGET,
            Blocks.NOTE_BLOCK, Blocks.JUKEBOX, Blocks.ANVIL, Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL, Blocks.OAK_DOOR, Blocks.IRON_DOOR, Blocks.OAK_TRAPDOOR,
            Blocks.IRON_TRAPDOOR, Blocks.LANTERN, Blocks.SOUL_LANTERN, Blocks.SEA_LANTERN,
            Blocks.REDSTONE_LAMP, Blocks.COMPOSTER, Blocks.LECTERN, Blocks.CAULDRON,
            Blocks.WATER_CAULDRON, Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON,
            Blocks.DECORATED_POT, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.WHITE_BED, Blocks.ORANGE_BED,
            Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED,
            Blocks.PINK_BED, Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED,
            Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED,
            Blocks.RED_BED, Blocks.BLACK_BED
        ))
        .visible(manualThreeDim::get)
        .build()
    );

    private final Setting<List<Block>> allDimFinder = sgGeneral.add(new BlockListSetting.Builder()
        .name("all-dimension-finder")
        .description("Blocks to search for across all dimensions.")
        .defaultValue(List.of(
            Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
            Blocks.STONECUTTER, Blocks.GRINDSTONE, Blocks.SMITHING_TABLE, Blocks.CARTOGRAPHY_TABLE,
            Blocks.FLETCHING_TABLE, Blocks.LOOM, Blocks.ENCHANTING_TABLE, Blocks.BREWING_STAND,
            Blocks.BEACON, Blocks.LODESTONE, Blocks.RESPAWN_ANCHOR, Blocks.REDSTONE_BLOCK,
            Blocks.OBSERVER, Blocks.DISPENSER, Blocks.DROPPER, Blocks.PISTON,
            Blocks.STICKY_PISTON, Blocks.REPEATER, Blocks.COMPARATOR, Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK,
            Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.CONDUIT
        ))
        .visible(() -> !manualThreeDim.get())
        .build()
    );

    private final Setting<Boolean> lockHighScore = sgGeneral.add(new BoolSetting.Builder()
        .name("lock-high-score")
        .description("Prevents chunk points from decreasing once a higher score has been recorded.")
        .defaultValue(false)
        .build()
    );

    // --- UTILITY SETTINGS ---
    public enum MsgMode {
        Toast, Chat, All
    }

    private final Setting<Boolean> msgToggle = sgUtility.add(new BoolSetting.Builder()
        .name("msg")
        .description("Send a notification when a stash chunk is found.")
        .defaultValue(true)
        .build()
    );

    private final Setting<MsgMode> msgMode = sgUtility.add(new EnumSetting.Builder<MsgMode>()
        .name("msg-mode")
        .description("Notification display mode.")
        .defaultValue(MsgMode.All)
        .visible(msgToggle::get)
        .build()
    );

    private final Setting<Integer> storageCount = sgUtility.add(new IntSetting.Builder()
        .name("storage-count")
        .description("Minimum threshold for storage blocks before adding score (+5 score per extra storage).")
        .defaultValue(4)
        .min(1)
        .max(1000)
        .sliderMin(1)
        .sliderMax(100)
        .build()
    );

    private final Setting<List<Block>> storageBlock = sgUtility.add(new BlockListSetting.Builder()
        .name("storage-block")
        .description("Specific storage blocks to detect.")
        .defaultValue(List.of(
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL,
            Blocks.SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
            Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.ENDER_CHEST
        ))
        .build()
    );

    private final Setting<Integer> chunkFinderScore = sgUtility.add(new IntSetting.Builder()
        .name("chunk-finder-score")
        .description("Required score to mark a chunk as a stash.")
        .defaultValue(50)
        .min(1)
        .max(100)
        .sliderMin(1)
        .sliderMax(100)
        .build()
    );

    private final Setting<SettingColor> chunkColor = sgUtility.add(new ColorSetting.Builder()
        .name("chunk-color")
        .description("Color for the detected stash chunk highlight.")
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .build()
    );

    private final Setting<Double> chunkOpacity = sgUtility.add(new DoubleSetting.Builder()
        .name("chunk-opacity")
        .description("Opacity factor for the chunk highlight fill.")
        .defaultValue(0.300)
        .min(0.001)
        .max(1.000)
        .sliderMin(0.001)
        .sliderMax(1.000)
        .build()
    );

    private final Setting<Boolean> detectTopSurface = sgUtility.add(new BoolSetting.Builder()
        .name("chunk-detect-at-top-surface")
        .description("Render the highlight box at the top surface level of the chunk.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> manualY = sgUtility.add(new IntSetting.Builder()
        .name("manual-y")
        .description("Custom Y height to render the chunk highlight box.")
        .defaultValue(67)
        .min(-60)
        .max(319)
        .sliderMin(-60)
        .sliderMax(319)
        .visible(() -> !detectTopSurface.get())
        .build()
    );

    private final Setting<Boolean> chunkProofStash = sgUtility.add(new BoolSetting.Builder()
        .name("chunk-proof-stash")
        .description("Enables score calculation using block weights per detected block from pickers.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> chunkPortal = sgUtility.add(new BoolSetting.Builder()
        .name("chunk-portal")
        .description("Adds 32 points if a Nether Portal or End Portal is detected in the chunk.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> chunkPointDebug = sgUtility.add(new BoolSetting.Builder()
        .name("chunk-point-debug")
        .description("Displays debug text showing calculated stash points at the surface center of the chunk.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> chunkTracer = sgUtility.add(new BoolSetting.Builder()
        .name("chunk-tracer")
        .description("Displays directional indicators around the crosshair pointing to detected chunks.")
        .defaultValue(true)
        .build()
    );

    // Cache & State Tracking
    private final Map<ChunkPos, Integer> chunkScores = new HashMap<>();
    private final Set<ChunkPos> scannedChunks = new HashSet<>();
    private final Set<ChunkPos> notifiedChunks = new HashSet<>();

    // Fast O(1) Set Caching
    private final Set<Block> activeFinderSet = new ReferenceOpenHashSet<>();
    private int timer = 0;
    private int recheckTimer = 0;

    public ChunkStashModule(Category category) {
        super(category, "chunk-stash", "Helps you identify chunks containing bases or stashes!");
    }

    @Override
    public void onActivate() {
        scannedChunks.clear();
        chunkScores.clear();
        notifiedChunks.clear();
        timer = 0;
        recheckTimer = 0;
    }

    @Override
    public void onDeactivate() {
        scannedChunks.clear();
        chunkScores.clear();
        notifiedChunks.clear();
    }

    private int getBlockWeight(Block block) {
        return BLOCK_WEIGHTS.getInt(block);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        if (checkChunkAgain.get() > 0) {
            recheckTimer++;
            if (recheckTimer >= checkChunkAgain.get()) {
                scannedChunks.clear();
                recheckTimer = 0;
            }
        }

        // Bypassing Delay jika player sedang terbang memakai Elytra
        boolean isFallFlying = mc.player.isGliding();
        if (!isFallFlying) {
            timer++;
            if (timer < scanDelay.get()) return;
            timer = 0;
        }

        ChunkPos playerChunk = mc.player.getChunkPos();
        int renderDistance = mc.options.getClampedViewDistance();

        List<Block> activeList = getActiveFinderList();
        activeFinderSet.clear();
        activeFinderSet.addAll(activeList);

        Set<Block> storageSet = new ReferenceOpenHashSet<>(storageBlock.get());

        // 1. KUMPULKAN DAN URUTKAN CHUNK DARIPADA TERDEKAT KE TERJAUH (SPIRAL SCAN)
        List<ChunkPos> candidates = new ArrayList<>();
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                ChunkPos cPos = new ChunkPos(playerChunk.x + x, playerChunk.z + z);
                if (!scannedChunks.contains(cPos) && mc.world.getChunkManager().isChunkLoaded(cPos.x, cPos.z)) {
                    candidates.add(cPos);
                }
            }
        }

        // Sorting kandidat chunk berdasarkan jarak Euclidian ke player
        candidates.sort(Comparator.comparingInt(c ->
            (c.x - playerChunk.x) * (c.x - playerChunk.x) + (c.z - playerChunk.z) * (c.z - playerChunk.z)
        ));

        // 2. SCAN SECARA BURST (BEBERAPA CHUNK TERDEKAT PER TICK)
        int maxChunksToScan = isFallFlying ? chunksPerTick.get() * 2 : chunksPerTick.get();
        int scannedThisTick = 0;

        for (ChunkPos cPos : candidates) {
            if (scannedThisTick >= maxChunksToScan) break;

            WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cPos.x, cPos.z);
            if (chunk == null) continue;

            scannedChunks.add(cPos);
            scannedThisTick++;

            int currentScore = 0;
            boolean portalFoundInChunk = false;

            // STORAGE SCORE
            int storageCountInChunk = 0;
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                Block block = be.getCachedState().getBlock();
                if (storageSet.contains(block)) {
                    storageCountInChunk++;
                }
            }

            if (storageCountInChunk > storageCount.get()) {
                currentScore += (storageCountInChunk - storageCount.get()) * 5;
            }

            // FINDER BLOCK SCORE & CHUNK PORTAL CHECK
            if ((chunkProofStash.get() && !activeFinderSet.isEmpty()) || chunkPortal.get()) {
                int startX = cPos.getStartX();
                int startZ = cPos.getStartZ();
                int minY = mc.world.getBottomY();
                int maxY = Math.min(mc.world.getTopYInclusive(), 319);

                BlockPos.Mutable mutablePos = new BlockPos.Mutable();

                for (int bx = 0; bx < 16; bx++) {
                    for (int bz = 0; bz < 16; bz++) {
                        int worldX = startX + bx;
                        int worldZ = startZ + bz;
                        int topYAtBlock = mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, worldX, worldZ);

                        for (int by = minY; by <= topYAtBlock && by <= maxY; by++) {
                            mutablePos.set(worldX, by, worldZ);
                            Block block = chunk.getBlockState(mutablePos).getBlock();

                            if (chunkProofStash.get() && activeFinderSet.contains(block)) {
                                currentScore += getBlockWeight(block);
                            }

                            if (chunkPortal.get() && !portalFoundInChunk) {
                                if (block == Blocks.NETHER_PORTAL || block == Blocks.END_PORTAL || block == Blocks.END_PORTAL_FRAME) {
                                    portalFoundInChunk = true;
                                }
                            }
                        }
                    }
                }
            }

            if (portalFoundInChunk) {
                currentScore += 32;
            }

            // LOGIKA UPDATE SCORE
            int finalScore = currentScore;
            if (chunkScores.containsKey(cPos)) {
                int oldScore = chunkScores.get(cPos);
                if (lockHighScore.get() && finalScore < oldScore) {
                    finalScore = oldScore;
                }
            }

            if (finalScore > 0) {
                chunkScores.put(cPos, finalScore);

                if (finalScore >= chunkFinderScore.get() && msgToggle.get() && !notifiedChunks.contains(cPos)) {
                    sendNotification(cPos);
                    notifiedChunks.add(cPos);
                }
            } else if (!lockHighScore.get()) {
                chunkScores.remove(cPos);
            }
        }
    }

    private List<Block> getActiveFinderList() {
        if (mc.world == null) return Collections.emptyList();

        if (manualThreeDim.get()) {
            if (mc.world.getRegistryKey() == World.OVERWORLD) return overworldFinder.get();
            if (mc.world.getRegistryKey() == World.NETHER) return netherFinder.get();
            if (mc.world.getRegistryKey() == World.END) return endFinder.get();
            return Collections.emptyList();
        } else {
            return allDimFinder.get();
        }
    }

    private void sendNotification(ChunkPos pos) {
        String coordsText = pos.x + ", " + pos.z;

        if (msgMode.get() == MsgMode.Chat || msgMode.get() == MsgMode.All) {
            info("Found a Stash at Chunk " + coordsText);
        }

        if (msgMode.get() == MsgMode.Toast || msgMode.get() == MsgMode.All) {
            SystemToast.add(
                mc.getToastManager(),
                SystemToast.Type.PERIODIC_NOTIFICATION,
                Text.literal("§dChunk Stash"),
                Text.literal("Found a Stash on Chunk " + coordsText)
            );
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null || chunkScores.isEmpty()) return;

        SettingColor baseColor = chunkColor.get();
        int alpha = Math.clamp(Math.round(chunkOpacity.get() * 255), 0, 255);

        Color fillColor = new Color(baseColor.r, baseColor.g, baseColor.b, alpha);
        Color lineColor = new Color(baseColor.r, baseColor.g, baseColor.b, baseColor.a);

        int renderDistSq = (mc.options.getClampedViewDistance() + 2) * 16;
        renderDistSq *= renderDistSq;

        for (Map.Entry<ChunkPos, Integer> entry : chunkScores.entrySet()) {
            ChunkPos pos = entry.getKey();
            int score = entry.getValue();

            if (score < chunkFinderScore.get() || !mc.world.getChunkManager().isChunkLoaded(pos.x, pos.z)) continue;

            if (pos.getStartPos().getSquaredDistance(mc.player.getBlockPos()) > renderDistSq) continue;

            double minX = pos.getStartX();
            double minZ = pos.getStartZ();
            double maxX = pos.getEndX() + 1;
            double maxZ = pos.getEndZ() + 1;

            double targetY = detectTopSurface.get()
                ? mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getCenterX(), pos.getCenterZ())
                : manualY.get();

            event.renderer.box(
                minX, targetY, minZ,
                maxX, targetY + 1.0, maxZ,
                fillColor,
                lineColor,
                ShapeMode.Both,
                0
            );
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.world == null || mc.player == null || chunkScores.isEmpty()) return;

        int renderDistSq = (mc.options.getClampedViewDistance() + 2) * 16;
        renderDistSq *= renderDistSq;

        // 1. Render Chunk Point Debug (Text Point di Atas Chunk)
        if (chunkPointDebug.get()) {
            for (Map.Entry<ChunkPos, Integer> entry : chunkScores.entrySet()) {
                ChunkPos pos = entry.getKey();
                int score = entry.getValue();

                if (score <= 0 || !mc.world.getChunkManager().isChunkLoaded(pos.x, pos.z)) continue;
                if (pos.getStartPos().getSquaredDistance(mc.player.getBlockPos()) > renderDistSq) continue;

                double centerX = pos.getCenterX();
                double centerZ = pos.getCenterZ();
                double surfaceY = mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, (int) centerX, (int) centerZ) + 1.2;

                Vector3d textWorldPos = new Vector3d(centerX, surfaceY, centerZ);

                if (NametagUtils.to2D(textWorldPos, 1.5)) {
                    NametagUtils.begin(textWorldPos);

                    String pointText = score + " Points";
                    double nameWidth = TextRenderer.get().getWidth(pointText);
                    double nameHeight = TextRenderer.get().getHeight();

                    Color bgColor = new Color(0, 0, 0, 150);
                    Renderer2D.COLOR.begin();
                    Renderer2D.COLOR.quad(-nameWidth / 2.0 - 2, -1, nameWidth + 4, nameHeight + 2, bgColor);
                    Renderer2D.COLOR.render();

                    Color textColor = score >= chunkFinderScore.get() ? new Color(255, 230, 0) : new Color(255, 255, 255);
                    TextRenderer.get().render(pointText, -nameWidth / 2.0, 0, textColor, true);

                    NametagUtils.end();
                }
            }
        }

        // --- 2. CHUNK TRACER (FILTERED SINGLE ARROW) ---
        if (chunkTracer.get()) {
            float screenCenterX = mc.getWindow().getScaledWidth() / 2.0f;
            float screenCenterY = mc.getWindow().getScaledHeight() / 2.0f;

            float radiusNormal = 35.0f;
            float radiusDebug = 50.0f;

            int colorWhite = 0x80FFFFFF;
            int colorGray  = 0x4D808080;

            DrawContext context = event.drawContext;

            for (Map.Entry<ChunkPos, Integer> entry : chunkScores.entrySet()) {
                ChunkPos pos = entry.getKey();
                int score = entry.getValue();

                if (score <= 0 || !mc.world.getChunkManager().isChunkLoaded(pos.x, pos.z)) continue;
                if (pos.getStartPos().getSquaredDistance(mc.player.getBlockPos()) > renderDistSq) continue;

                double diffX = pos.getCenterX() - mc.player.getX();
                double diffZ = pos.getCenterZ() - mc.player.getZ();

                double targetYaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
                double deltaYaw = MathHelper.wrapDegrees(mc.player.getYaw() - targetYaw);
                double angleRad = Math.toRadians(deltaYaw - 90.0);

                float dirX = (float) Math.cos(angleRad);
                float dirY = (float) Math.sin(angleRad);
                float perpX = -dirY;
                float perpY = dirX;

                if (score >= chunkFinderScore.get()) {
                    drawNativeArrow(context, screenCenterX, screenCenterY, radiusNormal, dirX, dirY, perpX, perpY, colorWhite);
                }
                else if (chunkPointDebug.get()) {
                    drawNativeArrow(context, screenCenterX, screenCenterY, radiusDebug, dirX, dirY, perpX, perpY, colorGray);
                }
            }
        }
    }

    private void drawNativeArrow(DrawContext context, float centerX, float centerY, float radius, float dirX, float dirY, float perpX, float perpY, int color) {
        float tipX = centerX + dirX * radius;
        float tipY = centerY + dirY * radius;
        float size = 7.0f;

        float leftX = tipX - dirX * size + perpX * (size * 0.5f);
        float leftY = tipY - dirY * size + perpY * (size * 0.5f);

        float rightX = tipX - dirX * size - perpX * (size * 0.5f);
        float rightY = tipY - dirY * size - perpY * (size * 0.5f);

        drawPixelLine(context, tipX, tipY, leftX, leftY, color);
        drawPixelLine(context, tipX, tipY, rightX, rightY, color);
    }

    private void drawPixelLine(DrawContext context, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps <= 0) return;

        float xInc = dx / steps;
        float yInc = dy / steps;
        float x = x1;
        float y = y1;

        for (int i = 0; i <= steps; i++) {
            context.fill((int) x, (int) y, (int) x + 1, (int) y + 1, color);
            x += xInc;
            y += yInc;
        }
    }
}
