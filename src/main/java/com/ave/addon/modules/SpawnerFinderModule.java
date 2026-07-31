package com.ave.addon.modules;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class SpawnerFinderModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // 1. Slider "Size Text"
    private final Setting<Double> sizeText = sgGeneral.add(new DoubleSetting.Builder()
        .name("size-text")
        .description("Scale of the floating text nametag.")
        .defaultValue(2.000)
        .sliderMin(0.001)
        .sliderMax(10.000)
        .build()
    );

    // 2. Slider "Size Highlights"
    private final Setting<Double> sizeHighlights = sgGeneral.add(new DoubleSetting.Builder()
        .name("size-highlights")
        .description("Thickness / Radius scale of the beacon beam highlights.")
        .defaultValue(5.000)
        .sliderMin(0.001)
        .sliderMax(10.000)
        .build()
    );

    private final Setting<Boolean> highlightsUp = sgGeneral.add(new BoolSetting.Builder()
        .name("highlights-up")
        .description("Draws a vertical render beacon beam from the spawner up to the sky.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> fillHighlight = sgGeneral.add(new BoolSetting.Builder()
        .name("fill-the-highlight")
        .description("Enables color fill for the spawner box and beacon beam cylinder.")
        .defaultValue(true)
        .build()
    );

    // --- SLIDER BARU: FILL OPACITY (0.001 - 100.000) ---
    private final Setting<Double> fillOpacity = sgGeneral.add(new DoubleSetting.Builder()
        .name("fill-opacity")
        .description("Opacity percentage of the fill highlight (0.001% - 100%).")
        .defaultValue(25.000)
        .min(0.001)
        .max(100.000)
        .sliderMin(0.001)
        .sliderMax(100.000)
        .build()
    );

    private final Setting<Boolean> showMobName = sgGeneral.add(new BoolSetting.Builder()
        .name("show-mob-name-spawner")
        .description("Displays floating text of the mob type inside the spawner.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-distance")
        .description("Displays the distance to the spawner below the mob name.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> tempSave = sgGeneral.add(new BoolSetting.Builder()
        .name("temporary-save-spawner-location")
        .description("Retains spawner locations in memory even if you move out of render distance.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> colorHighlights = sgGeneral.add(new ColorSetting.Builder()
        .name("color-highlights")
        .description("The color used for box outlines, fill, and beacon lines.")
        .defaultValue(new SettingColor(180, 0, 255, 255))
        .build()
    );

    // Cache koordinat spawner dan nama mob
    private final Map<BlockPos, String> spawners = new HashMap<>();

    public SpawnerFinderModule(Category category) {
        super(category, "spawner-finder", "Highlights nearby mob spawners.");
    }

    @Override
    public void onDeactivate() {
        spawners.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        if (!tempSave.get()) {
            spawners.clear();
        }

        int pChunkX = ((int) mc.player.getX()) >> 4;
        int pChunkZ = ((int) mc.player.getZ()) >> 4;

        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(pChunkX + x, pChunkZ + z);
                if (chunk == null) continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof MobSpawnerBlockEntity spawner) {
                        BlockPos pos = blockEntity.getPos();

                        String currentMobName = "Mob Spawner";
                        if (spawner.getLogic() != null) {
                            Entity renderedEntity = spawner.getLogic().getRenderedEntity(mc.world, pos);
                            if (renderedEntity != null) {
                                EntityType<?> type = renderedEntity.getType();
                                if (type != null) {
                                    currentMobName = type.getName().getString();
                                }
                            }
                        }

                        String cachedName = spawners.get(pos);
                        if (cachedName == null || !cachedName.equals(currentMobName)) {
                            if (!currentMobName.equals("Mob Spawner") || cachedName == null) {
                                spawners.put(pos, currentMobName);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null || spawners.isEmpty()) return;

        SettingColor baseColor = colorHighlights.get();
        Color outlineColor = new Color(baseColor.r, baseColor.g, baseColor.b, baseColor.a);

        // Konversi nilai slider (0.001 - 100.000%) ke Alpha RGB (0 - 255)
        int alpha = (int) Math.min(255, Math.max(0, Math.round((fillOpacity.get() / 100.0) * 255)));
        Color fillColor = new Color(baseColor.r, baseColor.g, baseColor.b, alpha);

        ShapeMode shapeMode = fillHighlight.get() ? ShapeMode.Both : ShapeMode.Lines;
        double beamRadius = 0.05 * sizeHighlights.get();

        for (BlockPos pos : spawners.keySet()) {
            if (!tempSave.get() && !mc.world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            // 1. RENDER BOX SPAWNER
            event.renderer.box(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                fillColor,
                outlineColor,
                shapeMode,
                0
            );

            // 2. RENDER BEACON BEAM (SILINDER 3D)
            if (highlightsUp.get()) {
                double centerX = pos.getX() + 0.5;
                double centerZ = pos.getZ() + 0.5;
                double startY = pos.getY() + 1.0;
                double endY = mc.world.getHeight();

                int segments = 12;

                for (int i = 0; i < segments; i++) {
                    double angle1 = (2 * Math.PI * i) / segments;
                    double angle2 = (2 * Math.PI * (i + 1)) / segments;

                    double x1 = centerX + beamRadius * Math.cos(angle1);
                    double z1 = centerZ + beamRadius * Math.sin(angle1);
                    double x2 = centerX + beamRadius * Math.cos(angle2);
                    double z2 = centerZ + beamRadius * Math.sin(angle2);

                    // Render Fill Silinder jika toggle 'fill-the-highlight' aktif & alpha > 0
                    if (fillHighlight.get() && alpha > 0) {
                        event.renderer.quad(
                            x1, startY, z1,
                            x2, startY, z2,
                            x2, endY, z2,
                            x1, endY, z1,
                            fillColor
                        );
                    }

                    // Render Outline Silinder
                    event.renderer.line(x1, startY, z1, x2, startY, z2, outlineColor);
                    event.renderer.line(x1, endY, z1, x2, endY, z2, outlineColor);
                    event.renderer.line(x1, startY, z1, x1, endY, z1, outlineColor);
                }
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.world == null || mc.player == null || spawners.isEmpty()) return;

        for (Map.Entry<BlockPos, String> entry : spawners.entrySet()) {
            BlockPos pos = entry.getKey();
            String mobName = entry.getValue();

            if (!tempSave.get() && !mc.world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            double distanceSq = mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double distance = Math.sqrt(distanceSq);
            String distanceText = formatDistance(distance);

            Vector3d textWorldPos = new Vector3d(pos.getX() + 0.5, mc.player.getEyeY(), pos.getZ() + 0.5);

            double textSizeScale = sizeText.get();
            if (NametagUtils.to2D(textWorldPos, textSizeScale)) {
                NametagUtils.begin(textWorldPos);

                double textX = 0;
                double textY = 0;

                if (showMobName.get()) {
                    double nameWidth = TextRenderer.get().getWidth(mobName);
                    TextRenderer.get().render(mobName, textX - (nameWidth / 2.0), textY, colorHighlights.get(), true);
                    textY += TextRenderer.get().getHeight() + 2;
                }

                if (showDistance.get()) {
                    double distWidth = TextRenderer.get().getWidth(distanceText);
                    TextRenderer.get().render(distanceText, textX - (distWidth / 2.0), textY, new Color(230, 230, 230), true);
                }

                NametagUtils.end();
            }
        }
    }

    private String formatDistance(double meters) {
        if (meters >= 1000) {
            double km = meters / 1000.0;
            if (km >= 10) {
                return String.format("%.0fkm", km);
            } else {
                return String.format("%.1fkm", km).replace(",", ".");
            }
        }
        return String.format("%.0fm", meters);
    }
}
